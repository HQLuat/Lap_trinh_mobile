package vn.edu.hcmuaf.fit.travelapp.auth.data.model;

import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.List;

public class User {
    private String userId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String profileImageUrl;
    private int role;
    private List<String> bookingHistory;
    private String address;
    private String gender;
    private Date dateOfBirth;
    private Timestamp createdAt;

    public User() {
    }

    public User(String userId, String fullName, String phoneNumber, String email, String profileImageUrl, int role, List<String> bookingHistory, String address, String gender, Date dateOfBirth, Timestamp createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.bookingHistory = bookingHistory;
        this.address = address;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public List<String> getBookingHistory() {
        return bookingHistory;
    }

    public void setBookingHistory(List<String> bookingHistory) {
        this.bookingHistory = bookingHistory;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
