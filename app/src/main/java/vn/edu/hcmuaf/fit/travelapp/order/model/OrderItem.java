package vn.edu.hcmuaf.fit.travelapp.order.model;

import java.io.Serializable;
import com.google.firebase.Timestamp;

/**
 * Model representing an OrderItem subcollection in Firestore.
 */
public class OrderItem implements Serializable {
    private String itemId;
    private String productId;
    private int quantity;
    private double unitPrice;
    private Timestamp tourDate;  // Optional: date of tour
    private String guestName;    // Optional: name of guest

    // Default constructor for Firestore
    public OrderItem() {}

    public OrderItem(String itemId, String productId, int quantity, double unitPrice,
                     Timestamp tourDate, String guestName) {
        this.itemId = itemId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.tourDate = tourDate;
        this.guestName = guestName;
    }

    // Getters and Setters
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public Timestamp getTourDate() { return tourDate; }
    public void setTourDate(Timestamp tourDate) { this.tourDate = tourDate; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
}

