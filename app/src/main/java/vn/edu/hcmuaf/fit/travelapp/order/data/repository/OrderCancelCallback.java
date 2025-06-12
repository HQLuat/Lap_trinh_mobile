package vn.edu.hcmuaf.fit.travelapp.order.data.repository;

public interface OrderCancelCallback {
    void onSuccess();
    void onError(Throwable t);
}

