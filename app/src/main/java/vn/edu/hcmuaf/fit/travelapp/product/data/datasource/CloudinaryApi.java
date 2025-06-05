package vn.edu.hcmuaf.fit.travelapp.product.data.datasource;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import vn.edu.hcmuaf.fit.travelapp.product.data.model.CloudinaryDeleteResponse;
import vn.edu.hcmuaf.fit.travelapp.product.data.model.CloudinaryUploadResponse;

public interface CloudinaryApi {
    @Multipart
    @POST("image/upload")
    Call<CloudinaryUploadResponse> uploadImage(
            @Part MultipartBody.Part file,
            @Part("upload_preset") RequestBody uploadPreset,
            @Part("folder") RequestBody folder
    );

    @FormUrlEncoded
    @POST("image/destroy")
    Call<CloudinaryDeleteResponse> deleteImage(
            @Field("public_id") String publicId,
            @Field("api_key") String apiKey,
            @Field("timestamp") long timestamp,
            @Field("signature") String signature
    );
}

