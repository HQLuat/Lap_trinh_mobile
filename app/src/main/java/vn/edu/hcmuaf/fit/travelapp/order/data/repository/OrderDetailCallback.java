package vn.edu.hcmuaf.fit.travelapp.order.data.repository;

import androidx.annotation.NonNull;

import java.util.List;

import vn.edu.hcmuaf.fit.travelapp.order.model.Order;

public interface OrderDetailCallback {
    void onSuccess(List<Order> orders);
    void onFailure(@NonNull Exception e);
}
