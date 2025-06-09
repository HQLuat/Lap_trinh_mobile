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
import java.util.List;
import java.util.UUID;

import vn.edu.hcmuaf.fit.travelapp.order.model.Order;
import vn.edu.hcmuaf.fit.travelapp.order.model.OrderItem;

public class OrderRepo {
    private final FirebaseFirestore db;
    private final CollectionReference ordersRef;

    public OrderRepo(FirebaseFirestore db, CollectionReference ordersRef) {
        this.db = db;
        this.ordersRef = ordersRef;
    }

    // No-arg constructor — khởi tạo bằng instance mặc định của Firebase
    public OrderRepo() {
        this(
                FirebaseFirestore.getInstance(),
                FirebaseFirestore.getInstance().collection("orders")
        );
    }

    public void createOrderWithItems(@NonNull String userId,
                                     @NonNull String imageUrl,
                                     @NonNull double totalAmount,
                                     @NonNull String paymentMethod,
                                     @NonNull Timestamp departureDate,
                                     @NonNull List<OrderItem> items,
                                     @NonNull OrderCreationCallback callback) {

        String orderId = UUID.randomUUID().toString();
        Timestamp now = Timestamp.now();

        // Tạo object Order
        Order order = new Order(
                orderId,
                userId,
                imageUrl,
                totalAmount,
                paymentMethod,
                "PENDING",
                departureDate,
                "NEW",
                now,
                now
        );

        DocumentReference orderRef = ordersRef.document(orderId);
        WriteBatch batch = db.batch();

        // Ghi đơn hàng (kèm imageUrl)
        batch.set(orderRef, order);

        // Ghi từng sản phẩm trong subcollection
        for (OrderItem item : items) {
            String itemId = UUID.randomUUID().toString();
            item.setItemId(itemId);

            DocumentReference itemRef = orderRef.collection("items").document(itemId);
            batch.set(itemRef, item);
        }

        // Commit batch
        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess(orderId))
                .addOnFailureListener(callback::onFailure);
    }

    public void updatePaymentStatus(String orderId, String paymentStatus, String status) {
        ordersRef.document(orderId).update(
                        "paymentStatus", paymentStatus,
                        "status", status,
                        "updatedAt", FieldValue.serverTimestamp()
                ).addOnSuccessListener(unused -> Log.d("OrderRepo", "Updated payment status"))
                .addOnFailureListener(e -> Log.e("OrderRepo", "Failed to update", e));
    }

    public void getOrdersWithItems(@NonNull String userId,
                                   @NonNull OrderListCallback callback) {
        ordersRef
                // chỉ lọc theo userId, không orderBy
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Order> orders = new ArrayList<>();
                    List<Task<QuerySnapshot>> itemTasks = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Order order = doc.toObject(Order.class);
                        if (order != null) {
                            orders.add(order);
                            Task<QuerySnapshot> t =
                                    ordersRef.document(order.getOrderId())
                                            .collection("items")
                                            .get();
                            itemTasks.add(t);
                        }
                    }

                    Tasks.whenAllSuccess(itemTasks)
                            .addOnSuccessListener(listOfSnapshots -> {
                                // gán items vào từng order
                                for (int i = 0; i < listOfSnapshots.size(); i++) {
                                    QuerySnapshot itemsSnap = (QuerySnapshot) listOfSnapshots.get(i);
                                    List<OrderItem> items = new ArrayList<>();
                                    for (DocumentSnapshot itemDoc : itemsSnap.getDocuments()) {
                                        OrderItem item = itemDoc.toObject(OrderItem.class);
                                        if (item != null) items.add(item);
                                    }
                                    orders.get(i).setItems(items);
                                }

                                // *** Sort thủ công theo createdAt giảm dần ***
                                Collections.sort(orders, (o1, o2) ->
                                        // giả sử getCreatedAt() trả về com.google.firebase.Timestamp
                                        o2.getCreatedAt().compareTo(o1.getCreatedAt())
                                );

                                callback.onSuccess(orders);
                            })
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }


}
