package vn.edu.hcmuaf.fit.travelapp.admin.ui;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import vn.edu.hcmuaf.fit.travelapp.R;
import vn.edu.hcmuaf.fit.travelapp.admin.adapter.FunctionAdapter;
import vn.edu.hcmuaf.fit.travelapp.admin.data.model.AdminFunction;
import vn.edu.hcmuaf.fit.travelapp.auth.viewmodel.UserViewModel;
import vn.edu.hcmuaf.fit.travelapp.databinding.ActivityAdminBinding;
import vn.edu.hcmuaf.fit.travelapp.product.home.adapter.PopularAdapter;

public class AdminActivity extends AppCompatActivity {
    private ActivityAdminBinding binding;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // init viewmodel
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        setupObservers();

        userViewModel.fetchCurrentUser();

        initFunctionUI();
    }

    private void setupObservers() {
        userViewModel.getUserLiveData().observe(this, user -> {
            binding.textTitle.setText("Hello, " + user.getFullName());
        });
    }

    private void initFunctionUI() {
        List<AdminFunction> functions = Arrays.asList(
                new AdminFunction("Quản lý vé", R.drawable.ic_ticket, R.color.dashboard_item_1),
                new AdminFunction("Quản lý người dùng", R.drawable.ic_user, R.color.dashboard_item_2)
        );

        binding.recyclerViewFunction.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.recyclerViewFunction.setAdapter(new FunctionAdapter(functions));
        binding.progressBarFunction.setVisibility(View.GONE);
    }
}