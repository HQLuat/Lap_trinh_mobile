// File: Order.java
package vn.edu.hcmuaf.fit.travelapp.order.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import vn.edu.hcmuaf.fit.travelapp.order.utils.OrderUtils;

public class Order implements Parcelable {
    private String orderId;
    private String userId;
    private String imageUrl;
    private double totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private Timestamp departureDate;
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String destination;
    private List<OrderItem> items;

    // Các field bổ sung cho ZaloPay
    private String appTransId;
    private String zpTransToken;
    private String zpTransId;
    private String refundId;

    // Firestore không lưu enum
    @Exclude private PaymentStatus paymentStatusEnum;
    @Exclude private OrderStatus orderStatusEnum;

    public Order() {}

    public Order(String orderId, String userId, String imageUrl, double totalAmount,
                 String paymentMethod, String paymentStatus, Timestamp departureDate,
                 String status, Timestamp createdAt, Timestamp updatedAt,
                 String destination, List<OrderItem> items) {
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

        // Read ZaloPay fields
        appTransId = in.readString();
        zpTransToken = in.readString();
        zpTransId = in.readString();
        refundId = in.readString();
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

        // Write ZaloPay fields
        dest.writeString(appTransId);
        dest.writeString(zpTransToken);
        dest.writeString(zpTransId);
        dest.writeString(refundId);
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

    public String getDestination() { return destination != null ? destination : ""; }
    public void setDestination(String destination) { this.destination = destination; }

    public List<OrderItem> getItems() {
        return items != null ? items : Collections.emptyList();
    }
    public void setItems(List<OrderItem> items) { this.items = items; }

    // ===== ZaloPay fields =====

    public String getAppTransId() { return appTransId; }
    public void setAppTransId(String appTransId) { this.appTransId = appTransId; }

    public String getZpTransToken() { return zpTransToken; }
    public void setZpTransToken(String zpTransToken) { this.zpTransToken = zpTransToken; }

    public String getZpTransId() { return zpTransId; }
    public void setZpTransId(String zpTransId) { this.zpTransId = zpTransId; }

    public String getRefundId() { return refundId; }
    public void setRefundId(String refundId) { this.refundId = refundId; }

    // ===== Nested enums =====

    public enum PaymentStatus {
        PENDING,           // Chưa thanh toán
        PAID,              // Đã thanh toán thành công
        FAILED,            // Thanh toán thất bại
        CANCELED,          // Hủy trước khi thanh toán
        REFUND_REQUESTED,  // Đã gửi yêu cầu hoàn tiền
        REFUND_PENDING,    // Hoàn tiền đang xử lý
        REFUNDED,          // Hoàn tiền thành công
        REFUND_FAILED,     // Hoàn tiền thất bại (ZaloPay trả về mã lỗi)
        REFUND_ERROR;      // Lỗi hệ thống khi gọi API hoặc phản hồi không hợp lệ

        public static PaymentStatus fromString(String s) {
            return OrderUtils.EnumUtils.safeValueOf(PaymentStatus.class, s);
        }
        public String toValue() {
            return name();
        }
        public String toDisplayText() {
            switch (this) {
                case PENDING:           return "Chờ thanh toán";
                case PAID:              return "Đã thanh toán";
                case FAILED:            return "Thanh toán thất bại";
                case CANCELED:          return "Đã hủy giao dịch";
                case REFUND_REQUESTED:  return "Đang yêu cầu hoàn tiền";
                case REFUND_PENDING:    return "Đang xử lý hoàn tiền";
                case REFUNDED:          return "Đã hoàn tiền";
                case REFUND_FAILED:     return "Hoàn tiền thất bại";
                case REFUND_ERROR:      return "Lỗi hoàn tiền";
                default:                return "Không rõ";
            }
        }
    }

    public enum OrderStatus {
        NEW,               // Mới tạo, chưa vào chờ thanh toán
        WAITING_PAYMENT,   // Chờ thanh toán
        CANCELED,          // Hủy trước thanh toán hoặc sau hoàn tiền
        PAID,              // Đã thanh toán, chờ xác nhận
        CONFIRMED,         // Đã xác nhận
        COMPLETED,         // Hoàn thành chuyến
        REFUND_REQUESTED,  // Yêu cầu hoàn tiền
        REFUND_PENDING,    // Hoàn tiền đang xử lý
        REFUNDED,          // Hoàn tiền thành công
        REFUND_FAILED,     // Hoàn tiền thất bại
        REFUND_ERROR;      // Lỗi khi gọi API refund

        public static OrderStatus fromString(String s) {
            return OrderUtils.EnumUtils.safeValueOf(OrderStatus.class, s);
        }
        public String toValue() { return name(); }
        public String toDisplayText() {
            switch (this) {
                case NEW:               return "Mới tạo";
                case WAITING_PAYMENT:   return "Chờ thanh toán";
                case CANCELED:          return "Đã hủy";
                case PAID:              return "Đã thanh toán";
                case CONFIRMED:         return "Đã xác nhận";
                case COMPLETED:         return "Hoàn thành";
                case REFUND_REQUESTED:  return "Yêu cầu hoàn tiền";
                case REFUND_PENDING:    return "Đang xử lý hoàn tiền";
                case REFUNDED:          return "Đã hoàn tiền";
                case REFUND_FAILED:     return "Hoàn tiền thất bại";
                case REFUND_ERROR:      return "Lỗi hoàn tiền";
                default:                return "Không rõ";
            }
        }
    }

    // Field name constants (Firestore)
    @Exclude public static final String FIELD_ORDER_ID = "orderId";
    @Exclude public static final String FIELD_USER_ID = "userId";
    @Exclude public static final String FIELD_DESTINATION = "destination";
    @Exclude public static final String FIELD_PAYMENT_STATUS = "paymentStatus";
    @Exclude public static final String FIELD_STATUS = "status";
    @Exclude public static final String FIELD_APP_TRANS_ID = "appTransId";
    @Exclude public static final String FIELD_ZP_TRANS_ID = "zpTransId";
    @Exclude public static final String FIELD_REFUND_ID = "refundId";
}
