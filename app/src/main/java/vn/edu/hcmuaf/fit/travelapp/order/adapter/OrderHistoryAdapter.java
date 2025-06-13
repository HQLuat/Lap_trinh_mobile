package vn.edu.hcmuaf.fit.travelapp.order.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
        void onCancelOrRefundClicked(Order order);
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
            String imageUrl = order.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(binding.imgThumbnail.getContext())
                        .load(imageUrl)
                        .into(binding.imgThumbnail);
            } else {
                binding.imgThumbnail.setImageResource(android.R.color.darker_gray);
            }
            binding.tvDestination.setText(order.getDestination());
            binding.tvAmount.setText(String.format("%,.0f ₫", order.getTotalAmount()));
            Order.PaymentStatus psEnum = order.getPaymentStatusEnum();
            String statusDisplay = psEnum != null ? psEnum.toDisplayText() : "Không rõ";
            binding.tvStatus.setText(statusDisplay);
            binding.btnDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClicked(order);
                }
            });

            // Xác định nếu được phép hủy (pending) hoặc hoàn tiền (paid)
            Order.PaymentStatus ps = order.getPaymentStatusEnum();
            boolean canCancel = ps == Order.PaymentStatus.PENDING || ps == Order.PaymentStatus.FAILED;
            boolean canRefund = ps == Order.PaymentStatus.PAID;

            if (canCancel) {
                binding.btnCancel.setText("Hủy đặt");
                binding.btnCancel.setVisibility(View.VISIBLE);
            }
            else if (canRefund) {
                binding.btnCancel.setText("Hoàn tiền");
                binding.btnCancel.setVisibility(View.VISIBLE);
            }
            else {
                binding.btnCancel.setVisibility(View.GONE);
            }

            binding.btnCancel.setOnClickListener(v -> {
                Log.d("Adapter", "Cancel/Refund button clicked for order: " + order.getOrderId());
                if (listener != null) {
                    listener.onCancelOrRefundClicked(order);
                }
            });
        }
    }
}
