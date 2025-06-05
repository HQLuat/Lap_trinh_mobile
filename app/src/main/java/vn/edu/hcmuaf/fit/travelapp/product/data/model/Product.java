package vn.edu.hcmuaf.fit.travelapp.product.data.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class Product implements Serializable {
    private String productId;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private int stock;
    private Timestamp departureDate;
    private boolean isActive;

    public Product() {
    }

    public Product(String name, String description, double price, String imageUrl, int stock, Timestamp departureDate) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.stock = stock;
        this.departureDate = departureDate;
        this.isActive = true;
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
}
