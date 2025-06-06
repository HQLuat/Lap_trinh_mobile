package vn.edu.hcmuaf.fit.travelapp.auth.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import vn.edu.hcmuaf.fit.travelapp.auth.data.model.User;

public class UserRepository {

    private final FirebaseFirestore db;
    private final CollectionReference usersRef;
    private final FirebaseAuth auth;

    public UserRepository() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");
        auth = FirebaseAuth.getInstance();
    }

    public void getCurrentUser(OnUserFetchListener listener) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            listener.onFailure("User not logged in");
            return;
        }

        String uid = firebaseUser.getUid();
        usersRef.document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        listener.onSuccess(user);
                    } else {
                        listener.onFailure("User document not found");
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void createOrUpdateUser(User user, OnUserSaveListener listener) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            listener.onFailure("User not logged in");
            return;
        }

        String uid = firebaseUser.getUid();
        usersRef.document(uid).set(user)
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public interface OnUserFetchListener {
        void onSuccess(User user);
        void onFailure(String errorMessage);
    }

    public interface OnUserSaveListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }
}
