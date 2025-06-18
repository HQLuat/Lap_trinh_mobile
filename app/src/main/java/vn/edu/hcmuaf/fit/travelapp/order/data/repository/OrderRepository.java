package vn.edu.hcmuaf.fit.travelapp.order.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import vn.edu.hcmuaf.fit.travelapp.order.model.Order;
import vn.edu.hcmuaf.fit.travelapp.order.model.Order.OrderStatus;
import vn.edu.hcmuaf.fit.travelapp.order.model.Order.PaymentStatus;
import vn.edu.hcmuaf.fit.travelapp.order.model.OrderItem;
import vn.edu.hcmuaf.fit.travelapp.payment.Api.RefundOrder;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OrderRepository {
    private static final String TAG = "OrderRepo";
    private static final Executor executor = Executors.newCachedThreadPool();
    private final FirebaseFirestore db;
    private final CollectionReference ordersRef;

    // Trong lớp RefundOrder hoặc lớp Repository của bạn
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // Số lần tối đa polling, khoảng cách giữa các lần (giây)
    private static final int MAX_POLL_ATTEMPTS = 10;
    private static final int POLL_INTERVAL_SEC = 30;

    public OrderRepository(FirebaseFirestore db, CollectionReference ordersRef) {
        this.db = db;
        this.ordersRef = ordersRef;
    }

    public OrderRepository() {
        this(
                FirebaseFirestore.getInstance(),
                FirebaseFirestore.getInstance().collection("orders")
        );
    }


    /**
     * Tạo đơn hàng mới cùng items.
     * Các bước:
     * 1. Tạo orderId và chuẩn bị dữ liệu cơ bản (userId, destination, totalAmount, paymentMethod, departureDate).
     * 2. Dùng WriteBatch để ghi Order và từng item trong subcollection "items".
     * 3. Commit batch, onSuccess gọi callback.onSuccess(orderId), onFailure gọi callback.onFailure.
     */
    public void createOrderWithItems(@NonNull String userId,
                                     @NonNull String imageUrl,
                                     double totalAmount,
                                     @NonNull String paymentMethod,
                                     @NonNull Timestamp departureDate,
                                     @NonNull String destination,
                                     @NonNull List<OrderItem> items,
                                     String appTransId,
                                     @NonNull OrderCreationCallback callback) {
        // 1. Tạo order cục bộ
        String orderId = UUID.randomUUID().toString();
        DocumentReference orderRef = ordersRef.document(orderId);
        WriteBatch batch = db.batch();

        Map<String,Object> orderData = new HashMap<>();
        orderData.put("orderId",      orderId);
        orderData.put("userId",       userId);
        orderData.put("destination",  destination);
        orderData.put("imageUrl",     imageUrl);
        orderData.put("totalAmount",  totalAmount);
        orderData.put("paymentMethod",paymentMethod);
        orderData.put("paymentStatus", PaymentStatus.PENDING.toValue());
        orderData.put("status",        OrderStatus.NEW.toValue());
        orderData.put("departureDate", departureDate);
        orderData.put("createdAt",     FieldValue.serverTimestamp());
        orderData.put("updatedAt",     FieldValue.serverTimestamp());
        orderData.put("appTransId",    appTransId);

        batch.set(orderRef, orderData);

        for (OrderItem item : items) {
            String itemId = UUID.randomUUID().toString();
            item.setItemId(itemId);
            DocumentReference itemRef = orderRef.collection("items").document(itemId);
            batch.set(itemRef, item);
        }

        // 2. Commit Firestore
        batch.commit()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "createOrderWithItems: success, orderId=" + orderId);
                    callback.onSuccess(orderId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "createOrderWithItems: failure", e);
                    callback.onFailure(e);
                });
    }

    /**
     * Cập nhật paymentStatus và order status, đồng thời updatedAt = serverTimestamp.
     * Tham số nên truyền enum.toValue(): ví dụ PaymentStatus.PAID.toValue()
     */
    public Task<Void> updatePaymentStatus(@NonNull String orderId,
                                          @NonNull PaymentStatus paymentStatus,
                                          @NonNull OrderStatus orderStatus) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("paymentStatus", paymentStatus.toValue());
        updates.put("status", orderStatus.toValue());
        updates.put("updatedAt", FieldValue.serverTimestamp());

        return ordersRef.document(orderId)
                .update(updates)
                .addOnSuccessListener(unused ->
                        Log.d(TAG, "updatePaymentStatus: success for " + orderId
                                + " -> paymentStatus=" + paymentStatus
                                + ", orderStatus=" + orderStatus))
                .addOnFailureListener(e ->
                        Log.e(TAG, "updatePaymentStatus: failure for " + orderId, e));
    }


    public Task<Void> updateZaloPayInfo(String orderId, String zpTransToken, String paymentUrl) {
        Map<String, Object> update = new HashMap<>();
        update.put("zpTransToken", zpTransToken);
        update.put("paymentUrl", paymentUrl);
        update.put("updatedAt", FieldValue.serverTimestamp());

        return ordersRef.document(orderId).update(update);
    }

    public Task<Void> updateZaloTransId(String orderId, String zpTransId) {
        return FirebaseFirestore.getInstance()
                .collection("orders")
                .document(orderId)
                .update("zpTransId", zpTransId);
    }

    /**
     * Hủy đơn trước khi thanh toán:
     * 1. Gọi ZaloPay cancel API
     * 2. Nếu return_code == 0 thì cập nhật Firestore thành CANCELED
     * @return Task<JSONObject> chứa response từ ZaloPay hoặc exception nếu lỗi
     */
    public Task<JSONObject> cancelOrderBeforePayment(@NonNull String orderId,
                                                     @NonNull String appTransId) {
        // Trường hợp đơn chưa thanh toán: không gọi API ZaloPay, chỉ xử lý nội bộ
        return updatePaymentStatus(orderId, PaymentStatus.CANCELED, OrderStatus.CANCELED)
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        // Trả về kết quả giả lập giống ZaloPay để tầng trên xử lý đồng nhất
                        JSONObject result = new JSONObject();
                        result.put("return_code", 0);
                        result.put("return_message", "Hủy đặt vé thành công");
                        return result;
                    } else {
                        // Cập nhật nội bộ thất bại
                        Exception e = task.getException();
                        throw new Exception("Hủy đặt vé thất bại", e);
                    }
                });
    }

    // 1. Call Refund API, returns m_refund_id
    public Task<String> refundOrder(@NonNull String orderId,
                                    @NonNull String zpTransId,
                                    @NonNull String amount,
                                    @NonNull String description) {
        return Tasks.call(executor, () -> {
            RefundOrder api = new RefundOrder();
            JSONObject fullResp = api.refund(zpTransId, amount, description);
            Log.d(TAG, "Refund API full response: " + fullResp);
            String mRefundId = fullResp.optString("m_refund_id", null);
            if (mRefundId == null || mRefundId.isEmpty()) {
                throw new Exception("m_refund_id not found in refund response");
            }
            return mRefundId;
        });
    }

    // 2. Synchronous query for polling
    public JSONObject queryRefundStatusSync(String mRefundId) throws Exception {
        return new RefundOrder().queryRefundStatus(mRefundId);
    }

    // 3. Polling logic: update Firestore and callback
    public void pollRefundStatus(@NonNull String orderId,
                                 @NonNull String mRefundId,
                                 @NonNull RefundCallback callback) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        final int[] attempts = {0};

        Runnable task = () -> {
            attempts[0]++;
            try {
                JSONObject status = queryRefundStatusSync(mRefundId);
                int code = status.optInt("return_code", -1);
                String subMsg = status.optString("sub_return_message", "");

                switch (code) {
                    case 1: // success
                        scheduler.shutdown();
                        updatePaymentStatus(orderId,
                                Order.PaymentStatus.REFUNDED,
                                Order.OrderStatus.REFUNDED)
                                .addOnSuccessListener(u -> callback.onSuccess(status))
                                .addOnFailureListener(callback::onError);
                        break;
                    case 2: // failed
                        scheduler.shutdown();
                        updatePaymentStatus(orderId,
                                Order.PaymentStatus.REFUND_FAILED,
                                Order.OrderStatus.REFUND_FAILED)
                                .addOnSuccessListener(u -> callback.onFailure(subMsg))
                                .addOnFailureListener(callback::onError);
                        break;
                    case 3: // pending
                        if (attempts[0] >= MAX_POLL_ATTEMPTS) {
                            scheduler.shutdown();
                            updatePaymentStatus(orderId,
                                    Order.PaymentStatus.REFUND_PENDING,
                                    Order.OrderStatus.REFUND_PENDING)
                                    .addOnSuccessListener(u -> callback.onTimeout("Timeout - still pending"))
                                    .addOnFailureListener(callback::onError);
                        }
                        break;
                    default:
                        scheduler.shutdown();
                        updatePaymentStatus(orderId,
                                Order.PaymentStatus.REFUND_ERROR,
                                Order.OrderStatus.REFUND_ERROR)
                                .addOnSuccessListener(u -> callback.onFailure("Unknown status: " + code))
                                .addOnFailureListener(callback::onError);
                        break;
                }
            } catch (Exception e) {
                scheduler.shutdown();
                updatePaymentStatus(orderId,
                        Order.PaymentStatus.REFUND_ERROR,
                        Order.OrderStatus.REFUND_ERROR);
                callback.onError(e);
            }
        };
        scheduler.scheduleAtFixedRate(task, 0, POLL_INTERVAL_SEC, TimeUnit.SECONDS);
    }

    /**
     * Lấy danh sách orders của user kèm items, sắp xếp theo createdAt giảm dần.
     * 1. Query orders theo userId.
     * 2. Với mỗi order, tạo Task để lấy subcollection items.
     * 3. Dùng Tasks.whenAllSuccess để chờ tất cả, sau đó gán items vào model.
     */
    public void getOrdersWithItems(@NonNull String userId,
                                   @NonNull OrderListCallback callback) {
        ordersRef
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Order> orders = new ArrayList<>();
                    List<Task<QuerySnapshot>> itemTasks = new ArrayList<>();
                    List<String> orderIds = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Order order = doc.toObject(Order.class);
                        if (order != null) {
                            orders.add(order);
                            orderIds.add(order.getOrderId());
                            Task<QuerySnapshot> t = ordersRef
                                    .document(order.getOrderId())
                                    .collection("items")
                                    .get();
                            itemTasks.add(t);
                        }
                    }
                    if (itemTasks.isEmpty()) {
                        callback.onSuccess(Collections.emptyList());
                        return;
                    }
                    Tasks.whenAllSuccess(itemTasks)
                            .addOnSuccessListener(listOfSnapshots -> {
                                for (int i = 0; i < listOfSnapshots.size(); i++) {
                                    @SuppressWarnings("unchecked")
                                    QuerySnapshot itemsSnap = (QuerySnapshot) listOfSnapshots.get(i);
                                    List<OrderItem> itemList = new ArrayList<>();
                                    for (DocumentSnapshot itemDoc : itemsSnap.getDocuments()) {
                                        OrderItem item = itemDoc.toObject(OrderItem.class);
                                        if (item != null) {
                                            itemList.add(item);
                                        }
                                    }
                                    orders.get(i).setItems(itemList);
                                }
                                callback.onSuccess(orders);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "getOrdersWithItems: failure fetching items", e);
                                callback.onFailure(e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getOrdersWithItems: failure fetching orders", e);
                    callback.onFailure(e);
                });
    }

    public interface OrderCancelCallback {
        void onSuccess();
        void onError(Throwable t);
    }

    public interface OrderCreationCallback {
        void onSuccess(String orderId);
        void onFailure(Exception e);
    }

    public interface OrderDetailCallback {
        void onSuccess(List<Order> orders);
        void onFailure(@NonNull Exception e);
    }

    public interface OrderListCallback {
        void onSuccess(List<Order> orders);
        void onFailure(Exception e);
    }

    public interface RefundCallback {
        void onSuccess(JSONObject statusResp);
        void onFailure(String message);
        void onTimeout(String message);
        void onError(Exception e);
    }

}
