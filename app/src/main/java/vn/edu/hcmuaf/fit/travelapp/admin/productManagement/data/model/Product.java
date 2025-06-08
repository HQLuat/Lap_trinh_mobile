package vn.edu.hcmuaf.fit.travelapp.admin.productManagement.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;

import java.util.Date;

public class Product implements Parcelable {
    private String productId;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private int stock;
    private Timestamp departureDate;
    private boolean isActive;
    private String address;

    public Product() {
    }

    public Product(String name, String description, double price, String imageUrl, int stock, Timestamp departureDate, String address) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.stock = stock;
        this.departureDate = departureDate;
        this.isActive = true;
        this.address = address;
    }

    // --- Parcelable implementation ---
    protected Product(Parcel in) {
        productId = in.readString();
        name = in.readString();
        description = in.readString();
        price = in.readDouble();
        imageUrl = in.readString();
        stock = in.readInt();
        long timestampMillis = in.readLong();
        departureDate = timestampMillis != 0 ? new Timestamp(new Date(timestampMillis)) : null;
        isActive = in.readByte() != 0;
        address = in.readString();
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public Timestamp getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(Timestamp departureDate) {
        this.departureDate = departureDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(productId);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeDouble(price);
        dest.writeString(imageUrl);
        dest.writeInt(stock);
        dest.writeLong(departureDate != null ? departureDate.toDate().getTime() : 0);
        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeString(address);
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };
}
