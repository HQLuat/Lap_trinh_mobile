package vn.edu.hcmuaf.fit.travelapp.order.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import vn.edu.hcmuaf.fit.travelapp.databinding.ItemOrderHistoryBinding;
import vn.edu.hcmuaf.fit.travelapp.order.model.Order;

public class OrderHistoryAdapter extends ListAdapter<Order, OrderHistoryAdapter.ViewHolder> {

    private static final String TAG = "OrderHistoryAdapter";
    private final OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClicked(Order order);
        void onCancelOrRefundClicked(Order order);
    }

    public OrderHistoryAdapter(OnOrderClickListener listener) {
        super(new DiffUtil.ItemCallback<Order>() {
            @Override
            public boolean areItemsTheSame(@NonNull Order oldItem, @NonNull Order newItem) {
                return oldItem.getOrderId().equals(newItem.getOrderId());
            }
            @Override
            public boolean areContentsTheSame(@NonNull Order oldItem, @NonNull Order newItem) {
                // So sánh các trường hiển thị; nếu model có equals phù hợp, có thể dùng oldItem.equals(newItem)
                return oldItem.getPaymentStatusEnum() == newItem.getPaymentStatusEnum()
                        && oldItem.getDestination().equals(newItem.getDestination())
                        && Double.compare(oldItem.getTotalAmount(), newItem.getTotalAmount()) == 0
                        && ((oldItem.getImageUrl() == null && newItem.getImageUrl() == null)
                        || (oldItem.getImageUrl() != null && oldItem.getImageUrl().equals(newItem.getImageUrl())));
                // Nếu cần so sánh thêm các trường khác hiển thị, bổ sung vào đây.
            }
        });
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
        Order order = getItem(position);
        holder.bind(order, listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrderHistoryBinding binding;

        ViewHolder(ItemOrderHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Order order, OnOrderClickListener listener) {
            bindImage(order);
            bindTexts(order);
//            bindDetailsClick(order, listener);
            bindCancelRefundButton(order, listener);
        }

        private void bindImage(Order order) {
            String imageUrl = order.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(binding.imgThumbnail.getContext())
                        .load(imageUrl)
                        .into(binding.imgThumbnail);
            } else {
                // Bạn có thể thay bằng drawable placeholder rõ ràng hơn
                binding.imgThumbnail.setImageResource(android.R.color.darker_gray);
            }
        }

        private void bindTexts(Order order) {
            binding.tvDestination.setText(order.getDestination());
            // Định dạng số tách miền: có thể tách helper nếu dùng nhiều chỗ
            binding.tvAmount.setText(String.format("%,.0f ₫", order.getTotalAmount()));
            Order.PaymentStatus psEnum = order.getPaymentStatusEnum();
            String statusDisplay = (psEnum != null) ? psEnum.toDisplayText() : "Không rõ";
            binding.tvStatus.setText(statusDisplay);
        }

//        private void bindDetailsClick(Order order, OnOrderClickListener listener) {
//            binding.btnDetails.setOnClickListener(v -> {
//                if (listener != null) listener.onOrderClicked(order);
//            });
//        }

        private void bindCancelRefundButton(Order order, OnOrderClickListener listener) {
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
                return;
            }

            binding.btnCancel.setOnClickListener(v -> {
                Log.d(TAG, "Cancel/Refund clicked for order: " + order.getOrderId());
                if (listener != null) listener.onCancelOrRefundClicked(order);
            });
        }
    }
}
