package vn.edu.hcmuaf.fit.travelapp.product.productManagement.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;

import vn.edu.hcmuaf.fit.travelapp.databinding.ActivityProductManagementBinding;
import vn.edu.hcmuaf.fit.travelapp.product.productManagement.adapter.ProductAdapter;
import vn.edu.hcmuaf.fit.travelapp.product.productManagement.data.model.Product;
import vn.edu.hcmuaf.fit.travelapp.product.productManagement.data.repository.OnProductActionListener;
import vn.edu.hcmuaf.fit.travelapp.product.productManagement.viewmodel.ProductViewModel;

public class ProductManagementActivity extends AppCompatActivity {
    private ActivityProductManagementBinding binding;
    private ProductViewModel viewModel;
    private ProductAdapter adapter;
    private ActivityResultLauncher<Intent> addProductLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initialize adapter
        adapter = new ProductAdapter(new ArrayList<>(), new OnProductActionListener() {
            @Override
            public void onDeleteProduct(Product product) {
                new AlertDialog.Builder(ProductManagementActivity.this)
                        .setTitle("Xoá sản phẩm")
                        .setMessage("Bạn có chắc muốn xoá sản phẩm này?")
                        .setPositiveButton("Xoá", (dialog, which) -> {
                            viewModel.deleteProduct(product);
                        })
                        .setNegativeButton("Huỷ", null)
                        .show();
            }

            @Override
            public void onUpdateProduct(Product product) {
                Intent intent = new Intent(ProductManagementActivity.this, AddEditProductActivity.class);
                intent.putExtra("product", product);
                addProductLauncher.launch(intent);
            }
        });

        binding.recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewProducts.setAdapter(adapter);

        // initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // observe productList
        viewModel.getProductList().observe(this, products -> {
            if (products != null && !products.isEmpty()) {
                adapter.setProductList(products);
            } else {
                adapter.setProductList(new ArrayList<>());
                Toast.makeText(this, "Không có sản phẩm nào.", Toast.LENGTH_SHORT).show();
            }
        });

        // observe errors
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });

        // observe message
        viewModel.getMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        // show products
        viewModel.fetchProducts();

        // after adding or editing product, reload the product list
        addProductLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        viewModel.fetchProducts();
                    }
                }
        );

        // Add event handler for add button
        binding.fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditProductActivity.class);
            addProductLauncher.launch(intent);
        });
    }
}