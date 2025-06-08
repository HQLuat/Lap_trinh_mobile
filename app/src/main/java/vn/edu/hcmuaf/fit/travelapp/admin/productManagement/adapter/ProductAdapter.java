package vn.edu.hcmuaf.fit.travelapp.admin.productManagement.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.hcmuaf.fit.travelapp.R;
import vn.edu.hcmuaf.fit.travelapp.databinding.ItemProductBinding;
import vn.edu.hcmuaf.fit.travelapp.admin.productManagement.data.model.Product;
import vn.edu.hcmuaf.fit.travelapp.admin.productManagement.data.repository.OnProductActionListener;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> productList;
    private final OnProductActionListener listener;

    public ProductAdapter(List<Product> productList, OnProductActionListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemProductBinding binding = ItemProductBinding.inflate(inflater, parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product p = productList.get(position);
        holder.binding.tvProductName.setText(p.getName());
        holder.binding.tvProductPrice.setText("Giá: " + p.getPrice());
        holder.binding.tvProductStock.setText("Kho: " + p.getStock());

        // Convert Timestamp to formatted date string
        if (p.getDepartureDate() != null) {
            Date date = p.getDepartureDate().toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String formattedDate = sdf.format(date);
            holder.binding.tvDepartureDate.setText("Khởi hành: " + formattedDate);
        } else {
            holder.binding.tvDepartureDate.setText("Khởi hành: --");
        }

        Glide.with(holder.itemView.getContext())
                .load(p.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_picture)
                .into(holder.binding.imgProduct);

        // add event handler for remove button
        holder.binding.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteProduct(p);
            }
        });

        // add event handler for update button
        holder.binding.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUpdateProduct(p);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ItemProductBinding binding;

        public ProductViewHolder(@NonNull ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
