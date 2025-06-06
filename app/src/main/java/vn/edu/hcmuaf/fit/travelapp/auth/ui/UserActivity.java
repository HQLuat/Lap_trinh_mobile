package vn.edu.hcmuaf.fit.travelapp.auth.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Locale;

import vn.edu.hcmuaf.fit.travelapp.auth.viewmodel.UserViewModel;
import vn.edu.hcmuaf.fit.travelapp.databinding.ActivityUserBinding;

public class UserActivity extends AppCompatActivity {
    private ActivityUserBinding binding;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // init binding
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // init viewModel
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        setupObserves();
        setupButtonListeners();

        userViewModel.fetchCurrentUser();
    }

    private void setupObserves() {
        userViewModel.getUserLiveData().observe(this, user -> {
            binding.tvUserName.setText(user.getFullName());

            // format date for birthday
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String birthdayFormatted = sdf.format(user.getDateOfBirth());
            binding.tvBirthday.setText(birthdayFormatted);

            binding.tvGender.setText(user.getGender());
            binding.tvAddress.setText(user.getAddress());
            binding.tvPhone.setText(user.getPhoneNumber());

            String roleDisplay;
            switch (user.getRole()) {
                case 0:
                    roleDisplay = "Quản trị viên";
                    break;
                case 1:
                    roleDisplay = "Nhân viên";
                    break;
                case 2:
                    roleDisplay = "Khách hàng";
                    break;
                default:
                    roleDisplay = "Không xác định";
            }
            binding.tvRole.setText(roleDisplay);
            binding.tvEmail.setText(user.getEmail());
        });
    }

    private void setupButtonListeners() {
        // event handler for the logout button
        binding.btnLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
        });
    }
}