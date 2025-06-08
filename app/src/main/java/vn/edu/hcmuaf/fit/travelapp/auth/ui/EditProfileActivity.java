package vn.edu.hcmuaf.fit.travelapp.auth.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import vn.edu.hcmuaf.fit.travelapp.R;
import vn.edu.hcmuaf.fit.travelapp.auth.data.model.User;
import vn.edu.hcmuaf.fit.travelapp.auth.viewmodel.UserViewModel;
import vn.edu.hcmuaf.fit.travelapp.databinding.ActivityEditProfileBinding;

public class EditProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private Uri imageUri;
    private ActivityEditProfileBinding binding;
    private UserViewModel userViewModel;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        loadUserInfo();
        setupObservers();
        setupListeners();
    }

    private void init() {
        userViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(UserViewModel.class);
    }

    private void loadUserInfo() {
        currentUser = (User) getIntent().getParcelableExtra("user");
        if (currentUser != null) {
            populateForm(currentUser);
        }
    }

    private void populateForm(User user) {
        binding.etFullName.setText(user.getFullName() != null ? user.getFullName() : "");
        binding.etPhone.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        binding.etEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        binding.etAddress.setText(user.getAddress() != null ? user.getAddress() : "");
        binding.etGender.setText(user.getGender() != null ? user.getGender() : "");

        if (user.getDateOfBirth() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String dobFormatted = sdf.format(user.getDateOfBirth());
            binding.etDob.setText(dobFormatted);
        } else {
            binding.etDob.setText("");
        }

        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(user.getProfileImageUrl())
                    .placeholder(R.drawable.ic_avatar_profile)
                    .error(R.drawable.ic_avatar_profile)
                    .into(binding.ivProfileImage);
        } else {
            binding.ivProfileImage.setImageResource(R.drawable.ic_avatar_profile);
        }
    }

    private void setupObservers() {
        userViewModel.getSaveSuccessLiveData().observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                setResult(RESULT_OK);
                finish();
            }
        });
        userViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        binding.btnUpdateUser.setOnClickListener(v -> onSaveProfileClick());
        binding.ivProfileImage.setOnClickListener(v -> onSelectImageClick());
    }

    private void onSaveProfileClick() {
        String fullName = binding.etFullName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();
        String gender = binding.etGender.getText().toString().trim();
        String dobString = binding.etDob.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.setFullName(fullName);
        currentUser.setEmail(email);
        currentUser.setPhoneNumber(phone.isEmpty() ? null : phone);
        currentUser.setAddress(address.isEmpty() ? null : address);
        currentUser.setGender(gender.isEmpty() ? null : gender);

        if (!dobString.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date dobDate = sdf.parse(dobString);
                currentUser.setDateOfBirth(dobDate);
            } catch (ParseException e) {
                Toast.makeText(this, "Ngày sinh không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            currentUser.setDateOfBirth(null);
        }

        userViewModel.updateUser(currentUser, imageUri);
    }


    private void onSelectImageClick() {
        if (checkSelfPermission()) {
            openImagePicker();
        } else {
            requestStoragePermission();
        }
    }

    private boolean checkSelfPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

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

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    binding.ivProfileImage.setImageURI(imageUri);
                }
            }
    );
}