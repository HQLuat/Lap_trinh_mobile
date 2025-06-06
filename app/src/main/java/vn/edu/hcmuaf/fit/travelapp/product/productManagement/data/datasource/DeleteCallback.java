package vn.edu.hcmuaf.fit.travelapp.product.productManagement.data.datasource;

public interface DeleteCallback {
    void onSuccess();
    void onFailure(Throwable error);
}
