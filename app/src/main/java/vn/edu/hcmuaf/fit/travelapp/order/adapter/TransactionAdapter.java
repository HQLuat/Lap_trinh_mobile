package vn.edu.hcmuaf.fit.travelapp.order.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import vn.edu.hcmuaf.fit.travelapp.R;
import vn.edu.hcmuaf.fit.travelapp.order.model.Order;


public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.Viewholder> {

    private final Context context;
    private final List<Order> orders;
    private final OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onClick(Order order);
    }

    public TransactionAdapter(Context context, List<Order> orders, OnOrderClickListener listener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_card, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        Order order = orders.get(position);

        // Load ảnh từ order.imageUrl
        Glide.with(context)
                .load(order.getImageUrl())
                .into(holder.imgThumbnail);

        // Tổng tiền
        holder.tvAmount.setText(String.format("%,.0f ₫", order.getTotalAmount()));

        // Trạng thái thanh toán
        holder.tvStatus.setText("Trạng thái: " + getPaymentStatusText(order.getPaymentStatus()));

        // Button click
        holder.btnDetails.setOnClickListener(v -> listener.onClick(order));
    }

    private String getPaymentStatusText(String status) {
        switch (status) {
            case "PAID":
                return "Đã thanh toán";
            case "PENDING":
                return "Chờ thanh toán";
            case "FAILED":
                return "Thanh toán thất bại";
            case "CANCELED":
                return "Đã hủy";
            default:
                return "Không rõ";
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class Viewholder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail;
        TextView tvDestination, tvReviewCount, tvAmount, tvStatus;
        RatingBar ratingBar;
        MaterialButton btnDetails;
        View ratingContainer;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgThumbnail);
            tvDestination = itemView.findViewById(R.id.tvDestination);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            tvReviewCount = itemView.findViewById(R.id.tvReviewCount);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnDetails = itemView.findViewById(R.id.btnDetails);
            ratingContainer = itemView.findViewById(R.id.ratingContainer);
        }
    }
}
