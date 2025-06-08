package vn.edu.hcmuaf.fit.travelapp.order.model;

import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.List;

/**
 * Model representing an Order in Firestore.
 */
public class Order implements Serializable {
    private String orderId;
    private String userId;
    private double totalAmount;
    private String paymentMethod;
    private String paymentStatus;  // PENDING, PAID, CANCELED
    private Timestamp departureDate;
    private String status;         // NEW, CONFIRMED, CANCELED
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Default constructor required for calls to DataSnapshot.getValue(Order.class)
    public Order() {}

    public Order(String orderId, String userId, double totalAmount, String paymentMethod, String paymentStatus,
                 Timestamp departureDate, String status, Timestamp createdAt, Timestamp updatedAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.departureDate = departureDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public Timestamp getDepartureDate() { return departureDate; }
    public void setDepartureDate(Timestamp departureDate) { this.departureDate = departureDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}