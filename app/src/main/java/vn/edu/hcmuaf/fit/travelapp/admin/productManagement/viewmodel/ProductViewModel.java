package vn.edu.hcmuaf.fit.travelapp.admin.productManagement.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import vn.edu.hcmuaf.fit.travelapp.admin.productManagement.data.datasource.DeleteCallback;
import vn.edu.hcmuaf.fit.travelapp.admin.productManagement.data.datasource.UploadCallback;
import vn.edu.hcmuaf.fit.travelapp.admin.productManagement.data.model.Product;
import vn.edu.hcmuaf.fit.travelapp.admin.productManagement.data.repository.ProductRepository;

public class ProductViewModel extends AndroidViewModel {
    private final ProductRepository repository;
    private final MutableLiveData<List<Product>> productList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
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

        // Step 1: Create product first (no image yet)
        repository.addProduct(product)
                .addOnSuccessListener(productId -> {
                    product.setProductId(productId);
                    // Step 2: Upload image with name according to productId
                    repository.uploadImageToCloudinary(getApplication(), imageUri, productId, new UploadCallback() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            product.setImageUrl(imageUrl);
                            // Step 3: Update product with imageUrl
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

    // Get products
    public void fetchProducts() {
        repository.getProducts()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Product> products = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Product product = doc.toObject(Product.class);
                            products.add(product);
                        }
                        productList.postValue(products);
                    } else {
                        errorMessage.postValue(task.getException() != null ?
                                task.getException().getMessage() : "Lỗi lấy dữ liệu");
                    }
                });
    }

    // Delete product
    public void deleteProduct(Product product) {
        // delete image in cloudinary
        String imageUrl = product.getImageUrl();
        String publicId = extractPublicIdFromUrl(imageUrl);
        if (publicId != null) {
            repository.deleteImageFromCloudinary(getApplication(), publicId, new DeleteCallback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(Throwable t) {
                    errorMessage.setValue("Không thể xóa ảnh: " + t.getMessage());
                }
            });
        }

        // delete product in firestore database
        repository.deleteProduct(product.getProductId(), task -> {
            if (task.isSuccessful()) {
                fetchProducts();
                message.setValue("Đã xoá sản phẩm thành công");
            } else {
                message.setValue("Xoá sản phẩm thất bại: " + task.getException().getMessage());
            }
        });
    }

    // Update product
    public void updateProduct(Product product, @Nullable Uri imageUri) {
        if (product.getProductId() == null || product.getProductId().isEmpty()) {
            errorMessage.setValue("ID sản phẩm không hợp lệ.");
            return;
        }

        if (imageUri != null) {
            // Delete old photos
            String oldImageUrl = product.getImageUrl();
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                // Extract publicId from old image URL
                String publicId = extractPublicIdFromUrl(oldImageUrl);
                if (publicId != null) {
                    repository.deleteImageFromCloudinary(getApplication(), publicId, new DeleteCallback() {
                        @Override
                        public void onSuccess() {
                            // After deleting old photo, upload new photo
                            uploadNewImageAndUpdate(product, imageUri);
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            errorMessage.setValue("Không thể xóa ảnh cũ: " + t.getMessage());
                        }
                    });
                } else {
                    errorMessage.setValue("Không thể xác định public_id ảnh cũ.");
                }
            } else {
                // there is no old photos, just upload new photos
                uploadNewImageAndUpdate(product, imageUri);
            }
        } else {
            // there is no new photos, just product updates
            repository.updateProduct(product)
                    .addOnSuccessListener(aVoid -> isSuccess.setValue(true))
                    .addOnFailureListener(e -> errorMessage.setValue("Lỗi cập nhật sản phẩm: " + e.getMessage()));
        }
    }

    private void uploadNewImageAndUpdate(Product product, Uri imageUri) {
        repository.uploadImageToCloudinary(getApplication(), imageUri, product.getProductId(), new UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                product.setImageUrl(imageUrl);
                repository.updateProduct(product)
                        .addOnSuccessListener(aVoid -> isSuccess.setValue(true))
                        .addOnFailureListener(e -> errorMessage.setValue("Lỗi cập nhật sản phẩm: " + e.getMessage()));
            }

            @Override
            public void onFailure(Throwable t) {
                errorMessage.setValue("Lỗi upload ảnh: " + t.getMessage());
            }
        });
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        try {
            Uri uri = Uri.parse(imageUrl);
            String path = uri.getPath();
            if (path != null) {
                int index = path.indexOf("/products/");
                if (index != -1) {
                    return path.substring(index + 1, path.lastIndexOf("."));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public LiveData<List<Product>> getProductList() {
        return productList;
    }
    public LiveData<Boolean> getIsSuccess() { return isSuccess; }
    public LiveData<String> getMessage() {
        return message;
    }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}
