package vn.edu.hcmuaf.fit.travelapp.admin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.edu.hcmuaf.fit.travelapp.R;
import vn.edu.hcmuaf.fit.travelapp.admin.data.model.AdminFunction;
import vn.edu.hcmuaf.fit.travelapp.databinding.ViewholderDashboardItemBinding;

public class FunctionAdapter extends RecyclerView.Adapter<FunctionAdapter.Viewholder> {
    List<AdminFunction> items;
    Context context;

    public FunctionAdapter(List<AdminFunction> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public FunctionAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderDashboardItemBinding binding = ViewholderDashboardItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        context = parent.getContext();
        return new FunctionAdapter.Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FunctionAdapter.Viewholder holder, int position) {
        AdminFunction item = items.get(position);
        holder.binding.textTitle.setText(item.getTitle());
        holder.binding.icFunction.setImageResource(item.getIconResId());
        holder.binding.cardIconBackground.setCardBackgroundColor(ContextCompat.getColor(context, item.getCardColorResId()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        ViewholderDashboardItemBinding binding;
        public Viewholder(ViewholderDashboardItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
