package vn.edu.hcmuaf.fit.travelapp.auth.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import vn.edu.hcmuaf.fit.travelapp.auth.data.model.User;

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

    public void createOrUpdateUser(User user, OnUserSaveListener listener) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            listener.onFailure("User not logged in");
            return;
        }

        String uid = firebaseUser.getUid();
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
                        listener.onSuccess(auth.getCurrentUser());
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthInvalidUserException) {
                            listener.onFailure("Email not found.");
                        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            listener.onFailure("Incorrect password.");
                        } else {
                            listener.onFailure("Login failed: " + (e != null ? e.getMessage() : "Unknown error"));
                        }
                    }
                });
    }



    public interface OnUserFetchListener {
        void onSuccess(User user);
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
        void onSuccess(FirebaseUser user);
        void onFailure(String errorMessage);
    }

}
