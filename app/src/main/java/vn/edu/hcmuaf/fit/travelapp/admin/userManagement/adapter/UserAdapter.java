package vn.edu.hcmuaf.fit.travelapp.admin.userManagement.adapter;

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
import vn.edu.hcmuaf.fit.travelapp.admin.userManagement.data.repository.OnUserActionListener;
import vn.edu.hcmuaf.fit.travelapp.auth.data.model.User;
import vn.edu.hcmuaf.fit.travelapp.databinding.ViewholderItemUserBinding;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> items;
    private OnUserActionListener listener;

    public UserAdapter(List<User> items, OnUserActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewholderItemUserBinding binding = ViewholderItemUserBinding.inflate(inflater, parent, false);
        return new UserAdapter.UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = items.get(position);
        holder.binding.tvUserName.setText(user.getFullName());
        holder.binding.tvUserEmail.setText("Email: " + user.getEmail());
        holder.binding.tvUserGender.setText(user.getGender() != null ? "Giới tính: " + user.getGender() : "Giới tính: Chưa cập nhật");

        // Convert timestamp to string
        Date date = user.getCreatedAt().toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy", Locale.getDefault());
        String formattedDate = sdf.format(date);
        holder.binding.tvCreateDate.setText("Ngày tạo: " + formattedDate);

        // avatar
        Glide.with(holder.itemView.getContext())
                .load(user.getProfileImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_picture)
                .into(holder.binding.imgUser);

        // event for buttons
        holder.binding.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteUser(user);
        });
        holder.binding.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onUpdateUser(user);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<User> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ViewholderItemUserBinding binding;

        public UserViewHolder(@NonNull ViewholderItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
