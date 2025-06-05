package vn.edu.hcmuaf.fit.travelapp.product.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import vn.edu.hcmuaf.fit.travelapp.product.data.datasource.UploadCallback;
import vn.edu.hcmuaf.fit.travelapp.product.data.model.Product;
import vn.edu.hcmuaf.fit.travelapp.product.data.repository.ProductRepository;

public class ProductViewModel extends AndroidViewModel {
    private final ProductRepository repository;
    private final MutableLiveData<Boolean> isSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ProductViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository();
    }

    // Add new product
    public void addProduct(Product product, Uri imageUri) {
        if (imageUri == null) {
            errorMessage.setValue("Vui lòng chọn ảnh sản phẩm trước khi thêm.");
            return;
        }

        // Step 1: Tạo product trước (chưa có ảnh)
        repository.addProduct(product)
                .addOnSuccessListener(productId -> {
                    product.setProductId(productId);
                    // Step 2: Upload ảnh với tên theo productId
                    repository.uploadImageToCloudinary(getApplication(), imageUri, productId, new UploadCallback() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            product.setImageUrl(imageUrl);
                            // Step 3: Cập nhật lại product có imageUrl
                            repository.updateProductImage(productId, imageUrl)
                                    .addOnSuccessListener(aVoid -> isSuccess.setValue(true))
                                    .addOnFailureListener(e -> errorMessage.setValue("Lỗi cập nhật ảnh: " + e.getMessage()));
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            errorMessage.setValue("Lỗi upload ảnh: " + t.getMessage());
                        }
                    });
                })
                .addOnFailureListener(e -> errorMessage.setValue("Lỗi thêm sản phẩm: " + e.getMessage()));
    }

    public LiveData<Boolean> getIsSuccess() { return isSuccess; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}
