package vn.edu.hcmuaf.fit.travelapp.product.presentation;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import vn.edu.hcmuaf.fit.travelapp.R;
import vn.edu.hcmuaf.fit.travelapp.product.data.model.Product;

public class AddProductActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ProductViewModel viewModel;
    private Uri imageUri;
    private Calendar departureCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        departureCalendar = Calendar.getInstance();

        // Xử lý chọn ngày
        EditText etDepartureDate = findViewById(R.id.et_departure_date);
        etDepartureDate.setOnClickListener(v -> showDatePicker());

        // Observe kết quả
        viewModel.getIsSuccess().observe(this, isSuccess -> {
            if (isSuccess) finish();
        });
    }

    private void showDatePicker() {
        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    departureCalendar.set(year, month, dayOfMonth);
                    ((EditText) findViewById(R.id.et_departure_date))
                            .setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    .format(departureCalendar.getTime()));
                },
                departureCalendar.get(Calendar.YEAR),
                departureCalendar.get(Calendar.MONTH),
                departureCalendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    public void onAddProductClick(View view) {
        String name = ((EditText) findViewById(R.id.et_product_name)).getText().toString();
        String description = ((EditText) findViewById(R.id.et_product_description)).getText().toString();
        double price = Double.parseDouble(((EditText) findViewById(R.id.et_product_price)).getText().toString());
        int stock = Integer.parseInt(((EditText) findViewById(R.id.et_product_stock)).getText().toString());

        // Tạo Timestamp từ Calendar
        Timestamp departureDate = new Timestamp(departureCalendar.getTime());

        Product product = new Product(name, description, price, "", stock, departureDate);
        viewModel.addProduct(product, imageUri);
    }

    // ... (phần chọn ảnh giữ nguyên)
}