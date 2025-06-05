package vn.edu.hcmuaf.fit.travelapp.product.data.datasource;

public interface DeleteCallback {
    void onSuccess();
    void onFailure(Throwable error);
}
