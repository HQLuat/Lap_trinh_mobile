package vn.edu.hcmuaf.fit.travelapp.product.ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import vn.edu.hcmuaf.fit.travelapp.databinding.ActivityAddProductBinding;
import vn.edu.hcmuaf.fit.travelapp.product.data.model.Product;
import vn.edu.hcmuaf.fit.travelapp.product.viewmodel.ProductViewModel;

public class AddProductActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private ProductViewModel viewModel;
    private Uri imageUri;
    private Calendar departureCalendar;
    private ActivityAddProductBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize binding, viewModel, calendar
        binding = ActivityAddProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        departureCalendar = Calendar.getInstance();

        // initialize viewModel
        viewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                .get(ProductViewModel.class);

        // select date event
        binding.etDepartureDate.setOnClickListener(v -> showDatePicker());

        // Observe success
        viewModel.getIsSuccess().observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                Toast.makeText(this, "Thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show();
//                finish();
            }
        });

        // Observe error message
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
    public void onAddProductClick(View view) {
        Product product = new Product();

        // check product's information
        try {
            String name = binding.etProductName.getText().toString();
            String description = binding.etProductDescription.getText().toString();
            double price = Double.parseDouble(binding.etProductPrice.getText().toString());
            int stock = Integer.parseInt(binding.etProductStock.getText().toString());

            Timestamp departureDate = new Timestamp(departureCalendar.getTime());
            product = new Product(name, description, price, "", stock, departureDate);


        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập giá và số lượng hợp lệ", Toast.LENGTH_SHORT).show();
        }

        // check image
        if (imageUri == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        // add product
        viewModel.addProduct(product, imageUri);
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
}