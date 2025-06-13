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
import vn.edu.hcmuaf.fit.travelapp.payment.Api.CancelOrder;
import vn.edu.hcmuaf.fit.travelapp.payment.Api.CreateOrder;
import vn.edu.hcmuaf.fit.travelapp.payment.Api.RefundOrder;

public class OrderRepository {
    private static final String TAG = "OrderRepo";

    private final FirebaseFirestore db;
    private final CollectionReference ordersRef;

    // Có thể inject FirebaseFirestore và path collection để dễ test.
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
                        Log.d(TAG, "updatePaymentStatus: success for " + orderId))
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

    public Task<Void> updateAppTransId(@NonNull String orderId, @NonNull String appTransId) {
        return ordersRef.document(orderId).update("appTransId", appTransId)
                .addOnSuccessListener(unused ->
                        Log.d(TAG, "updateAppTransId: success for " + orderId))
                .addOnFailureListener(e ->
                        Log.e(TAG, "updateAppTransId: failure", e));
    }

    public Task<Void> updateZpTransId(@NonNull String orderId, @NonNull String zpTransId) {
        return ordersRef.document(orderId).update("zpTransId", zpTransId)
                .addOnSuccessListener(unused ->
                        Log.d(TAG, "updateZpTransId: success for " + orderId))
                .addOnFailureListener(e ->
                        Log.e(TAG, "updateZpTransId: failure", e));
    }

    /**
     * Hủy đơn trước khi thanh toán:
     * 1. Gọi ZaloPay cancel API
     * 2. Nếu return_code == 0 thì cập nhật Firestore thành CANCELED
     * @return Task<JSONObject> chứa response từ ZaloPay hoặc exception nếu lỗi
     */
    public Task<JSONObject> cancelOrderBeforePayment(@NonNull String orderId,
                                                     @NonNull String appTransId) {
        return Tasks.call(() -> {
            CancelOrder api = new CancelOrder();
            return api.cancel(appTransId);
        }).onSuccessTask(zpResponse -> {
            int returnCode = zpResponse.optInt("return_code", -1);
            if (returnCode == 0) {
                return updatePaymentStatus(orderId, PaymentStatus.CANCELED, OrderStatus.CANCELED)
                        .continueWith(task -> zpResponse);
            } else {
                throw new Exception("ZaloPay cancel failed: "
                        + zpResponse.optString("return_message"));
            }
        });
    }

    /**
     * Hoàn tiền đơn đã thanh toán:
     * 1. Gọi RefundOrder API với zpTransId, amount, description.
     * 2. Nếu return_code == 0, cập nhật Firestore: status thành REFUNDED, paymentStatus thành CANCELED.
     * @return Task<JSONObject> trả về kết quả JSON từ ZaloPay, caller tiếp tục .addOnSuccessListener
     */
    public Task<JSONObject> refundPaidOrder(@NonNull String orderId,
                                            @NonNull String zpTransId,
                                            @NonNull String amount,
                                            @NonNull String description) {
        return Tasks.call(() -> {
            RefundOrder api = new RefundOrder();
            return api.refund(zpTransId, amount, description);
        }).onSuccessTask(zpResponse -> {
            int returnCode = zpResponse.optInt("return_code", -1);
            if (returnCode == 0) {
                return updatePaymentStatus(orderId, PaymentStatus.CANCELED, OrderStatus.REFUNDED)
                        .continueWith(task -> zpResponse);
            } else {
                throw new Exception("ZaloPay refund failed: " + zpResponse.optString("return_message"));
            }
        });
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

    /**
     * Tương tự getOrdersWithItems nhưng trả về Task để chaining hoặc LiveData.
     */
    public Task<List<Order>> getOrdersWithItemsTask(@NonNull String userId) {
        return ordersRef
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    QuerySnapshot qs = task.getResult();
                    List<Order> orders = new ArrayList<>();
                    List<Task<QuerySnapshot>> itemTasks = new ArrayList<>();
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        Order order = doc.toObject(Order.class);
                        if (order != null) {
                            orders.add(order);
                            itemTasks.add(ordersRef
                                    .document(order.getOrderId())
                                    .collection("items")
                                    .get());
                        }
                    }
                    if (itemTasks.isEmpty()) {
                        return Tasks.forResult(Collections.emptyList());
                    }
                    return Tasks.whenAllSuccess(itemTasks)
                            .continueWith(innerTask -> {
                                if (!innerTask.isSuccessful()) {
                                    throw innerTask.getException();
                                }
                                List<?> rawList = innerTask.getResult(); // List<Object>
                                for (int i = 0; i < rawList.size(); i++) {
                                    Object obj = rawList.get(i);
                                    if (obj instanceof QuerySnapshot) {
                                        QuerySnapshot itemsSnap = (QuerySnapshot) obj;
                                        List<OrderItem> itemList = new ArrayList<>();
                                        for (DocumentSnapshot itemDoc : itemsSnap.getDocuments()) {
                                            OrderItem item = itemDoc.toObject(OrderItem.class);
                                            if (item != null) {
                                                itemList.add(item);
                                            }
                                        }
                                        orders.get(i).setItems(itemList);
                                    } else {
                                        Log.w(TAG, "Unexpected itemTasks result type: " + obj);
                                        orders.get(i).setItems(Collections.emptyList());
                                    }
                                }
                                return orders;
                            });
                });
    }

    /**
     * Lấy chi tiết một order.
     * 1. Lấy document order theo orderId.
     * 2. Nếu tồn tại, lấy subcollection items và gán vào model.
     */
    public void getOrderDetail(@NonNull String orderId,
                               @NonNull OrderDetailCallback callback) {
        DocumentReference orderDocRef = ordersRef.document(orderId);
        orderDocRef.get()
                .addOnSuccessListener(docSnap -> {
                    if (!docSnap.exists()) {
                        callback.onFailure(new Exception("Order not found: " + orderId));
                        return;
                    }
                    Order order = docSnap.toObject(Order.class);
                    if (order == null) {
                        callback.onFailure(new Exception("Failed to parse Order: " + orderId));
                        return;
                    }
                    // Fetch items
                    orderDocRef.collection("items").get()
                            .addOnSuccessListener(itemsSnap -> {
                                List<OrderItem> itemList = new ArrayList<>();
                                for (DocumentSnapshot itemDoc : itemsSnap.getDocuments()) {
                                    OrderItem item = itemDoc.toObject(OrderItem.class);
                                    if (item != null) itemList.add(item);
                                }
                                order.setItems(itemList);
                                callback.onSuccess((List<Order>) order);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "getOrderDetail: failure fetching items for " + orderId, e);
                                callback.onFailure(e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getOrderDetail: failure fetching order " + orderId, e);
                    callback.onFailure(e);
                });
    }

}

