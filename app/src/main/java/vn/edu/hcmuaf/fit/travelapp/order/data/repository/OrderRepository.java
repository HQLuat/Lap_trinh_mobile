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
     * Tạo đơn mới cùng items. Sử dụng serverTimestamp cho createdAt/updatedAt.
     */
    public void createOrderWithItems(@NonNull String userId,
                                     @NonNull String imageUrl,
                                     double totalAmount,
                                     @NonNull String paymentMethod,
                                     @NonNull Timestamp departureDate,
                                     @NonNull String destination,            // thêm destination
                                     @NonNull List<OrderItem> items,
                                     @NonNull OrderCreationCallback callback) {
        String orderId = UUID.randomUUID().toString();
        DocumentReference orderRef = ordersRef.document(orderId);
        WriteBatch batch = db.batch();

        // Chuẩn bị bản ghi Order với createdAt, updatedAt là serverTimestamp
        Map<String, Object> orderData = new HashMap<>();
        orderData.put(Order.FIELD_ORDER_ID, orderId);
        orderData.put(Order.FIELD_USER_ID, userId);
        orderData.put(Order.FIELD_DESTINATION, destination);
        orderData.put("imageUrl", imageUrl);
        orderData.put("totalAmount", totalAmount);
        orderData.put("paymentMethod", paymentMethod);
        orderData.put("paymentStatus", Order.PaymentStatus.PENDING.toValue());
        orderData.put("departureDate", departureDate);
        orderData.put("status", Order.OrderStatus.NEW.toValue());
        orderData.put("createdAt", FieldValue.serverTimestamp());
        orderData.put("updatedAt", FieldValue.serverTimestamp());

        batch.set(orderRef, orderData);

        // Ghi từng OrderItem trong subcollection "items"
        for (OrderItem item : items) {
            String itemId = UUID.randomUUID().toString();
            item.setItemId(itemId);
            DocumentReference itemRef = orderRef.collection("items").document(itemId);
            batch.set(itemRef, item);
        }

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

    /**
     * Lấy danh sách orders của user kèm items, sorted theo createdAt giảm dần.
     * Dùng callback.
     */
    public void getOrdersWithItems(@NonNull String userId,
                                   @NonNull OrderListCallback callback) {
        // Query bằng userId và orderBy createdAt desc.
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
                        // Không có đơn hàng nào hoặc items
                        callback.onSuccess(Collections.emptyList());
                        return;
                    }
                    // Khi tất cả itemTasks hoàn thành
                    Tasks.whenAllSuccess(itemTasks)
                            .addOnSuccessListener(listOfSnapshots -> {
                                // Gán items cho từng order tương ứng
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
     * Trả về Task<List<Order>> để caller có thể chain hoặc convert sang LiveData.
     * Ví dụ dùng Tasks API:
     *   getOrdersWithItemsTask(userId).addOnSuccessListener(list -> { ... });
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
                        // Không có đơn hàng => trả về empty list ngay
                        return Tasks.forResult(Collections.emptyList());
                    }
                    // Chờ tất cả tasks của items hoàn thành
                    return Tasks.whenAllSuccess(itemTasks)
                            .continueWith(innerTask -> {
                                if (!innerTask.isSuccessful()) {
                                    throw innerTask.getException();
                                }
                                List<?> rawList = innerTask.getResult(); // List<Object>
                                // Duyệt từng phần tử, ép về QuerySnapshot
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
                                        // trường hợp không phải QuerySnapshot (hiếm xảy ra), bỏ qua hoặc log
                                        Log.w(TAG, "Unexpected itemTasks result type: " + obj);
                                        orders.get(i).setItems(Collections.emptyList());
                                    }
                                }
                                return orders;
                            });
                });
    }

    /**
     * Lấy chi tiết một order (Order + items).
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
