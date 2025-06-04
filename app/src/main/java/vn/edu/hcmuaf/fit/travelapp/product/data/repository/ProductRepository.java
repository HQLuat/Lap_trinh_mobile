package vn.edu.hcmuaf.fit.travelapp.product.data.repository;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.UUID;

import vn.edu.hcmuaf.fit.travelapp.product.data.model.Product;

public class ProductRepository {
    private final FirebaseFirestore db;
    private final CollectionReference productsRef;

    public ProductRepository() {
        db = FirebaseFirestore.getInstance();
        productsRef = db.collection("products");
    }

    // Thêm sản phẩm mới và trả về ID của sản phẩm vừa tạo
    public Task<String> addProduct(Product product) {
        // Tạo document mới với ID tự động
        DocumentReference newProductRef = productsRef.document();
        product.setProductId(newProductRef.getId()); // Gán ID vào product
        return newProductRef.set(product).continueWith(task -> newProductRef.getId());
    }

    // (Optional) Upload ảnh lên Firebase Storage và trả về URL
//    public Task<String> uploadProductImage(Uri imageUri) {
//        FirebaseStorage storage = FirebaseStorage.getInstance();
//        StorageReference storageRef = storage.getReference()
//                .child("product_images/" + UUID.randomUUID().toString());
//
//        return storageRef.putFile(imageUri)
//                .continueWithTask(task -> storageRef.getDownloadUrl())
//                .continueWith(task -> task.getResult().toString());
//    }
}
