package vn.edu.hcmuaf.fit.travelapp.admin.productManagement.data.model;

import com.google.gson.annotations.SerializedName;

public class CloudinaryUploadResponse {
    @SerializedName("secure_url")
    private String secureUrl;

    public String getSecureUrl() {
        return secureUrl;
    }
}
