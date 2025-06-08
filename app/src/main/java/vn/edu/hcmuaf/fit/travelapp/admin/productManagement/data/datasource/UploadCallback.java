package vn.edu.hcmuaf.fit.travelapp.admin.productManagement.data.datasource;

public interface UploadCallback {
    void onSuccess(String imageUrl);
    void onFailure(Throwable error);
}
