package vn.edu.hcmuaf.fit.travelapp.auth.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import vn.edu.hcmuaf.fit.travelapp.auth.viewmodel.SignupViewModel;
import vn.edu.hcmuaf.fit.travelapp.databinding.ActivitySignupBinding;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private final SignupViewModel signupViewModel = new SignupViewModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.signupBtn.setOnClickListener(v -> {
            String name = binding.nameEdt.getText().toString().trim();
            String email = binding.gmailEdt.getText().toString().trim();
            String password = binding.paswordEdt.getText().toString().trim();
            String repass = binding.rePaswordEdt.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                binding.nameEdt.setError("Name is required");
                binding.nameEdt.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(email)) {
                binding.gmailEdt.setError("Email is required");
                binding.gmailEdt.requestFocus();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.gmailEdt.setError("Invalid email address");
                binding.gmailEdt.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                binding.paswordEdt.setError("Password is required");
                binding.paswordEdt.requestFocus();
                return;
            }

            if (password.length() < 6) {
                binding.paswordEdt.setError("Password must be at least 6 characters");
                binding.paswordEdt.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(repass)) {
                binding.rePaswordEdt.setError("Please confirm your password");
                binding.rePaswordEdt.requestFocus();
                return;
            }

            if (!password.equals(repass)) {
                binding.rePaswordEdt.setError("Passwords do not match");
                binding.rePaswordEdt.requestFocus();
                return;
            }


            signupViewModel.register(name, email, password);

        });

        // Observe success user data
        signupViewModel.getUserLiveData().observe(this, firebaseUser -> {
            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
            finish(); // close activity
        });

        // Observe errors
        signupViewModel.getErrorLiveData().observe(this, errorMessage -> {
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        });

    }
}
