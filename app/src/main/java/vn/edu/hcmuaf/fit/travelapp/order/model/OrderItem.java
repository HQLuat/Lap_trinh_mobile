// File: app/src/main/java/vn/edu/hcmuaf/fit/travelapp/order/model/OrderItem.java
package vn.edu.hcmuaf.fit.travelapp.order.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Model representing an OrderItem in Firestore.
 * - Implement Parcelable.
 * - Firestore yêu cầu default constructor và public getters/setters.
 * - @Exclude cho helper methods.
 */
public class OrderItem implements Parcelable {
    private String itemId;
    private String productId;
    private int quantity;
    private double unitPrice;
    private Timestamp tourDate;
    private String guestName;

    // Default constructor for Firestore
    public OrderItem() {}

    public OrderItem(String itemId,
                     String productId,
                     int quantity,
                     double unitPrice,
                     Timestamp tourDate,
                     String guestName) {
        this.itemId = itemId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.tourDate = tourDate;
        this.guestName = guestName;
    }

    // Parcelable constructor
    protected OrderItem(Parcel in) {
        itemId = in.readString();
        productId = in.readString();
        quantity = in.readInt();
        unitPrice = in.readDouble();
        long ts = in.readLong();
        tourDate = ts != -1 ? new Timestamp(new Date(ts)) : null;
        guestName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(itemId);
        dest.writeString(productId);
        dest.writeInt(quantity);
        dest.writeDouble(unitPrice);
        if (tourDate != null) {
            dest.writeLong(tourDate.toDate().getTime());
        } else {
            dest.writeLong(-1);
        }
        dest.writeString(guestName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<OrderItem> CREATOR = new Creator<OrderItem>() {
        @Override
        public OrderItem createFromParcel(Parcel in) {
            return new OrderItem(in);
        }
        @Override
        public OrderItem[] newArray(int size) {
            return new OrderItem[size];
        }
    };

    // ===== Getters / Setters =====

    public String getItemId() {
        return itemId;
    }
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getProductId() {
        return productId;
    }
    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Timestamp getTourDate() {
        return tourDate;
    }
    public void setTourDate(Timestamp tourDate) {
        this.tourDate = tourDate;
    }
    @Exclude
    public Date getTourDateAsDate() {
        return tourDate != null ? tourDate.toDate() : null;
    }
    @Exclude
    public String getFormattedTourDate() {
        Date date = getTourDateAsDate();
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    public String getGuestName() {
        return guestName;
    }
    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    // ===== (Nếu cần) Field name constants cho Firestore queries =====
    @Exclude public static final String FIELD_ITEM_ID = "itemId";
    @Exclude public static final String FIELD_PRODUCT_ID = "productId";
    // ... thêm các FIELD_ khác nếu cần
}
