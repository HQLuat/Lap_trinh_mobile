package vn.edu.hcmuaf.fit.travelapp.admin.userManagement.ui;

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

import vn.edu.hcmuaf.fit.travelapp.admin.userManagement.adapter.UserAdapter;
import vn.edu.hcmuaf.fit.travelapp.admin.userManagement.data.repository.OnUserActionListener;
import vn.edu.hcmuaf.fit.travelapp.auth.data.model.User;
import vn.edu.hcmuaf.fit.travelapp.auth.viewmodel.UserViewModel;
import vn.edu.hcmuaf.fit.travelapp.databinding.ActivityUserManagementBinding;

public class UserManagementActivity extends AppCompatActivity {
    private ActivityUserManagementBinding binding;
    private UserViewModel viewModel;
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initialize ViewModel
        viewModel = new ViewModelProvider(this).get(UserViewModel.class);

        setupObservers();
        initAdapter();

        viewModel.fetchUsers();
    }

    private void setupObservers() {
        viewModel.getUsersLiveData().observe(this, users -> {
            if (users != null && !users.isEmpty()) {
                adapter.setItems(users);
            } else {
                adapter.setItems(new ArrayList<>());
                Toast.makeText(this, "Không có tài khoản nào.", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getDeleteResult().observe(this, result -> {
            if (result != null && result) {
                viewModel.fetchUsers();
            }
        });

        viewModel.getErrorLiveData().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void initAdapter() {
        adapter = new UserAdapter(new ArrayList<>(), new OnUserActionListener() {
            @Override
            public void onDeleteUser(User user) {
                new AlertDialog.Builder(UserManagementActivity.this)
                        .setTitle("Xoá người dùng")
                        .setMessage("Bạn có chắc muốn xoá tài khoản người dùng này?")
                        .setPositiveButton("Xoá", (dialog, which) -> {
                            viewModel.deleteUser(user.getUserId());
                        })
                        .setNegativeButton("Huỷ", null)
                        .show();
            }

            @Override
            public void onUpdateUser(User user) {
                Intent intent = new Intent(UserManagementActivity.this, EditUserActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        });

        binding.recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewUsers.setAdapter(adapter);
    }

    private final ActivityResultLauncher<Intent> addUserLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    viewModel.fetchUsers();
                }
            }
    );
}