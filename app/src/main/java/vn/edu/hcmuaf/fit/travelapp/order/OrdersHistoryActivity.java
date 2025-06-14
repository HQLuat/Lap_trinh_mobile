package vn.edu.hcmuaf.fit.travelapp.order;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.List;

import vn.edu.hcmuaf.fit.travelapp.R;
import vn.edu.hcmuaf.fit.travelapp.databinding.ActivityOrdersHistoryBinding;
import vn.edu.hcmuaf.fit.travelapp.home.ui.MenuHandler;
import vn.edu.hcmuaf.fit.travelapp.order.adapter.OrderHistoryAdapter;
import vn.edu.hcmuaf.fit.travelapp.order.model.Order;

public class OrdersHistoryActivity extends AppCompatActivity implements OrdersHistoryLogic.Callback {

    private static final String TAG = "OrdersHistoryActivity";

    private ActivityOrdersHistoryBinding binding;
    private OrderHistoryAdapter adapter;
    private OrdersHistoryLogic logic;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrdersHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Log.d(TAG, "onCreate: Bắt đầu OrdersHistoryActivity");

        // Bottom nav
        ChipNavigationBar bottomNavigation = findViewById(R.id.bottomNavigation);
        MenuHandler menuHandler = new MenuHandler(this, bottomNavigation, R.id.orderHistory);
        menuHandler.setupMenu();

        // Check login
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử đơn hàng", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "onCreate: Người dùng chưa đăng nhập → thoát activity");
            finish();
            return;
        }
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "onCreate: currentUserId = " + currentUserId);

        // RecyclerView + Adapter (ListAdapter)
        binding.rvOrderHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderHistoryAdapter(new OrderHistoryAdapter.OnOrderClickListener() {
            @Override
            public void onOrderClicked(Order order) {
                Log.d(TAG, "onOrderClicked: " + order.getOrderId());
                // TODO: Mở OrderDetailActivity hoặc chuyển sang Fragment
            }

            @Override
            public void onCancelOrRefundClicked(Order order) {
                Log.d(TAG, "onCancelOrRefundClicked: " + order.getOrderId() + " - status = " + order.getPaymentStatusEnum());
                Order.PaymentStatus ps = order.getPaymentStatusEnum();
                if (ps == Order.PaymentStatus.PENDING || ps == Order.PaymentStatus.FAILED) {
                    confirmCancel(order);
                } else if (ps == Order.PaymentStatus.PAID) {
                    confirmRefund(order);
                }
            }
        });
        binding.rvOrderHistory.setAdapter(adapter);

        // Logic handler
        logic = new OrdersHistoryLogic(this);

        loadOrders();
    }

    private void loadOrders() {
        Log.d(TAG, "loadOrders: Đang tải danh sách đơn hàng cho user " + currentUserId);
        logic.loadOrders(currentUserId);
    }

    private void confirmCancel(Order order) {
        Log.d(TAG, "confirmCancel: Xác nhận hủy đơn " + order.getOrderId());
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy đơn")
                .setMessage("Bạn có chắc muốn hủy đơn này không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    Log.d(TAG, "confirmCancel: Đã xác nhận hủy → gọi logic.cancelOrderBeforePayment()");
                    logic.cancelOrderBeforePayment(order.getOrderId(), order.getAppTransId());
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void confirmRefund(Order order) {
        Log.d(TAG, "confirmRefund: Xác nhận hoàn tiền cho đơn " + order.getOrderId());
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận hoàn tiền")
                .setMessage("Bạn có chắc muốn hoàn tiền đơn này không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    Log.d(TAG, "confirmRefund: Đã xác nhận hoàn → gọi logic.refundPaidOrder()");
                    logic.refundPaidOrder(order.getOrderId(), order.getZpTransId(),
                            String.valueOf((long) order.getTotalAmount()));
                })
                .setNegativeButton("Không", null)
                .show();
    }

    // ===== Callback từ logic =====
    @Override
    public void onOrdersLoaded(List<Order> orders) {
        Log.d(TAG, "onOrdersLoaded: " + (orders != null ? orders.size() + " đơn hàng" : "null"));
        if (orders != null && !orders.isEmpty()) {
            adapter.submitList(orders);
        } else {
            adapter.submitList(null);
            Toast.makeText(this, "Chưa có lịch sử đơn hàng", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onError(String message) {
        Log.e(TAG, "onError: " + message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancelSuccess() {
        Log.d(TAG, "onCancelSuccess: Hủy đơn thành công");
        Toast.makeText(this, "Hủy đơn thành công", Toast.LENGTH_SHORT).show();
        loadOrders();
    }

    @Override
    public void onRefundSuccess() {
        Log.d(TAG, "onRefundSuccess: Hoàn tiền thành công");
        Toast.makeText(this, "Hoàn tiền thành công", Toast.LENGTH_SHORT).show();
        loadOrders();
    }
}
