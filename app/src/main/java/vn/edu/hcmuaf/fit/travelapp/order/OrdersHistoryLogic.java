package vn.edu.hcmuaf.fit.travelapp.order;

import android.util.Log;
import androidx.annotation.NonNull;
import org.json.JSONObject;
import java.util.List;
import vn.edu.hcmuaf.fit.travelapp.order.data.repository.OrderRepository;
import vn.edu.hcmuaf.fit.travelapp.order.data.repository.OrderRepository.OrderListCallback;
import vn.edu.hcmuaf.fit.travelapp.order.model.Order;

/**
 * Logic for loading, canceling, and refunding orders.
 */
public class OrdersHistoryLogic {
    public interface Callback {
        void onOrdersLoaded(List<Order> orders);
        void onError(String message);
        void onCancelSuccess();
        void onRefundSuccess();
        void onRefundPending();
    }

    private static final String TAG = OrdersHistoryLogic.class.getSimpleName();
    private static final String REFUND_DESCRIPTION = "Khach hang yeu cau hoan tien";

    private final OrderRepository repository;
    private final Callback callback;
    private String currentUserId;

    public OrdersHistoryLogic(@NonNull Callback callback) {
        this.callback = callback;
        this.repository = new OrderRepository();
    }

    /**
     * Load order history for a user.
     */
    public void loadOrders(@NonNull String userId) {
        this.currentUserId = userId;
        repository.getOrdersWithItems(userId, new OrderListCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                callback.onOrdersLoaded(orders);
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                String msg = String.format("Lỗi khi tải đơn hàng: %s", e.getLocalizedMessage());
                Log.e(TAG, msg, e);
                callback.onError(msg);
            }
        });
    }

    /**
     * Cancel an order before payment.
     */
    public void cancelOrderBeforePayment(@NonNull String orderId, @NonNull String appTransId) {
        repository.cancelOrderBeforePayment(orderId, appTransId)
                .addOnSuccessListener(zpResp -> {
                    Log.d(TAG, "Hủy đơn thành công: " + orderId);
                    callback.onCancelSuccess();
                })
                .addOnFailureListener(e -> {
                    String err = String.format("Lỗi hủy đơn: %s", e.getLocalizedMessage());
                    Log.e(TAG, err, e);
                    callback.onError(err);
                });
    }

    public void refundPaidOrder(@NonNull String orderId,
                                @NonNull String zpTransId,
                                @NonNull String amount) {
        // Step 1: mark requested
        repository.updatePaymentStatus(orderId,
                        Order.PaymentStatus.REFUND_REQUESTED,
                        Order.OrderStatus.REFUND_REQUESTED)
                .addOnSuccessListener(u ->
                        // Step 2: call refund to get m_refund_id
                        repository.refundOrder(orderId, zpTransId, amount, REFUND_DESCRIPTION)
                                .addOnSuccessListener(mRefundId -> {
                                    Log.d(TAG, "Immediate refund accepted, m_refund_id=" + mRefundId);
                                    // Step 3: polling
                                    repository.pollRefundStatus(orderId, mRefundId, new OrderRepository.RefundCallback() {
                                        @Override public void onSuccess(JSONObject status) { callback.onRefundSuccess(); }
                                        @Override public void onFailure(String msg) { callback.onError("Hoàn tiền thất bại: " + msg); }
                                        @Override public void onTimeout(String msg) { callback.onRefundPending(); }
                                        @Override public void onError(Exception e) { callback.onError("Polling error: " + e.getMessage()); }
                                    });
                                })
                                .addOnFailureListener(e -> callback.onError("Refund API error: " + e.getMessage()))
                )
                .addOnFailureListener(e -> callback.onError("Cannot set REFUND_REQUESTED"));
    }
}



