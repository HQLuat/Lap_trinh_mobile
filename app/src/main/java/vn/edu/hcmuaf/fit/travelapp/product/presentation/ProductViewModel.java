package vn.edu.hcmuaf.fit.travelapp.product.presentation;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import vn.edu.hcmuaf.fit.travelapp.product.data.model.Product;
import vn.edu.hcmuaf.fit.travelapp.product.data.repository.ProductRepository;

public class ProductViewModel extends ViewModel {
    private final ProductRepository repository;
    private final MutableLiveData<Boolean> isSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ProductViewModel() {
        repository = new ProductRepository();
    }

    // Thêm sản phẩm mới
    public void addProduct(Product product, Uri imageUri) {
        if (imageUri != null) {
            // Nếu có ảnh, upload ảnh trước
//            repository.uploadProductImage(imageUri)
//                    .addOnSuccessListener(imageUrl -> {
//                        product.setImageUrl(imageUrl);
//                        saveProductToFirestore(product);
//                    })
//                    .addOnFailureListener(e -> {
//                        errorMessage.setValue("Lỗi upload ảnh: " + e.getMessage());
//                    });
        } else {
            // Nếu không có ảnh, lưu luôn sản phẩm
            saveProductToFirestore(product);
        }
    }

    private void saveProductToFirestore(Product product) {
        repository.addProduct(product)
                .addOnSuccessListener(productId -> {
                    isSuccess.setValue(true);
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Lỗi thêm sản phẩm: " + e.getMessage());
                });
    }

    // LiveData để UI observe
    public LiveData<Boolean> getIsSuccess() { return isSuccess; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}