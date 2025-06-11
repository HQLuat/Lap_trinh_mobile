// File: app/src/main/java/vn/edu/hcmuaf/fit/travelapp/order/model/Order.java
package vn.edu.hcmuaf.fit.travelapp.order.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.hcmuaf.fit.travelapp.order.utils.OrderUtils;


/**
 * Model representing an Order in Firestore, có thêm field destination.
 */
public class Order implements Parcelable {
    private String orderId;
    private String userId;
    private String imageUrl;
    private double totalAmount;
    private String paymentMethod;
    private String paymentStatus;  // PENDING, PAID, FAILED, CANCELED
    private Timestamp departureDate;
    private String status;         // NEW, CONFIRMED, CANCELED
    private Timestamp createdAt;
    private Timestamp updatedAt;

    private String destination;    // thêm field destination
    private List<OrderItem> items;

    @Exclude
    private PaymentStatus paymentStatusEnum;
    @Exclude
    private OrderStatus orderStatusEnum;

    // Default constructor required for Firestore
    public Order() {}

    // Constructor đầy đủ (khi khởi tạo phía client, truyền destination)
    public Order(String orderId,
                 String userId,
                 String imageUrl,
                 double totalAmount,
                 String paymentMethod,
                 String paymentStatus,
                 Timestamp departureDate,
                 String status,
                 Timestamp createdAt,
                 Timestamp updatedAt,
                 String destination,
                 List<OrderItem> items) {
        this.orderId = orderId;
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.departureDate = departureDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.destination = destination;
        this.items = items;
    }

    // Parcelable constructor
    protected Order(Parcel in) {
        orderId = in.readString();
        userId = in.readString();
        imageUrl = in.readString();
        totalAmount = in.readDouble();
        paymentMethod = in.readString();
        paymentStatus = in.readString();
        departureDate = OrderUtils.ParcelUtils.readTimestamp(in);
        status = in.readString();
        createdAt = OrderUtils.ParcelUtils.readTimestamp(in);
        updatedAt = OrderUtils.ParcelUtils.readTimestamp(in);
        destination = in.readString();
        items = in.createTypedArrayList(OrderItem.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(orderId);
        dest.writeString(userId);
        dest.writeString(imageUrl);
        dest.writeDouble(totalAmount);
        dest.writeString(paymentMethod);
        dest.writeString(paymentStatus);
        OrderUtils.ParcelUtils.writeTimestamp(dest, departureDate);
        dest.writeString(status);
        OrderUtils.ParcelUtils.writeTimestamp(dest, createdAt);
        OrderUtils.ParcelUtils.writeTimestamp(dest, updatedAt);
        dest.writeString(destination);
        dest.writeTypedList(items);
    }

    @Override public int describeContents() { return 0; }

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override public Order createFromParcel(Parcel in) { return new Order(in); }
        @Override public Order[] newArray(int size) { return new Order[size]; }
    };

    // ===== Getters / Setters =====

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
        this.paymentStatusEnum = null;
    }
    @Exclude
    public PaymentStatus getPaymentStatusEnum() {
        if (paymentStatusEnum == null && paymentStatus != null) {
            paymentStatusEnum = PaymentStatus.fromString(paymentStatus);
        }
        return paymentStatusEnum;
    }
    public void setPaymentStatusEnum(PaymentStatus statusEnum) {
        this.paymentStatusEnum = statusEnum;
        this.paymentStatus = statusEnum != null ? statusEnum.toValue() : null;
    }

    public Timestamp getDepartureDate() { return departureDate; }
    public void setDepartureDate(Timestamp departureDate) { this.departureDate = departureDate; }
    @Exclude
    public Date getDepartureDateAsDate() {
        return departureDate != null ? departureDate.toDate() : null;
    }
    @Exclude
    public String getFormattedDepartureDate() {
        return OrderUtils.DateUtils.formatDate(getDepartureDateAsDate());
    }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.orderStatusEnum = null;
    }
    @Exclude
    public OrderStatus getOrderStatusEnum() {
        if (orderStatusEnum == null && status != null) {
            orderStatusEnum = OrderStatus.fromString(status);
        }
        return orderStatusEnum;
    }
    public void setOrderStatusEnum(OrderStatus statusEnum) {
        this.orderStatusEnum = statusEnum;
        this.status = statusEnum != null ? statusEnum.toValue() : null;
    }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    @Exclude
    public String getFormattedCreatedAt() {
        return OrderUtils.DateUtils.formatDateTime(
                createdAt != null ? createdAt.toDate() : null
        );
    }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    @Exclude
    public String getFormattedUpdatedAt() {
        return OrderUtils.DateUtils.formatDateTime(
                updatedAt != null ? updatedAt.toDate() : null
        );
    }

    public String getDestination() {
        return destination != null ? destination : "";
    }
    public void setDestination(String destination) {
        this.destination = destination;
    }

    public List<OrderItem> getItems() {
        return items != null ? items : Collections.emptyList();
    }
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    // ===== Nested enums =====
    public enum PaymentStatus {
        PENDING, PAID, FAILED, CANCELED;
        public static PaymentStatus fromString(String s) {
            return OrderUtils.EnumUtils.safeValueOf(PaymentStatus.class, s);
        }
        public String toDisplayText() {
            switch (this) {
                case PAID: return "Đã thanh toán";
                case PENDING: return "Chờ thanh toán";
                case FAILED: return "Thanh toán thất bại";
                case CANCELED: return "Đã hủy";
                default: return "Không rõ";
            }
        }
        public String toValue() { return name(); }
    }
    public enum OrderStatus {
        NEW, CONFIRMED, CANCELED;
        public static OrderStatus fromString(String s) {
            return OrderUtils.EnumUtils.safeValueOf(OrderStatus.class, s);
        }
        public String toDisplayText() {
            switch (this) {
                case NEW: return "Mới";
                case CONFIRMED: return "Đã xác nhận";
                case CANCELED: return "Đã hủy";
                default: return "Không rõ";
            }
        }
        public String toValue() { return name(); }
    }

    // Field name constants (nếu cần)
    @Exclude public static final String FIELD_ORDER_ID = "orderId";
    @Exclude public static final String FIELD_USER_ID = "userId";
    @Exclude public static final String FIELD_DESTINATION = "destination";
    @Exclude public static final String FIELD_PAYMENT_STATUS = "paymentStatus";
    @Exclude public static final String FIELD_STATUS = "status";
    // ... các FIELD_ khác
}
