package com.example.mobilalk;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

public class ShoppingItemActivity extends AppCompatActivity {
    public static final String TAG = ShoppingItemActivity.class.getName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shopping_item);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ShoppingItem item = (ShoppingItem) getIntent().getSerializableExtra("item");
        String name = item.getName();
        String price = item.getPrice();
        String desc = item.getDesc();

        TextView nameTV = findViewById(R.id.itemTitle);
        ImageView imageView = (ImageView) findViewById(R.id.itemImage);
        TextView priceTV = findViewById(R.id.price);
        TextView descTV = findViewById(R.id.description);

        nameTV.setText(name);
        priceTV.setText(price);
        descTV.setText(desc);
        try {
            Glide.with(this).load(item.getImageResource()).into(imageView);
        } catch (Exception e) {
            Log.e(TAG, "Error loading image", e);
        }
        Log.d(TAG, "onCreate: " + item.getImageResource());
    }
}