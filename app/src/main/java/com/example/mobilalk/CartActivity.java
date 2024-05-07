package com.example.mobilalk;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CartActivity extends AppCompatActivity {
    public static final String TAG = CartActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences sharedPreferences = getSharedPreferences("phones", MODE_PRIVATE);
        Map<String, ?> phones = sharedPreferences.getAll();
        Log.d(TAG, "onCreate: " + phones);

        List<CartItem> cartItems = new ArrayList<>();
        for (Map.Entry<String, ?> entry : phones.entrySet()) {
            String phoneName = entry.getKey();
            int count = (Integer) entry.getValue();
            cartItems.add(new CartItem(phoneName, count));
        }

        CartItemAdapter adapter = new CartItemAdapter(this, cartItems);
        recyclerView.setAdapter(adapter);
    }
}