package vn.edu.hcmuaf.fit.travelapp.admin.productManagement.ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import vn.edu.hcmuaf.fit.travelapp.R;
import vn.edu.hcmuaf.fit.travelapp.databinding.ActivityAddEditProductBinding;
import vn.edu.hcmuaf.fit.travelapp.admin.productManagement.data.model.Product;
import vn.edu.hcmuaf.fit.travelapp.admin.productManagement.viewmodel.ProductViewModel;

public class AddEditProductActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private ProductViewModel viewModel;
    private Uri imageUri;
    private Calendar departureCalendar;
    private ActivityAddEditProductBinding binding;
    private boolean isEditMode;
    private Product existingProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();

        setupUiForMode();
        setupObservers();
    }

    public void init() {
        // initialize calendar
        departureCalendar = Calendar.getInstance();

        // initialize viewModel
        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(ProductViewModel.class);

        // change mode
        existingProduct = getIntent().getParcelableExtra("product");
        if (existingProduct != null) {
            isEditMode = true;
        } else {
            isEditMode = false;
        }
    }

    private void setupUiForMode() {
        binding.btnAddProduct.setText(isEditMode ? "Cập nhật sản phẩm" : "Thêm sản phẩm");
        binding.tvTitle.setText(isEditMode ? "Cập nhật sản phẩm" : "Thêm sản phẩm");
        binding.etDepartureDate.setOnClickListener(v -> showDatePicker());
        if(isEditMode) populateForm(existingProduct);
    }

    private void setupObservers() {
        viewModel.getIsSuccess().observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                setResult(RESULT_OK);
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker() {
        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    departureCalendar.set(year, month, dayOfMonth);
                    binding.etDepartureDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    .format(departureCalendar.getTime()));
                },
                departureCalendar.get(Calendar.YEAR),
                departureCalendar.get(Calendar.MONTH),
                departureCalendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // add product event
    public void onSaveProductClick(View view) {
        Product product = new Product();

        // check product information
        try {
            String name = binding.etProductName.getText().toString();
            String description = binding.etProductDescription.getText().toString();
            double price = Double.parseDouble(binding.etProductPrice.getText().toString());
            int stock = Integer.parseInt(binding.etProductStock.getText().toString());

            Timestamp departureDate = new Timestamp(departureCalendar.getTime());
            product = new Product(name, description, price, "", stock, departureDate, "abc");


        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập giá và số lượng hợp lệ", Toast.LENGTH_SHORT).show();
        }

        // mode
        if (isEditMode) {
            // update product
            product.setProductId(existingProduct.getProductId());
            product.setImageUrl(existingProduct.getImageUrl());
            viewModel.updateProduct(product, imageUri);
        } else {
            // check image
            if (imageUri == null) {
                Toast.makeText(this, "Vui lòng chọn ảnh sản phẩm", Toast.LENGTH_SHORT).show();
                return;
            }

            // add product
            viewModel.addProduct(product, imageUri);
        }
    }

    // on click select image
    public void onSelectImageClick(View view) {
        if (checkSelfPermission()) {
            openImagePicker();
        } else {
            requestStoragePermission();
        }
    }

    // check permission
    private boolean checkSelfPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    // request permission
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        }
    }

    // after successful authorization
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền để chọn ảnh.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // open image picker
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    // after picking image successfully
    private ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    binding.ivProductImage.setImageURI(imageUri);

                    // hide icon
                    binding.ivAddIcon.setVisibility(View.GONE);
                }
            }
    );

    // load product information
    private void populateForm(Product product) {
        Log.d("populateForm", "populateForm");
        binding.etProductName.setText(product.getName());
        binding.etProductDescription.setText(product.getDescription());
        binding.etProductPrice.setText(String.valueOf(product.getPrice()));
        binding.etProductStock.setText(String.valueOf(product.getStock()));

        // Convert Timestamp to String
        Date departureDate = product.getDepartureDate().toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String formattedDate = sdf.format(departureDate);
        binding.etDepartureDate.setText(formattedDate);

        // load image into imageView
        Glide.with(this)
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_picture)
                .into(binding.ivProductImage);

        // hide icon
        binding.ivAddIcon.setVisibility(View.GONE);
    }
}