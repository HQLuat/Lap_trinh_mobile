package vn.edu.hcmuaf.fit.travelapp.order.data.repository;

public interface OrderCreationCallback {
    void onSuccess(String orderId);
    void onFailure(Exception e);
}
