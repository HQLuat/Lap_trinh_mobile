package vn.edu.hcmuaf.fit.travelapp.order;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;

import vn.edu.hcmuaf.fit.travelapp.order.data.repository.OrderRepository;
import vn.edu.hcmuaf.fit.travelapp.order.data.repository.OrderRepository.*;
import vn.edu.hcmuaf.fit.travelapp.order.model.Order;

public class OrdersHistoryLogic {
    public interface Callback {
        void onOrdersLoaded(List<Order> orders);
        void onError(String message);
        void onCancelSuccess();
        void onRefundSuccess();
    }

    private OrderRepository repo;
    private Callback callback;
    private String currentUserId;

    public OrdersHistoryLogic(Callback callback) {
        this.callback = callback;
        this.repo = new OrderRepository();
    }

    public void loadOrders(String userId) {
        this.currentUserId = userId;
        repo.getOrdersWithItems(userId,  new OrderListCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                callback.onOrdersLoaded(orders);
            }
            @Override
            public void onFailure(@NonNull Exception e) {
                callback.onError("Lỗi khi tải đơn hàng: " + e.getLocalizedMessage());
            }
        });
    }

    public void cancelOrderBeforePayment(String orderId, String appTransId) {
        repo.cancelOrderBeforePayment(orderId, appTransId)
                .addOnSuccessListener(zpResp -> {
                    callback.onCancelSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onError("Lỗi hủy đơn: " + e.getMessage());
                });
    }


    public void refundPaidOrder(String orderId, String zpTransId, String amount) {
        repo.refundPaidOrder(orderId, zpTransId, amount, "Khách yêu cầu hoàn")
                .addOnSuccessListener(zpResp -> callback.onRefundSuccess())
                .addOnFailureListener(e -> callback.onError("Lỗi hoàn tiền: " + e.getMessage()));
    }
}
