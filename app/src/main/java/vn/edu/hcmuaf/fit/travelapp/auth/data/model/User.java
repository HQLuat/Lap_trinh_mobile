package vn.edu.hcmuaf.fit.travelapp.auth.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class User implements Parcelable {
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
    private boolean active = true;
    private boolean deleted = false;

    public User() {
    }

    public User(String userId, String fullName, String phoneNumber, String email,
                String profileImageUrl, int role, @Nullable List<String> bookingHistory,
                String address, String gender, @Nullable Date dateOfBirth, @Nullable Timestamp createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.bookingHistory = bookingHistory != null ? new ArrayList<>(bookingHistory) : new ArrayList<>();
        this.address = address;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.createdAt = createdAt;
    }

    public User(User other) {
        this(
                other.userId,
                other.fullName,
                other.phoneNumber,
                other.email,
                other.profileImageUrl,
                other.role,
                other.bookingHistory,
                other.address,
                other.gender,
                other.dateOfBirth != null ? new Date(other.dateOfBirth.getTime()) : null,
                other.createdAt
        );
        this.active = other.active;
        this.deleted = other.deleted;
    }

    protected User(Parcel in) {
        userId = in.readString();
        fullName = in.readString();
        phoneNumber = in.readString();
        email = in.readString();
        profileImageUrl = in.readString();
        role = in.readInt();
        bookingHistory = in.createStringArrayList();
        address = in.readString();
        gender = in.readString();

        long dobMillis = in.readLong();
        dateOfBirth = dobMillis != -1 ? new Date(dobMillis) : null;

        long createdAtMillis = in.readLong();
        createdAt = createdAtMillis != -1 ? new Timestamp(new Date(createdAtMillis)) : null;

        active = in.readByte() != 0;
        deleted = in.readByte() != 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(fullName);
        dest.writeString(phoneNumber);
        dest.writeString(email);
        dest.writeString(profileImageUrl);
        dest.writeInt(role);
        dest.writeStringList(bookingHistory);
        dest.writeString(address);
        dest.writeString(gender);
        dest.writeLong(dateOfBirth != null ? dateOfBirth.getTime() : -1);
        dest.writeLong(createdAt != null ? createdAt.toDate().getTime() : -1);
        dest.writeByte((byte) (active ? 1 : 0));
        dest.writeByte((byte) (deleted ? 1 : 0));
    }

    // Getters & Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public int getRole() { return role; }
    public void setRole(int role) { this.role = role; }

    public List<String> getBookingHistory() { return bookingHistory; }
    public void setBookingHistory(List<String> bookingHistory) {
        this.bookingHistory = bookingHistory != null ? new ArrayList<>(bookingHistory) : new ArrayList<>();
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Date getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}