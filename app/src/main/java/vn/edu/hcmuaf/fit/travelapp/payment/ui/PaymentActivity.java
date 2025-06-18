package vn.edu.hcmuaf.fit.travelapp.payment.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import vn.edu.hcmuaf.fit.travelapp.databinding.ActivityPaymentBinding;
import vn.edu.hcmuaf.fit.travelapp.order.data.repository.OrderRepository;
import vn.edu.hcmuaf.fit.travelapp.order.data.repository.OrderRepository.*;
import vn.edu.hcmuaf.fit.travelapp.order.model.Order;
import vn.edu.hcmuaf.fit.travelapp.order.model.OrderItem;
import vn.edu.hcmuaf.fit.travelapp.payment.Api.CreateOrder;
import vn.edu.hcmuaf.fit.travelapp.admin.productManagement.data.model.Product;
import vn.edu.hcmuaf.fit.travelapp.payment.Helper.Helpers;
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

        // Sinh appTransId đúng định dạng
        String appTransId = Helpers.getAppTransId();

        // 3. Tạo order trên Firestore
        orderRepo.createOrderWithItems(
                userId,
                imageUrl,
                totalAmount,
                paymentMethod,
                departureDate,
                destinationString,
                items,
                appTransId,
                new OrderCreationCallback() {
                    @Override
                    public void onSuccess(String orderId) {
                        Log.d("PaymentActivity", "Order created: " + orderId);
                        // 4. Gọi ZaloPay khi đã tạo xong order
                        payWithZaloPay(orderId, appTransId);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("PaymentActivity", "Failed to create order", e);
                        Toast.makeText(PaymentActivity.this, "Không thể tạo đơn hàng (zp). Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void payWithZaloPay(String orderId, String appTransId) {
        try {
            CreateOrder orderApi = new CreateOrder();
            JSONObject data = orderApi.createOrder(totalString, appTransId);
            Log.d("ZaloPayDebug", "createOrder response: " + data.toString());
            String code = data.getString("return_code");

            if (code.equals("1")) {
                String token = data.getString("zp_trans_token");
                String paymentUrl = data.optString("order_url");
                Log.d("ZaloPayDebug", "Token: " + token);

                // 🔄 Cập nhật Firestore với zpOrderId và paymentUrl
                orderRepo.updateZaloPayInfo(orderId, token, paymentUrl)
                        .addOnSuccessListener(unused -> Log.d("ZaloPay", "Order info updated"))
                        .addOnFailureListener(e -> Log.e("ZaloPay", "Failed to update Firestore", e));

                ZaloPaySDK.getInstance().payOrder(PaymentActivity.this, token, "demozpdk://app", new PayOrderListener() {
                    @Override
                    public void onPaymentSucceeded(String transactionId, String transToken, String appTransID) {
                        Log.d("ZaloPayDebug", "Payment Success: " + transactionId + " | " + transToken + " | " + appTransID);

                        // ✅ Gọi hàm để set zpTransId vào đơn hàng trong DB
                        orderRepo.updateZaloTransId(orderId, transactionId)  // transactionId = zp_trans_id
                                .addOnSuccessListener(unused -> Log.d("PaymentActivity", "Saved zp_trans_id"))
                                .addOnFailureListener(e -> Log.e("PaymentActivity", "Failed to save zp_trans_id", e));

                        // Cập nhật trạng thái thanh toán: PAID, order sang PAID hoặc CONFIRMED
                        orderRepo.updatePaymentStatus(orderId,
                                        Order.PaymentStatus.PAID,
                                        Order.OrderStatus.CONFIRMED)  // hoặc OrderStatus.PAID nếu tách bước xác nhận
                                .addOnSuccessListener(unused ->
                                        Log.d("PaymentActivity", "Order status updated to PAID/CONFIRMED"))
                                .addOnFailureListener(e ->
                                        Log.e("PaymentActivity", "Failed to update status after success", e));

                        navigateToResult("Thanh toán thành công");
                    }


                    @Override
                    public void onPaymentCanceled(String s, String s1) {
                        Log.d("ZaloPayDebug", "Payment Canceled: " + s + " | " + s1);

                        // Hủy trước thanh toán: CANCELED
                        orderRepo.updatePaymentStatus(orderId,
                                        Order.PaymentStatus.CANCELED,
                                        Order.OrderStatus.CANCELED)
                                .addOnSuccessListener(unused ->
                                        Log.d("PaymentActivity", "Order status updated to CANCELED"))
                                .addOnFailureListener(e ->
                                        Log.e("PaymentActivity", "Failed to update status after cancel", e));

                        navigateToResult("Huỷ thanh toán");
                    }

                    @Override
                    public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
                        Log.e("ZaloPayDebug", "Payment Error: " + zaloPayError.toString() + " | " + s + " | " + s1);

                        // Payment error: đánh dấu FAILED, nhưng vẫn cho retry -> orderStatus = WAITING_PAYMENT
                        orderRepo.updatePaymentStatus(orderId,
                                        Order.PaymentStatus.FAILED,
                                        Order.OrderStatus.WAITING_PAYMENT)
                                .addOnSuccessListener(unused ->
                                        Log.d("PaymentActivity", "Order status updated to FAILED/WARNING_PAYMENT"))
                                .addOnFailureListener(e ->
                                        Log.e("PaymentActivity", "Failed to update status after error", e));

                        navigateToResult("Lỗi thanh toán");
                    }
                });
            } else {
                Log.e("ZaloPayDebug", "Order creation failed: return_code = " + code);

                // Nếu tạo ZaloPay order thất bại: FAILED, nhưng cho retry -> status vẫn WAITING_PAYMENT
                orderRepo.updatePaymentStatus(orderId,
                                Order.PaymentStatus.FAILED,
                                Order.OrderStatus.WAITING_PAYMENT)
                        .addOnSuccessListener(unused ->
                                Log.d("PaymentActivity", "Order marked FAILED, chờ retry"))
                        .addOnFailureListener(error ->
                                Log.e("PaymentActivity", "Failed to update status after return_code error", error));
                Toast.makeText(this, "Không thể tạo đơn thanh toán. Vui lòng thử lại sau.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("ZaloPayDebug", "Exception during order creation: ", e);
            Toast.makeText(this, "Lỗi khi tạo đơn ZaloPay. Vui lòng thử lại sau.", Toast.LENGTH_SHORT).show();

            // Đánh dấu lỗi tạo order: vẫn để chờ retry
            orderRepo.updatePaymentStatus(orderId,
                            Order.PaymentStatus.FAILED,
                            Order.OrderStatus.WAITING_PAYMENT)
                    .addOnSuccessListener(unused ->
                            Log.d("PaymentActivity", "Order marked FAILED due to exception"))
                    .addOnFailureListener(error ->
                            Log.e("PaymentActivity", "Failed to update status after exception", error));
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