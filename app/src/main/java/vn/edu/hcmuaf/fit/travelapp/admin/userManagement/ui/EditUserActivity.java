package vn.edu.hcmuaf.fit.travelapp.admin.userManagement.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.Arrays;

import vn.edu.hcmuaf.fit.travelapp.R;
import vn.edu.hcmuaf.fit.travelapp.auth.data.model.User;
import vn.edu.hcmuaf.fit.travelapp.auth.viewmodel.UserViewModel;
import vn.edu.hcmuaf.fit.travelapp.databinding.ActivityEditUserBinding;

public class EditUserActivity extends AppCompatActivity {
    private ActivityEditUserBinding binding;
    private UserViewModel viewModel;
    private User user;
    String[] roles = {"Admin", "Manager", "Customer"};
    String[] statuses = {"Active", "InActive"};
    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapterItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // initialize ViewModel
        viewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // get user
        user = getIntent().getParcelableExtra("user");

        setupObservers();
        setupEventListeners();
        setupDropdownOptions();

        if (user != null)  viewModel.fetchUser(user.getUserId());
    }

    private void setupObservers() {
        viewModel.getSaveSuccessLiveData().observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void setupEventListeners() {
        binding.btnUpdateUser.setOnClickListener(v -> {

            // Get value of autoCompleteTextView
            String selectedRole = binding.autoCompleteRoleTxt.getText().toString();
            String selectedStatus = binding.autoCompleteStatusTxt.getText().toString();
            Log.d("selectedStatus", "[" + selectedStatus + "]");

            // convert role from string to int
            int roleIndex = Arrays.asList(roles).indexOf(selectedRole);
            if (roleIndex != -1) {
                user.setRole(roleIndex);
            }

            // convert status from string to boolean
            boolean isActive = "Active".equalsIgnoreCase(selectedStatus);
            user.setActive(isActive);
            boolean active = user.isActive();
            Log.d("user active", String.valueOf(active));

            viewModel.updateUser(user, null);
        });
    }

    private void setupDropdownOptions() {
        // set value into autoCompleteTextView
        String roleText = roles[user.getRole()];
        binding.autoCompleteRoleTxt.setText(roleText, false);
        String statusText = user.isActive() ? "Active" : "InActive";
        binding.autoCompleteStatusTxt.setText(statusText, false);

        // set roles dropdown
        autoCompleteTextView = binding.autoCompleteRoleTxt;
        adapterItems = new ArrayAdapter<String>(this, R.layout.viewholder_selector_item, roles);
        autoCompleteTextView.setAdapter(adapterItems);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
            }
        });

        // set statuses dropdown
        autoCompleteTextView = binding.autoCompleteStatusTxt;
        adapterItems = new ArrayAdapter<String>(this, R.layout.viewholder_selector_item, statuses);
        autoCompleteTextView.setAdapter(adapterItems);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
            }
        });
    }
}