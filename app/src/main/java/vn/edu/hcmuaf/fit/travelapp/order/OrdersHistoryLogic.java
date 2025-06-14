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

    /**
     * Refund a paid order and update status based on ZaloPay response.
     */
    public void refundPaidOrder(@NonNull String orderId,
                                @NonNull String zpTransId,
                                @NonNull String amount) {
        repository.refundPaidOrder(orderId, zpTransId, amount, REFUND_DESCRIPTION)
                .addOnSuccessListener(zpResp -> handleRefundResponse(zpResp))
                .addOnFailureListener(e -> {
                    String err = String.format("Lỗi HTTP khi hoàn tiền: %s", e.getLocalizedMessage());
                    Log.e(TAG, err, e);
                    callback.onError(err);
                });
    }

    private void handleRefundResponse(JSONObject zpResp) {
        try {
            JSONObject status = zpResp.optJSONObject("status_response");
            if (status == null) {
                callback.onError("Phản hồi từ ZaloPay không hợp lệ (thiếu status_response)");
                return;
            }

            int returnCode = status.optInt("return_code", -1);
            String message = status.optString("return_message", "Không rõ lý do");
            String subMessage = status.optString("sub_return_message", "");

            Log.d(TAG, String.format("ZaloPay refund → return_code=%d, message=%s", returnCode, message));

            switch (returnCode) {
                case 1:
                    callback.onRefundSuccess();
                    break;
                case 2:
                    callback.onError("Hoàn tiền thất bại: " + subMessage);
                    break;
                case 3:
                    callback.onError("Hoàn tiền đang xử lý, thử lại sau.");
                    break;
                default:
                    callback.onError("Không xác định được trạng thái hoàn tiền.");
                    break;
            }

        } catch (Exception e) {
            Log.e(TAG, "Lỗi xử lý phản hồi hoàn tiền", e);
            callback.onError("Lỗi định dạng phản hồi từ ZaloPay");
        }
    }

}
