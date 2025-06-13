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

        private ActivityOrdersHistoryBinding binding;
        private OrderHistoryAdapter adapter;
        private OrdersHistoryLogic logic;
        private String currentUserId;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            binding = ActivityOrdersHistoryBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            // Bottom nav
            ChipNavigationBar bottomNavigation = findViewById(R.id.bottomNavigation);
            MenuHandler menuHandler = new MenuHandler(this, bottomNavigation, R.id.orderHistory);
            menuHandler.setupMenu();

            // Check login
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử đơn hàng", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // RecyclerView + Adapter (ListAdapter)
            binding.rvOrderHistory.setLayoutManager(new LinearLayoutManager(this));
            adapter = new OrderHistoryAdapter(new OrderHistoryAdapter.OnOrderClickListener() {
                @Override
                public void onOrderClicked(Order order) {
                    // TODO: Mở OrderDetailActivity hoặc chuyển sang Fragment
                }
                @Override
                public void onCancelOrRefundClicked(Order order) {
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
            logic.loadOrders(currentUserId);
        }

        private void confirmCancel(Order order) {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận hủy đơn")
                    .setMessage("Bạn có chắc muốn hủy đơn này không?")
                    .setPositiveButton("Có", (dialog, which) ->
                            logic.cancelOrderBeforePayment(order.getOrderId(), order.getAppTransId())
                    )
                    .setNegativeButton("Không", null)
                    .show();
        }

        private void confirmRefund(Order order) {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận hoàn tiền")
                    .setMessage("Bạn có chắc muốn hoàn tiền đơn này không?")
                    .setPositiveButton("Có", (dialog, which) ->
                            logic.refundPaidOrder(order.getOrderId(), order.getZpTransId(),
                                    String.valueOf((long) order.getTotalAmount()))
                    )
                    .setNegativeButton("Không", null)
                    .show();
        }

        // Callback từ logic
        @Override
        public void onOrdersLoaded(List<Order> orders) {
            if (orders != null && !orders.isEmpty()) {
                adapter.submitList(orders);
            } else {
                adapter.submitList(null);
                Toast.makeText(this, "Chưa có lịch sử đơn hàng", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onError(String message) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            Log.e("OrdersHistoryActivity", message);
        }

        @Override
        public void onCancelSuccess() {
            Toast.makeText(this, "Hủy đơn thành công", Toast.LENGTH_SHORT).show();
            loadOrders();
        }

        @Override
        public void onRefundSuccess() {
            Toast.makeText(this, "Hoàn tiền thành công", Toast.LENGTH_SHORT).show();
            loadOrders();
        }
    }
