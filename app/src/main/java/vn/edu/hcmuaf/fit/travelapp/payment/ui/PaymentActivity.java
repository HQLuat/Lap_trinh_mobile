package vn.edu.hcmuaf.fit.travelapp.payment.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import vn.edu.hcmuaf.fit.travelapp.databinding.ActivityPaymentBinding;
import vn.edu.hcmuaf.fit.travelapp.order.data.repository.OrderCreationCallback;
import vn.edu.hcmuaf.fit.travelapp.order.data.repository.OrderRepository;
import vn.edu.hcmuaf.fit.travelapp.order.model.Order;
import vn.edu.hcmuaf.fit.travelapp.order.model.OrderItem;
import vn.edu.hcmuaf.fit.travelapp.payment.Api.CreateOrder;
import vn.edu.hcmuaf.fit.travelapp.admin.productManagement.data.model.Product;
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class PaymentActivity extends AppCompatActivity {
    private ActivityPaymentBinding binding;
    private Product product;
    private OrderRepository orderRepo;
    String totalString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orderRepo = new OrderRepository();  // Khởi tạo repo Firebase
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

        binding.addToCartBtn.setOnClickListener(v -> createOrderAndPay());
    }

    private void createOrderAndPay() {
        // 1. Chuẩn bị OrderItem list
        List<OrderItem> items = new ArrayList<>();
        OrderItem item = new OrderItem();
        // Chỉ set các trường cần cho Firestore; itemId sẽ được set trong OrderRepo
        item.setProductId(product.getProductId());
        item.setQuantity(1);
        item.setUnitPrice(product.getPrice());
        item.setTourDate(Timestamp.now());
        item.setGuestName(
                FirebaseAuth.getInstance()
                        .getCurrentUser()
                        .getDisplayName()
        );
        items.add(item);

        // 2. Thông tin order
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        double totalAmount = product.getPrice();
        String paymentMethod = "ZaloPay";
        Timestamp departureDate = Timestamp.now();
        String imageUrl = product.getImageUrl();
        String destinationString = product.getName();

        // 3. Tạo order trên Firestore
        orderRepo.createOrderWithItems(
                userId,
                imageUrl,
                totalAmount,
                paymentMethod,
                departureDate,
                destinationString,
                items,
                new OrderCreationCallback() {
                    @Override
                    public void onSuccess(String orderId) {
                        Log.d("PaymentActivity", "Order created: " + orderId);
                        // 4. Gọi ZaloPay khi đã tạo xong order
                        payWithZaloPay(orderId);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("PaymentActivity", "Failed to create order", e);
                        // Có thể show Toast báo lỗi tạo order
                    }
                }
        );
    }

    private void payWithZaloPay(String orderId) {
        try {
            CreateOrder orderApi = new CreateOrder();
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
                        startActivity(intent1);

                        // 5. Cập nhật trạng thái order: dùng enum từ model
                        orderRepo.updatePaymentStatus(orderId,
                                        Order.PaymentStatus.PAID,
                                        Order.OrderStatus.CONFIRMED
                                ).addOnSuccessListener(unused ->
                                        Log.d("PaymentActivity", "Order status updated to PAID/CONFIRMED"))
                                .addOnFailureListener(e ->
                                        Log.e("PaymentActivity", "Failed to update status after success", e)
                                );

                        navigateToResult("Thanh toán thành công");
                    }

                    @Override
                    public void onPaymentCanceled(String s, String s1) {
                        Log.d("ZaloPayDebug", "Payment Canceled: " + s + " | " + s1);
                        Intent intent1 = new Intent(PaymentActivity.this, PaymentNotificationActivity.class);
                        intent1.putExtra("result","Huỷ thanh toán");
                        startActivity(intent1);

                        // Cập nhật trạng thái thất bại/hủy
                        orderRepo.updatePaymentStatus(orderId,
                                        Order.PaymentStatus.CANCELED,
                                        Order.OrderStatus.CANCELED
                                ).addOnSuccessListener(unused ->
                                        Log.d("PaymentActivity", "Order status updated to CANCELED"))
                                .addOnFailureListener(e ->
                                        Log.e("PaymentActivity", "Failed to update status after cancel", e)
                                );

                        navigateToResult("Huỷ thanh toán");
                    }

                    @Override
                    public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
                        Log.e("ZaloPayDebug", "Payment Error: " + zaloPayError.toString() + " | " + s + " | " + s1);
                        Intent intent1 = new Intent(PaymentActivity.this, PaymentNotificationActivity.class);
                        intent1.putExtra("result","Lỗi thanh toán");
                        startActivity(intent1);

                        orderRepo.updatePaymentStatus(orderId,
                                        Order.PaymentStatus.FAILED,
                                        Order.OrderStatus.CANCELED
                                ).addOnSuccessListener(unused ->
                                        Log.d("PaymentActivity", "Order status updated to FAILED"))
                                .addOnFailureListener(e ->
                                        Log.e("PaymentActivity", "Failed to update status after error", e)
                                );

                        navigateToResult("Lỗi thanh toán");
                    }
                });
            }else {
                Log.e("ZaloPayDebug", "Order creation failed: return_code = " + code);
            }

        } catch (Exception e) {
            Log.e("ZaloPayDebug", "Exception during order creation: ", e);
        }
    }

    private void navigateToResult(String result) {
        Intent intent = new Intent(this, PaymentNotificationActivity.class);
        intent.putExtra("result", result);
        startActivity(intent);
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