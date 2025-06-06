package vn.edu.hcmuaf.fit.travelapp.auth.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import vn.edu.hcmuaf.fit.travelapp.auth.viewmodel.LoginViewModel;
import vn.edu.hcmuaf.fit.travelapp.databinding.ActivityLoginBinding;
import vn.edu.hcmuaf.fit.travelapp.product.home.ui.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        binding.loginBtn.setOnClickListener(v -> {
            String email = binding.gmailEdt.getText().toString().trim();
            String password = binding.paswordEdt.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                binding.gmailEdt.setError("Email is required");
                binding.gmailEdt.requestFocus();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.gmailEdt.setError("Invalid email format");
                binding.gmailEdt.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                binding.paswordEdt.setError("Password is required");
                binding.paswordEdt.requestFocus();
                return;
            }

            loginViewModel.login(email, password);
        });

        loginViewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        loginViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
