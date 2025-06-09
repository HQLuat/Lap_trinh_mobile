package vn.edu.hcmuaf.fit.travelapp.order;

import android.util.Log;

import android.os.Bundle;
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
import vn.edu.hcmuaf.fit.travelapp.databinding.ActivityTransactionBinding;
import vn.edu.hcmuaf.fit.travelapp.home.ui.MenuHandler;
import vn.edu.hcmuaf.fit.travelapp.order.adapter.TransactionAdapter;
import vn.edu.hcmuaf.fit.travelapp.order.data.repository.OrderListCallback;
import vn.edu.hcmuaf.fit.travelapp.order.data.repository.OrderRepo;
import vn.edu.hcmuaf.fit.travelapp.order.model.Order;

public class TransactionActivity extends AppCompatActivity {

    private RecyclerView rvTransaction;
    private TransactionAdapter adapter;
    private List<Order> orderList = new ArrayList<>();
    private OrderRepo orderRepository; // Bạn phải khởi tạo class này
    private String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private MenuHandler menuHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_transaction);

        // set up menu
        ChipNavigationBar bottomNavigation =findViewById(R.id.bottomNavigation);
        menuHandler = new MenuHandler(this, bottomNavigation, R.id.transaction);
        menuHandler.setupMenu();

        rvTransaction = findViewById(R.id.rvTransaction);
        rvTransaction.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TransactionAdapter(this, orderList, null);

//        adapter = new TransactionAdapter(this, orderList, order -> {
//            // Xử lý khi bấm "Xem chi tiết"
//            Intent intent = new Intent(TransactionActivity.this, OrderDetailActivity.class);
//            intent.putExtra("order", order); // Truyền object
//            startActivity(intent);
//        });
        rvTransaction.setAdapter(adapter);

        orderRepository = new OrderRepo();
        loadOrders();
    }

    private void loadOrders() {
        orderRepository.getOrdersWithItems(userId, new OrderListCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                orderList.clear();
                orderList.addAll(orders);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("OrderLoadError", "Lỗi khi tải đơn hàng", e); // log chi tiết
                Toast.makeText(TransactionActivity.this, "Lỗi tải đơn hàng: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
