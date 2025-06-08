package vn.edu.hcmuaf.fit.travelapp.admin.productManagement.data.repository;

import android.content.Context;
import android.net.Uri;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.edu.hcmuaf.fit.travelapp.admin.productManagement.data.datasource.CloudinaryApi;
import vn.edu.hcmuaf.fit.travelapp.admin.productManagement.data.datasource.DeleteCallback;
import vn.edu.hcmuaf.fit.travelapp.admin.productManagement.data.datasource.UploadCallback;
import vn.edu.hcmuaf.fit.travelapp.admin.productManagement.data.model.CloudinaryDeleteResponse;
import vn.edu.hcmuaf.fit.travelapp.admin.productManagement.data.model.CloudinaryUploadResponse;
import vn.edu.hcmuaf.fit.travelapp.admin.productManagement.data.model.Product;

public class ProductRepository {
    private final FirebaseFirestore db;
    private final CollectionReference productsRef;

    public ProductRepository() {
        db = FirebaseFirestore.getInstance();
        productsRef = db.collection("products");
    }

    // get all products
    public Task<QuerySnapshot> getProducts() {
        return productsRef.get();
    }

    // delete product
    public void deleteProduct(String productId, OnCompleteListener<Void> listener) {
        db.collection("products").document(productId)
                .delete()
                .addOnCompleteListener(listener);
    }

    // Update product
    public Task<Void> updateProduct(Product product) {
        return productsRef.document(product.getProductId())
                .set(product, SetOptions.merge());
    }

    // add new product and return ID of newly created product
    public Task<String> addProduct(Product product) {
        // Create new document with automatic ID
        DocumentReference newProductRef = productsRef.document();
        product.setProductId(newProductRef.getId());
        return newProductRef.set(product).continueWith(task -> newProductRef.getId());
    }

    // Update product imageUrl
    public Task<Void> updateProductImage(String productId, String imageUrl) {
        return productsRef.document(productId)
                .update("imageUrl", imageUrl);
    }

    // upload image to Cloudinary and return URL
    public void uploadImageToCloudinary(Context context, Uri imageUri, String productId, UploadCallback callback) {
        String cloudName = "dbpvcjmk0";
        String uploadPreset = "unsigned_preset_1";
        String folderName = "products";
        String fileName = "product_" + productId + ".jpg";

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

    public void deleteImageFromCloudinary(Context context, String publicId, DeleteCallback callback) {
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
                    callback.onFailure(new Exception("Delete failed: " + response.body().getResult()));
                }
            }

            @Override
            public void onFailure(Call<CloudinaryDeleteResponse> call, Throwable t) {
                callback.onFailure(new Exception("Delete failed: response not successful"));
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
}
