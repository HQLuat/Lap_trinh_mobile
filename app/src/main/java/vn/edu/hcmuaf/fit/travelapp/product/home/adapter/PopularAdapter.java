package vn.edu.hcmuaf.fit.travelapp.product.home.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import vn.edu.hcmuaf.fit.travelapp.databinding.ViewholderPopularBinding;
import vn.edu.hcmuaf.fit.travelapp.product.home.ui.DetailActivity;
import vn.edu.hcmuaf.fit.travelapp.product.productManagement.data.model.Product;

public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.Viewholder> {
    List<Product> items;
    Context context;

    public PopularAdapter(List<Product> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public PopularAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderPopularBinding binding = ViewholderPopularBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        context = parent.getContext();
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PopularAdapter.Viewholder holder, int position) {
        Product item = items.get(position);
        holder.binding.titleTxt.setText(item.getName());
        holder.binding.priceTxt.setText(item.getPrice() + "đ");
        holder.binding.addressTxt.setText(item.getAddress());
        holder.binding.scoreTxt.setText("5");

        Glide.with(context)
                .load(item.getImageUrl())
                .into(holder.binding.pic);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("object", item);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        ViewholderPopularBinding binding;
        public Viewholder(ViewholderPopularBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
