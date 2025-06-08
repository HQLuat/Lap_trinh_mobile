package vn.edu.hcmuaf.fit.travelapp.admin.productManagement.data.datasource;

public interface DeleteCallback {
    void onSuccess();
    void onFailure(Throwable error);
}
