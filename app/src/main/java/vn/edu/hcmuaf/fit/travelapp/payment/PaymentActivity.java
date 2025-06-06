package vn.edu.hcmuaf.fit.travelapp.payment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import vn.edu.hcmuaf.fit.travelapp.R;

public class PaymentActivity extends AppCompatActivity {
    Button buyButton;
    TextView price;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);

        buyButton = findViewById(R.id.addToCartBtn);
        price = findViewById(R.id.priceTxt);
        Intent intent = getIntent();
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double tongTien = Double.parseDouble(price.getText().toString());

            }
        });
    }
}