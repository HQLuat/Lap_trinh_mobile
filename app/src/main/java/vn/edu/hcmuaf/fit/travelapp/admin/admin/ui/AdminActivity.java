package vn.edu.hcmuaf.fit.travelapp.admin.admin.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Arrays;
import java.util.List;

import vn.edu.hcmuaf.fit.travelapp.R;
import vn.edu.hcmuaf.fit.travelapp.admin.admin.adapter.FunctionAdapter;
import vn.edu.hcmuaf.fit.travelapp.admin.admin.data.model.AdminFunction;
import vn.edu.hcmuaf.fit.travelapp.admin.admin.data.model.Functions;
import vn.edu.hcmuaf.fit.travelapp.auth.data.model.User;
import vn.edu.hcmuaf.fit.travelapp.auth.ui.ProfileActivity;
import vn.edu.hcmuaf.fit.travelapp.auth.ui.SplashAuthenticationActivity;
import vn.edu.hcmuaf.fit.travelapp.auth.viewmodel.LogoutViewModel;
import vn.edu.hcmuaf.fit.travelapp.auth.viewmodel.UserViewModel;
import vn.edu.hcmuaf.fit.travelapp.databinding.ActivityAdminBinding;

public class AdminActivity extends AppCompatActivity {
    private ActivityAdminBinding binding;
    private UserViewModel userViewModel;
    private LogoutViewModel logoutViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // init viewmodel
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        logoutViewModel = new ViewModelProvider(this).get(LogoutViewModel.class);

        setupObservers();
        initFunctionUI();
        setupEventListeners();

        userViewModel.fetchCurrentUser();
    }

    private void setupObservers() {

        // user viewmodel
        userViewModel.getUserLiveData().observe(this, user -> {
            binding.textTitle.setText("Hello, " + user.getFullName());
        });
        userViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // logout viewmodel
        logoutViewModel.getLogoutSuccessLiveData().observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                Intent intent = new Intent(AdminActivity.this, SplashAuthenticationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
        logoutViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initFunctionUI() {
        List<AdminFunction> functions = Arrays.asList(
                new AdminFunction(Functions.TICKET_MANAGEMENT, R.drawable.ic_ticket, R.color.dashboard_item_1),
                new AdminFunction(Functions.USER_MANAGEMENT, R.drawable.ic_user, R.color.dashboard_item_2)
        );

        binding.recyclerViewFunction.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.recyclerViewFunction.setAdapter(new FunctionAdapter(functions));
        binding.progressBarFunction.setVisibility(View.GONE);
    }

    private void setupEventListeners() {
        binding.btnLogout.setOnClickListener(v -> {
            logoutViewModel.logout();
        });
    }
}