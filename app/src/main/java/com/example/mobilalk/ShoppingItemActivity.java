package com.example.mobilalk;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

public class ShoppingItemActivity extends AppCompatActivity {
    public static final String TAG = ShoppingItemActivity.class.getName();
    Button mapButton;
    Button addToCart;
    private FrameLayout redCircle;
    private TextView countTV;
    private int cartItems;

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

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(name);
        }

        ImageView imageView = (ImageView) findViewById(R.id.itemImage);
        TextView priceTV = findViewById(R.id.price);
        TextView descTV = findViewById(R.id.description);

        priceTV.setText(price);
        descTV.setText(desc);
        try {
            Glide.with(this).load(item.getImageResource()).into(imageView);
        } catch (Exception e) {
            Log.e(TAG, "Error loading image", e);
        }
        Log.d(TAG, "onCreate: " + item.getImageResource());

        mapButton = (Button) findViewById(R.id.open_map);

        mapButton.setOnClickListener(v -> {
            Intent intent = new Intent(ShoppingItemActivity.this, MapsActivity.class);
            startActivity(intent);
        });

        addToCart = (Button) findViewById(R.id.add_to_cart);

        addToCart.setOnClickListener(v -> {
            updateAlertIcon();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.shop_item_menu, menu);

        MenuItem menuItem2 = menu.findItem(R.id.cart);
        View view = LayoutInflater.from(this).inflate(R.layout.custom_menu_item, null);
        menuItem2.setActionView(view);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG,"onoptions");
        if (item.getItemId() == R.id.log_out){
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;
        } else if (item.getItemId() == R.id.settings) {
            Log.d(TAG, "Settings");
            return true;
        } else if (item.getItemId() == R.id.cart) {
            Log.d(TAG, "Cart");
            return true;
        } else{
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        final MenuItem alertMI = menu.findItem(R.id.cart);
        FrameLayout rootView = (FrameLayout) alertMI.getActionView();
        Log.d(TAG, "rootview: " + rootView);

        redCircle = (FrameLayout) rootView.findViewById(R.id.circle);
        countTV = (TextView) rootView.findViewById(R.id.count);

        rootView.setOnClickListener(v -> onOptionsItemSelected(alertMI));

        updateAlertIcon();

        return super.onPrepareOptionsMenu(menu);
    }

    public void updateAlertIcon() {
        cartItems = getCartItemCount() + 1;

        if (countTV != null) {
            if (0 < cartItems){
                countTV.setText(String.valueOf(cartItems));
            } else{
                countTV.setText("");
            }
        }

        SharedPreferences sharedPreferences = getSharedPreferences("cart", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("cartItemCount", cartItems);
        editor.apply();

        if (redCircle != null) {
            redCircle.setVisibility((cartItems > 0) ? View.VISIBLE : View.GONE);
        }
    }

    public int getCartItemCount() {
        SharedPreferences sharedPreferences = getSharedPreferences("cart", MODE_PRIVATE);
        return sharedPreferences.getInt("cartItemCount", 0);
    }
}