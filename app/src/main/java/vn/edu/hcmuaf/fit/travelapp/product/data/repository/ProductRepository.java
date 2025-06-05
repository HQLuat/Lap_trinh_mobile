package vn.edu.hcmuaf.fit.travelapp.product.data.repository;

import android.content.Context;
import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.edu.hcmuaf.fit.travelapp.product.data.datasource.CloudinaryApi;
import vn.edu.hcmuaf.fit.travelapp.product.data.datasource.UploadCallback;
import vn.edu.hcmuaf.fit.travelapp.product.data.model.CloudinaryUploadResponse;
import vn.edu.hcmuaf.fit.travelapp.product.data.model.Product;

public class ProductRepository {
    private final FirebaseFirestore db;
    private final CollectionReference productsRef;

    public ProductRepository() {
        db = FirebaseFirestore.getInstance();
        productsRef = db.collection("products");
    }

    // Add new product and return ID of newly created product
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

    // Upload image to Cloudinary and return URL
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
}
