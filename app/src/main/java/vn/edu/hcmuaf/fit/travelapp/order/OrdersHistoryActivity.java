package vn.edu.hcmuaf.fit.travelapp.order;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

/**
 * Activity hiển thị lịch sử đơn hàng của người dùng.
 * Sử dụng RecyclerView với OrderHistoryAdapter để trình bày danh sách đơn hàng.
 */
public class OrdersHistoryActivity extends AppCompatActivity {

    private ActivityOrdersHistoryBinding binding;
    private RecyclerView rvOrderHistory;
    private OrderHistoryAdapter adapter;
    private List<Order> orderList = new ArrayList<>();
    private OrderRepository orderRepository;
    private String currentUserId;
    private MenuHandler menuHandler;

    /**
     * Khởi tạo Activity và thiết lập giao diện, menu, kiểm tra đăng nhập,
     * khởi tạo RecyclerView và tác vụ tải đơn hàng.
     * @param savedInstanceState trạng thái trước đó của Activity (nếu có)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrdersHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Thiết lập bottom navigation và đánh dấu tab hiện tại
        ChipNavigationBar bottomNavigation = findViewById(R.id.bottomNavigation);
        menuHandler = new MenuHandler(this, bottomNavigation, R.id.orderHistory);
        menuHandler.setupMenu();

        // Kiểm tra đăng nhập; nếu chưa, hiển thị thông báo và đóng Activity
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        binding.rvOrderHistory.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo adapter và gán listener cho sự kiện click
        adapter = new OrderHistoryAdapter(orderList, order -> {
            // TODO: Mở OrderDetailActivity khi click vào đơn hàng
            // Intent intent = new Intent(this, OrderDetailActivity.class);
            // intent.putExtra("orderId", order.getOrderId());
            // startActivity(intent);
        });
        binding.rvOrderHistory.setAdapter(adapter);

        orderRepository = new OrderRepository();
        loadOrders();
    }

    /**
     * Gửi yêu cầu tải danh sách đơn hàng kèm chi tiết từ repository.
     * Cập nhật adapter hoặc hiển thị thông báo nếu có lỗi.
     */
    private void loadOrders() {
        orderRepository.getOrdersWithItems(currentUserId, new OrderListCallback() {
            // Callback khi tải đơn hàng thành công.
            // @param orders danh sách đơn hàng nhận được
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

            //Callback khi tải đơn hàng thất bại.
            //@param e ngoại lệ phát sinh trong quá trình tải
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("OrderLoadError", "Lỗi khi tải đơn hàng", e);
                Toast.makeText(OrdersHistoryActivity.this,
                        "Lỗi tải đơn hàng: " + e.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
