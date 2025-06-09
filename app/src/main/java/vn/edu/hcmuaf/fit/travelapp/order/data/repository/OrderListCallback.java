package vn.edu.hcmuaf.fit.travelapp.order.data.repository;

import java.util.List;

import vn.edu.hcmuaf.fit.travelapp.order.model.Order;

public interface OrderListCallback {
    void onSuccess(List<Order> orders);
    void onFailure(Exception e);
}
