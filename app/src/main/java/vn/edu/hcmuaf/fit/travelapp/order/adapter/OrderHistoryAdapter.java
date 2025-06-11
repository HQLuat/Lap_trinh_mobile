package vn.edu.hcmuaf.fit.travelapp.order.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import vn.edu.hcmuaf.fit.travelapp.databinding.ItemOrderHistoryBinding;
import vn.edu.hcmuaf.fit.travelapp.order.model.Order;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {

    private final List<Order> orders;
    private final OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClicked(Order order);
    }

    public OrderHistoryAdapter(List<Order> orders, OnOrderClickListener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemOrderHistoryBinding binding = ItemOrderHistoryBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order, listener);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrderHistoryBinding binding;

        public ViewHolder(ItemOrderHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Order order, OnOrderClickListener listener) {
            // Ảnh thumbnail của order (theo model)
            String imageUrl = order.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(binding.imgThumbnail.getContext())
                        .load(imageUrl)
                        .into(binding.imgThumbnail);
            } else {
                binding.imgThumbnail.setImageResource(android.R.color.darker_gray);
            }

            // Destination
            binding.tvDestination.setText(order.getDestination());

            // Ngày (ví dụ show departure date hoặc createdAt)
//            String dateText = order.getFormattedDepartureDate();
//            if (dateText.isEmpty()) {
//                dateText = order.getFormattedCreatedAt();
//            }
//            binding.tvDate.setText(dateText);

            // Tổng tiền
            binding.tvAmount.setText(String.format("%,.0f ₫", order.getTotalAmount()));

            // Trạng thái thanh toán (display)
            Order.PaymentStatus psEnum = order.getPaymentStatusEnum();
            String statusDisplay = psEnum != null ? psEnum.toDisplayText() : "Không rõ";
            binding.tvStatus.setText(statusDisplay);

            // Nếu có nút chi tiết
            binding.btnDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClicked(order);
                }
            });
        }
    }
}
