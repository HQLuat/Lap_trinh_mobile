package vn.edu.hcmuaf.fit.travelapp.order.adapter;

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
        void onReorderClicked(Order order);
    }

    public OrderHistoryAdapter(OnOrderClickListener listener) {
        super(new DiffUtil.ItemCallback<Order>() {
            @Override
            public boolean areItemsTheSame(@NonNull Order oldItem, @NonNull Order newItem) {
                return oldItem.getOrderId().equals(newItem.getOrderId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Order oldItem, @NonNull Order newItem) {
                return oldItem.getPaymentStatusEnum() == newItem.getPaymentStatusEnum()
                        && oldItem.getDestination().equals(newItem.getDestination())
                        && Double.compare(oldItem.getTotalAmount(), newItem.getTotalAmount()) == 0
                        && ((oldItem.getImageUrl() == null && newItem.getImageUrl() == null)
                        || (oldItem.getImageUrl() != null && oldItem.getImageUrl().equals(newItem.getImageUrl())));
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
            bindActionButton(order, listener);
            itemView.setOnClickListener(v -> listener.onOrderClicked(order));
        }

        private void bindImage(Order order) {
            String imageUrl = order.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(binding.imgThumbnail.getContext())
                        .load(imageUrl)
                        .into(binding.imgThumbnail);
            } else {
                binding.imgThumbnail.setImageResource(android.R.color.darker_gray);
            }
        }

        private void bindTexts(Order order) {
            binding.tvDestination.setText(order.getDestination());
            binding.tvAmount.setText(String.format("%,.0f ₫", order.getTotalAmount()));
            Order.PaymentStatus psEnum = order.getPaymentStatusEnum();
            String statusDisplay = (psEnum != null) ? psEnum.toDisplayText() : "Không rõ";
            binding.tvStatus.setText(statusDisplay);
        }

        private void bindActionButton(Order order, OnOrderClickListener listener) {
            Order.PaymentStatus ps = order.getPaymentStatusEnum();
            if (ps == null) {
                binding.btnCancel.setVisibility(View.GONE);
                return;
            }
            switch (ps) {
                case PENDING:
                case FAILED:
                    binding.btnCancel.setText("Hủy đặt");
                    binding.btnCancel.setVisibility(View.VISIBLE);
                    binding.btnCancel.setOnClickListener(v -> listener.onCancelOrRefundClicked(order));
                    break;
                case PAID:
                    binding.btnCancel.setText("Hoàn tiền");
                    binding.btnCancel.setVisibility(View.VISIBLE);
                    binding.btnCancel.setOnClickListener(v -> listener.onCancelOrRefundClicked(order));
                    break;
//                case REFUNDED:
//                case CANCELED:
//                    binding.btnCancel.setText("Đặt lại");
//                    binding.btnCancel.setVisibility(View.VISIBLE);
//                    binding.btnCancel.setOnClickListener(v -> listener.onReorderClicked(order));
//                    break;
                default:
                    binding.btnCancel.setVisibility(View.GONE);
                    break;
            }
        }
    }
}
