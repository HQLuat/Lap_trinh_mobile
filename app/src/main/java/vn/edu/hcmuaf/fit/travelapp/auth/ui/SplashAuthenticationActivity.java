package vn.edu.hcmuaf.fit.travelapp.auth.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import vn.edu.hcmuaf.fit.travelapp.R;
import vn.edu.hcmuaf.fit.travelapp.databinding.ActivitySplashAuthenticationBinding;

public class SplashAuthenticationActivity extends AppCompatActivity {

    private ActivitySplashAuthenticationBinding binding ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashAuthenticationBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        binding.loginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(SplashAuthenticationActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        binding.signupBtn.setOnClickListener(v -> {
            Intent intent = new Intent(SplashAuthenticationActivity.this, SignupActivity.class);
            startActivity(intent);
        });

    }
}