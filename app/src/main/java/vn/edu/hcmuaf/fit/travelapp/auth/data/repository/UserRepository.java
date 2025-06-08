package vn.edu.hcmuaf.fit.travelapp.auth.data.repository;

import android.content.Context;
import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.edu.hcmuaf.fit.travelapp.auth.data.model.User;
import vn.edu.hcmuaf.fit.travelapp.product.productManagement.data.datasource.CloudinaryApi;
import vn.edu.hcmuaf.fit.travelapp.product.productManagement.data.datasource.DeleteCallback;
import vn.edu.hcmuaf.fit.travelapp.product.productManagement.data.datasource.UploadCallback;
import vn.edu.hcmuaf.fit.travelapp.product.productManagement.data.model.CloudinaryDeleteResponse;
import vn.edu.hcmuaf.fit.travelapp.product.productManagement.data.model.CloudinaryUploadResponse;

public class UserRepository {

    private final FirebaseFirestore db;
    private final CollectionReference usersRef;
    private final FirebaseAuth auth;

    public UserRepository() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
        auth = FirebaseAuth.getInstance();
    }

    public void getCurrentUser(OnUserFetchListener listener) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            listener.onFailure("User not logged in");
            return;
        }

        String uid = firebaseUser.getUid();
        usersRef.document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        listener.onSuccess(user);
                    } else {
                        listener.onFailure("User document not found");
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void updateUser(User user, OnUserSaveListener listener) {
//        FirebaseUser firebaseUser = auth.getCurrentUser();
//        if (firebaseUser == null) {
//            listener.onFailure("User not logged in");
//            return;
//        }

//        String uid = firebaseUser.getUid();
        String uid = user.getUserId();
        usersRef.document(uid).set(user)
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void registerUser(String fullName, String email, String password, OnUserRegisterListener listener) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && auth.getCurrentUser() != null) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        String userId = firebaseUser.getUid();

                        User newUser = new User();
                        newUser.setUserId(userId);
                        newUser.setFullName(fullName);
                        newUser.setEmail(email);
                        newUser.setPhoneNumber("");
                        newUser.setRole(2);
                        newUser.setCreatedAt(new com.google.firebase.Timestamp(new java.util.Date()));

                        usersRef.document(userId).set(newUser)
                                .addOnSuccessListener(unused -> listener.onSuccess(firebaseUser))
                                .addOnFailureListener(e -> listener.onFailure("Error saving user information: " + e.getMessage()));
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            listener.onFailure("Email is already registered.");
                        } else {
                            listener.onFailure("Registration failed: " + (e != null ? e.getMessage() : "Unknown error"));
                        }
                    }
                });
    }

    public void loginUser(String email, String password, OnUserLoginListener listener) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && auth.getCurrentUser() != null) {
                        String userId = auth.getCurrentUser().getUid();

                        // Lấy thông tin người dùng từ Firestore
                        usersRef.document(userId).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        User user = documentSnapshot.toObject(User.class);
                                        listener.onSuccess(user);
                                    } else {
                                        listener.onFailure("User document not found in database.");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    listener.onFailure("Failed to retrieve user data: " + e.getMessage());
                                });
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthInvalidUserException || e instanceof FirebaseAuthInvalidCredentialsException) {
                            listener.onFailure("Email or password is incorrect.");
                        } else {
                            listener.onFailure("Login failed: " + (e != null ? e.getMessage() : "Unknown error"));
                        }
                    }

                });
    }


    public void logoutUser() {
        auth.signOut();
    }

    public void deleteUserById(String userId, OnUserDeleteListener listener) {
        usersRef.document(userId)
                .update("isDeleted", true)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void getAllUsers(OnUsersFetchListener listener) {
        usersRef.whereEqualTo("deleted", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            users.add(user);
                        }
                    }
                    listener.onSuccess(users);
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void findUserById(String userId, OnUserFetchListener listener) {
        usersRef.document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && !Boolean.TRUE.equals(user.isDeleted())) {
                            listener.onSuccess(user);
                        } else {
                            listener.onFailure("User not found or has been deleted");
                        }
                    } else {
                        listener.onFailure("User document not found");
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void uploadProfileImageToCloudinary(Context context, Uri imageUri, UploadCallback callback) {
        String cloudName = "dbpvcjmk0";
        String uploadPreset = "unsigned_preset_1";
        String folderName = "users";

        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        String userId = firebaseUser.getUid();
        String fileName = "user_" + userId + ".jpg";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.cloudinary.com/v1_1/" + cloudName + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CloudinaryApi api = retrofit.create(CloudinaryApi.class);

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            byte[] imageBytes = new byte[inputStream.available()];
            inputStream.read(imageBytes);
            inputStream.close();

            RequestBody requestFile = RequestBody.create(imageBytes, MediaType.parse("image/*"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileName, requestFile);

            RequestBody preset = RequestBody.create(uploadPreset, MediaType.parse("text/plain"));
            RequestBody folder = RequestBody.create(folderName, MediaType.parse("text/plain"));

            api.uploadImage(body, preset, folder).enqueue(new Callback<CloudinaryUploadResponse>() {
                @Override
                public void onResponse(Call<CloudinaryUploadResponse> call, Response<CloudinaryUploadResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        callback.onSuccess(response.body().getSecureUrl());
                    } else {
                        callback.onFailure(new Exception("Upload failed"));
                    }
                }

                @Override
                public void onFailure(Call<CloudinaryUploadResponse> call, Throwable t) {
                    callback.onFailure(t);
                }
            });

        } catch (IOException e) {
            callback.onFailure(e);
        }
    }

    public void deleteUserImageFromCloudinary(Context context, String publicId, DeleteCallback callback) {
        String cloudName = "dbpvcjmk0";
        String apiKey = "599825456567849";
        String apiSecret = "gN40yfRK0VrKQRXDYB1rZho4UEY";

        long timestamp = System.currentTimeMillis() / 1000L;
        String toSign = "public_id=" + publicId + "&timestamp=" + timestamp + apiSecret;
        String signature = sha1(toSign);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.cloudinary.com/v1_1/" + cloudName + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CloudinaryApi api = retrofit.create(CloudinaryApi.class);

        api.deleteImage(publicId, apiKey, timestamp, signature).enqueue(new Callback<CloudinaryDeleteResponse>() {
            @Override
            public void onResponse(Call<CloudinaryDeleteResponse> call, Response<CloudinaryDeleteResponse> response) {
                if (response.isSuccessful() && response.body() != null && "ok".equalsIgnoreCase(response.body().getResult())) {
                    callback.onSuccess();
                } else {
                    callback.onFailure(new Exception("Delete failed: " + (response.body() != null ? response.body().getResult() : "No response")));
                }
            }

            @Override
            public void onFailure(Call<CloudinaryDeleteResponse> call, Throwable t) {
                callback.onFailure(new Exception("Delete failed: " + t.getMessage()));
            }
        });
    }

    private String sha1(String input) {
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA-1");
            byte[] result = mDigest.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not found");
        }
    }

    public interface OnUserFetchListener {
        void onSuccess(User user);
        void onFailure(String errorMessage);
    }

    public interface OnUsersFetchListener {
        void onSuccess(List<User> users);
        void onFailure(String errorMessage);
    }

    public interface OnUserSaveListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface OnUserRegisterListener {
        void onSuccess(FirebaseUser user);
        void onFailure(String errorMessage);
    }

    public interface OnUserLoginListener {
        void onSuccess(User user);
        void onFailure(String errorMessage);
    }

    public interface OnUserDeleteListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }
}
