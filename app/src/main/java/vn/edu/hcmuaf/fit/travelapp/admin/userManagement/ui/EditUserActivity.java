package vn.edu.hcmuaf.fit.travelapp.admin.userManagement.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import vn.edu.hcmuaf.fit.travelapp.auth.data.model.User;
import vn.edu.hcmuaf.fit.travelapp.auth.viewmodel.UserViewModel;
import vn.edu.hcmuaf.fit.travelapp.databinding.ActivityEditUserBinding;

public class EditUserActivity extends AppCompatActivity {
    private ActivityEditUserBinding binding;
    private UserViewModel viewModel;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initialize ViewModel
        viewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // get user
        user = getIntent().getParcelableExtra("user");

        binding.btnUpdateUser.setEnabled(false);

        setupObservers();
        setupEventListeners();
        setupDropdownOptions();

        if (user != null)  viewModel.fetchUser(user.getUserId());
    }

    private void setupObservers() {
        viewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                int role = user.getRole();
                String roleText = "";

                switch (role) {
                    case 0: roleText = "Admin"; break;
                    case 1: roleText = "Manager"; break;
                    case 2: roleText = "Customer"; break;
                }

                binding.etUserRole.setText(roleText);
                binding.etUserActive.setText(user.isActive() ? "Active" : "Inactive");
                binding.btnUpdateUser.setEnabled(true);
            }
        });
        viewModel.getSaveSuccessLiveData().observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void setupEventListeners() {
        binding.btnUpdateUser.setOnClickListener(v -> {
            Log.d("EditUserActivity", "Update button clicked");
            String selectedRole = binding.etUserRole.getText().toString();
            String selectedStatus = binding.etUserActive.getText().toString();

            int roleInt = 2;
            if (selectedRole.equals("Admin")) roleInt = 0;
            else if (selectedRole.equals("Manager")) roleInt = 1;

            user.setRole(roleInt);
            user.setActive("Active".equals(selectedStatus));

            viewModel.updateUser(user, null);
        });
    }

    private void setupDropdownOptions() {
        String[] roles = {"Admin", "Manager", "Customer"};
        String[] statuses = {"Active", "Inactive"};

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roles);
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, statuses);

        binding.etUserRole.setAdapter(roleAdapter);
        binding.etUserActive.setAdapter(statusAdapter);
    }

}