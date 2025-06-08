package vn.edu.hcmuaf.fit.travelapp.order.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

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
                                     @NonNull double totalAmount,
                                     @NonNull String paymentMethod,
                                     @NonNull Timestamp departureDate,
                                     @NonNull List<OrderItem> items,
                                     @NonNull OrderCreationCallback callback) {

        String orderId = UUID.randomUUID().toString();
        Timestamp now = Timestamp.now();

        Order order = new Order(
                orderId,
                userId,
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

        // Ghi đơn hàng
        batch.set(orderRef, order);

        // Ghi từng sản phẩm trong subcollection
        for (OrderItem item : items) {
            String itemId = UUID.randomUUID().toString();
            item.setItemId(itemId);

            DocumentReference itemRef = orderRef.collection("items").document(itemId);
            batch.set(itemRef, item);
        }

        // Commit
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
}
