package vn.edu.hcmuaf.fit.travelapp.payment.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import vn.edu.hcmuaf.fit.travelapp.R;
import vn.edu.hcmuaf.fit.travelapp.databinding.ActivityDetailBinding;
import vn.edu.hcmuaf.fit.travelapp.databinding.ActivityPaymentBinding;
import vn.edu.hcmuaf.fit.travelapp.payment.Api.CreateOrder;
import vn.edu.hcmuaf.fit.travelapp.product.home.ui.DetailActivity;
import vn.edu.hcmuaf.fit.travelapp.product.productManagement.data.model.Product;
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class PaymentActivity extends AppCompatActivity {
    Button buyButton;
    TextView price;
    private ActivityPaymentBinding binding;
    private Product product;
    String totalString;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // ZaloPay SDK Init
        ZaloPaySDK.init(2553, Environment.SANDBOX);



        getIntentExtra();
        setVariable();

        double totalPrice = product.getPrice();
        String quantity = "1";
        totalString = String.format("%.0f", totalPrice);


    }
    private void setVariable() {
        binding.titleTxt.setText(product.getName());
        binding.priceTxt.setText(product.getPrice() + "đ");
        binding.backBtn.setOnClickListener(v -> finish());
        binding.bedTxt.setText("2");
        binding.durationTxt.setText("2 ngay 1 dem");
        binding.distanceTxt.setText("5");
        binding.descriptionTxt.setText(product.getDescription());
        binding.addressTxt.setText(product.getAddress());
        binding.ratingBar.setRating(4.5f);
        binding.ratingTxt.setText("4.5 Rating");

        Glide.with(PaymentActivity.this)
                .load(product.getImageUrl())
                .into(binding.pic);

        binding.addToCartBtn.setOnClickListener(view -> {
            CreateOrder orderApi = new CreateOrder();

            try {
                JSONObject data = orderApi.createOrder(totalString);
                Log.d("ZaloPayDebug", "createOrder response: " + data.toString());
                String code = data.getString("return_code");

                if (code.equals("1")) {
                    String token = data.getString("zp_trans_token");
                    Log.d("ZaloPayDebug", "Token: " + token);
                    ZaloPaySDK.getInstance().payOrder(PaymentActivity.this, token, "demozpdk://app", new PayOrderListener() {
                        @Override
                        public void onPaymentSucceeded(String s, String s1, String s2) {
                            Log.d("ZaloPayDebug", "Payment Success: " + s + " | " + s1 + " | " + s2);
                            Intent intent1 = new Intent(PaymentActivity.this, PaymentNotificationActivity.class);
                            intent1.putExtra("result","Thanh toán thành công");
                        }

                        @Override
                        public void onPaymentCanceled(String s, String s1) {
                            Log.d("ZaloPayDebug", "Payment Canceled: " + s + " | " + s1);
                            Intent intent1 = new Intent(PaymentActivity.this, PaymentNotificationActivity.class);
                            intent1.putExtra("result","Huỷ thanh toán");
                        }

                        @Override
                        public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
                            Log.e("ZaloPayDebug", "Payment Error: " + zaloPayError.toString() + " | " + s + " | " + s1);
                            Intent intent1 = new Intent(PaymentActivity.this, PaymentNotificationActivity.class);
                            intent1.putExtra("result","Lỗi thanh toán");
                        }
                    });
                }else {
                    Log.e("ZaloPayDebug", "Order creation failed: return_code = " + code);
                }

            } catch (Exception e) {
                Log.e("ZaloPayDebug", "Exception during order creation: ", e);
            }


        });
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }
    private void getIntentExtra() {
        product = getIntent().getParcelableExtra("object");
    }
}