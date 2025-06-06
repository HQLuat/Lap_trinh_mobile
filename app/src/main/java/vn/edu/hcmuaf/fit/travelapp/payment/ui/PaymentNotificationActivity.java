package vn.edu.hcmuaf.fit.travelapp.payment.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import vn.edu.hcmuaf.fit.travelapp.R;
import vn.edu.hcmuaf.fit.travelapp.databinding.ActivityPaymentBinding;
import vn.edu.hcmuaf.fit.travelapp.databinding.ActivityPaymentNotificationBinding;
import vn.edu.hcmuaf.fit.travelapp.product.home.ui.MainActivity;

public class PaymentNotificationActivity extends AppCompatActivity {
    private ActivityPaymentNotificationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        Intent intent = getIntent();
        binding.tvNotify.setText(intent.getStringExtra("result"));
        binding.btnBackToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(PaymentNotificationActivity.this, MainActivity.class);
                startActivity(intent1);
            }
        });


    }
}