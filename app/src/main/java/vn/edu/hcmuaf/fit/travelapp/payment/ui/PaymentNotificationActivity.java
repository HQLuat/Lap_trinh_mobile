package vn.edu.hcmuaf.fit.travelapp.payment.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import vn.edu.hcmuaf.fit.travelapp.databinding.ActivityPaymentNotificationBinding;
import vn.edu.hcmuaf.fit.travelapp.home.ui.MainActivity;

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