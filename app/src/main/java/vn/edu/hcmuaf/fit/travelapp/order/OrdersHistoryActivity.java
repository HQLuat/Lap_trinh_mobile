package vn.edu.hcmuaf.fit.travelapp.order;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.ArrayList;
import java.util.List;

import vn.edu.hcmuaf.fit.travelapp.R;
import vn.edu.hcmuaf.fit.travelapp.databinding.ActivityOrdersHistoryBinding;
import vn.edu.hcmuaf.fit.travelapp.home.ui.MenuHandler;
import vn.edu.hcmuaf.fit.travelapp.order.adapter.OrderHistoryAdapter;
import vn.edu.hcmuaf.fit.travelapp.order.data.repository.OrderListCallback;
import vn.edu.hcmuaf.fit.travelapp.order.data.repository.OrderRepository;
import vn.edu.hcmuaf.fit.travelapp.order.model.Order;

public class OrdersHistoryActivity extends AppCompatActivity {

    private ActivityOrdersHistoryBinding binding;
    private OrderHistoryAdapter adapter;
    private List<Order> orderList = new ArrayList<>();
    private OrderRepository orderRepository;
    private String currentUserId;
    private MenuHandler menuHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrdersHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Bottom nav
        ChipNavigationBar bottomNavigation = findViewById(R.id.bottomNavigation);
        menuHandler = new MenuHandler(this, bottomNavigation, R.id.orderHistory);
        menuHandler.setupMenu();

        // Check login
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        binding.rvOrderHistory.setLayoutManager(new LinearLayoutManager(this));

        // Adapter với listener mới
        adapter = new OrderHistoryAdapter(orderList, new OrderHistoryAdapter.OnOrderClickListener() {
            @Override
            public void onOrderClicked(Order order) {
                // TODO: Mở OrderDetailActivity
            }

            @Override
            public void onCancelOrRefundClicked(Order order) {
                Order.PaymentStatus ps = order.getPaymentStatusEnum();
                if (ps == Order.PaymentStatus.PENDING) {
                    confirmCancel(order);
                } else if (ps == Order.PaymentStatus.PAID) {
                    confirmRefund(order);
                }
            }
        });
        binding.rvOrderHistory.setAdapter(adapter);

        orderRepository = new OrderRepository();
        loadOrders();
    }

    private void loadOrders() {
        orderRepository.getOrdersWithItems(currentUserId, new OrderListCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                orderList.clear();
                if (orders != null && !orders.isEmpty()) {
                    orderList.addAll(orders);
                } else {
                    Toast.makeText(OrdersHistoryActivity.this,
                            "Chưa có lịch sử đơn hàng", Toast.LENGTH_SHORT).show();
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("OrderLoadError", "Lỗi khi tải đơn hàng", e);
                Toast.makeText(OrdersHistoryActivity.this,
                        "Lỗi tải đơn hàng: " + e.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmCancel(Order order) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy đơn")
                .setMessage("Bạn có chắc muốn hủy đơn này không?")
                .setPositiveButton("Có", (dialog, which) -> orderRepository.cancelOrderBeforePayment(
                                order.getOrderId(),
                                order.getOrderId() /* appTransId phải đúng giá trị ban đầu */)
                        .addOnSuccessListener(zpResp -> {
                            Toast.makeText(OrdersHistoryActivity.this,
                                    "Hủy đơn thành công", Toast.LENGTH_SHORT).show();
                            loadOrders();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(OrdersHistoryActivity.this,
                                    "Lỗi hủy đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }))
                .setNegativeButton("Không", null)
                .show();
    }

    private void confirmRefund(Order order) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận hoàn tiền")
                .setMessage("Bạn có chắc muốn hoàn tiền đơn này không?")
                .setPositiveButton("Có", (dialog, which) -> orderRepository.refundPaidOrder(
                                order.getOrderId(),
                                order.getOrderId() /* zpTransId thực tế */ ,
                                String.valueOf((long) order.getTotalAmount()),
                                "Khách yêu cầu hoàn")
                        .addOnSuccessListener(zpResp -> {
                            Toast.makeText(OrdersHistoryActivity.this,
                                    "Hoàn tiền thành công", Toast.LENGTH_SHORT).show();
                            loadOrders();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(OrdersHistoryActivity.this,
                                    "Lỗi hoàn tiền: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }))
                .setNegativeButton("Không", null)
                .show();
    }
}
