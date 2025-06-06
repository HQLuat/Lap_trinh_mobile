package vn.edu.hcmuaf.fit.travelapp.product.home.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import vn.edu.hcmuaf.fit.travelapp.databinding.ActivityDetailBinding;
import vn.edu.hcmuaf.fit.travelapp.product.productManagement.data.model.Product;

public class DetailActivity extends AppCompatActivity {
    ActivityDetailBinding binding;
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getIntentExtra();
        setVariable();
    }

    private void setVariable() {
        binding.titleTxt.setText(product.getName());
        binding.priceTxt.setText(product.getPrice() + "đ");
        binding.backBtn.setOnClickListener(v -> finish());
        binding.bedTxt.setText("2");
        binding.durationTxt.setText("2 ngay 1 dem");
        binding.distanceTxt.setText("5");
        binding.descriptionTxt.setText(product.getDescription());
        binding.addressTxt.setText(product.getAddress());
        binding.ratingBar.setRating(4.5f);
        binding.ratingTxt.setText("4.5 Rating");

        Glide.with(DetailActivity.this)
                .load(product.getImageUrl())
                .into(binding.pic);

        binding.addToCartBtn.setOnClickListener(view -> {
//            Intent intent = new Intent(DetailActivity.this, TicketActivity.class);
//            intent.putExtra("object", product);
//            startActivity(intent);
        });
    }

    private void getIntentExtra() {
        product = getIntent().getParcelableExtra("object");
    }
}