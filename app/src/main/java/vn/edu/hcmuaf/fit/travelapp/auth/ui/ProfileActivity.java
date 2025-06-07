package vn.edu.hcmuaf.fit.travelapp.auth.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.text.SimpleDateFormat;
import java.util.Locale;

import vn.edu.hcmuaf.fit.travelapp.R;
import vn.edu.hcmuaf.fit.travelapp.auth.viewmodel.LogoutViewModel;
import vn.edu.hcmuaf.fit.travelapp.auth.viewmodel.UserViewModel;
import vn.edu.hcmuaf.fit.travelapp.databinding.ActivityProfileBinding;
import vn.edu.hcmuaf.fit.travelapp.product.home.ui.MenuHandler;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private UserViewModel userViewModel;
    private MenuHandler menuHandler;
    private LogoutViewModel logoutViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // init binding
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // init viewModel
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        logoutViewModel = new ViewModelProvider(this).get(LogoutViewModel.class);

        setupObserves();
        setupButtonListeners();

        userViewModel.fetchCurrentUser();

        // set up menu
        ChipNavigationBar bottomNavigation = binding.bottomNavigation;
        menuHandler = new MenuHandler(this, bottomNavigation, R.id.profile);
        menuHandler.setupMenu();
    }

    private void setupObserves() {
        userViewModel.getUserLiveData().observe(this, user -> {
            binding.tvUserName.setText(user.getFullName());

            // format date for birthday
            if (user.getDateOfBirth() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String birthdayFormatted = sdf.format(user.getDateOfBirth());
                binding.tvBirthday.setText(birthdayFormatted);
            } else {
                binding.tvBirthday.setText("Chưa cập nhật");
            }

            binding.tvGender.setText(
                    user.getGender() != null && !user.getGender().isEmpty() ? user.getGender() : "Chưa cập nhật"
            );

            binding.tvAddress.setText(
                    user.getAddress() != null && !user.getAddress().isEmpty() ? user.getAddress() : "Chưa cập nhật"
            );

            binding.tvPhone.setText(
                    user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty() ? user.getPhoneNumber() : "Chưa cập nhật"
            );

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

            String imageUrl = user.getProfileImageUrl();
            if (imageUrl == null || imageUrl.isEmpty()) {
                binding.imgAvatar.setImageResource(R.drawable.ic_avatar_profile);
            } else {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_avatar_profile)
                        .error(R.drawable.ic_avatar_profile)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(binding.imgAvatar);
            }

        });

        logoutViewModel.getLogoutSuccessLiveData().observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                Intent intent = new Intent(ProfileActivity.this, SplashAuthenticationActivity.class);
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

    private void setupButtonListeners() {
        // log out button
        binding.btnLogout.setOnClickListener(v -> {
            logoutViewModel.logout();
        });

        // back button
        binding.btnBack.setOnClickListener(v -> {
            finish();
        });

        // edit button
        binding.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            intent.putExtra("user", userViewModel.getUserLiveData().getValue());
            editProfileLauncher.launch(intent);
        });
    }

    private final ActivityResultLauncher<Intent> editProfileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    userViewModel.fetchCurrentUser();
                }
            }
    );
}