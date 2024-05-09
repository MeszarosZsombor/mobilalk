package com.example.mobilalk;

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
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Map;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class ShoppingItemActivity extends AppCompatActivity {
    public static final String TAG = ShoppingItemActivity.class.getName();
    Button mapButton;
    Button addToCart;
    private FrameLayout redCircle;
    private TextView countTV;
    private int cartItems;
    private CollectionReference collection;
    private ShoppingItemAdapter adapter;

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
        int count = item.getCount();

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(name);
        }

        ImageView imageView = findViewById(R.id.itemImage);
        TextView priceTV = findViewById(R.id.price);
        TextView descTV = findViewById(R.id.description);

        ImageView map = findViewById(R.id.map);

        if(count < 10) {
            TextView countTV = findViewById(R.id.count);

            countTV.setText("Már csak " + count + " db maradt!");
        }

        priceTV.setText(price);
        descTV.setText(desc);
        try {
            Glide.with(this).load(item.getImageResource()).into(imageView);
        } catch (Exception e) {
            Log.e(TAG, "Error loading image", e);
        }


        try {
            Glide.with(this)
                    .load(R.drawable.map)
                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(25)))
                    .into(map);
        } catch (Exception e) {
            Log.e(TAG, "Error loading image", e);
        }

        Log.d(TAG, "Map: " + map);

        mapButton = findViewById(R.id.open_map);

        mapButton.setOnClickListener(v -> {
            Intent intent = new Intent(ShoppingItemActivity.this, MapsActivity.class);
            startActivity(intent);
        });

        addToCart = findViewById(R.id.add_item_to_cart);

        addToCart.setOnClickListener(v -> updateAlertIcon(name));

        collection = FirebaseFirestore.getInstance().collection("Phones");
        adapter = new ShoppingItemAdapter(this, new ArrayList<>());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.shop_item_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.cart_in_item);
        View view = LayoutInflater.from(this).inflate(R.layout.custom_menu_item, null);
        menuItem.setActionView(view);

        invalidateOptionsMenu();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG,"onoptions");
        if (item.getItemId() == R.id.log_out_button){
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;
        } else if (item.getItemId() == R.id.cart_in_item) {
            Intent intent = new Intent(ShoppingItemActivity.this, CartActivity.class);
            SharedPreferences sharedPreferences = getSharedPreferences("phones", MODE_PRIVATE);
            Map<String, ?> phones = sharedPreferences.getAll();
            Log.d(TAG, "updateItemsList: " + phones);
            startActivity(intent);
            return true;
        } else{
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);
        final MenuItem alertMI = menu.findItem(R.id.cart_in_item);
        FrameLayout rootView = (FrameLayout) alertMI.getActionView();
        Log.d(TAG, "rootview: " + rootView);

        redCircle = (FrameLayout) rootView.findViewById(R.id.circle);
        countTV = (TextView) rootView.findViewById(R.id.count);

        rootView.setOnClickListener(v -> onOptionsItemSelected(alertMI));

        refreshAlertIcon();

        return true;
    }

    public void refreshAlertIcon(){
        cartItems = getCartItemCount();

        if (countTV != null) {
            if (0 < cartItems){
                countTV.setText(String.valueOf(cartItems));
            } else{
                countTV.setText("");
            }
        }

        if (redCircle != null) {
            redCircle.setVisibility((cartItems > 0) ? View.VISIBLE : View.GONE);
        }
    }

    public void updateAlertIcon(String phoneName) {
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

        collection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    if (doc.toObject(ShoppingItem.class).getName().equals(phoneName)) {
                        String id = doc.getId();

                        DocumentReference itemRef = collection.document(id);

                        itemRef.get().addOnSuccessListener(documentSnapshot -> {
                            Long count = documentSnapshot.getLong("count");
                            if (count != null && count > 0) {
                                itemRef.update("count", count - 1).addOnSuccessListener(v -> {
                                    doc.toObject(ShoppingItem.class).setCount(count.intValue() - 1);

                                    runOnUiThread(() -> adapter.notifyDataSetChanged());

                                    SharedPreferences sharedPreferencesP = getSharedPreferences("phones", MODE_PRIVATE);
                                    SharedPreferences.Editor editorP = sharedPreferencesP.edit();
                                    int itemCount = getCartItemCount(phoneName) + 1;
                                    String priceStr = doc.toObject(ShoppingItem.class).getPrice();
                                    priceStr = priceStr.replace(" ", "").replace("Ft", "");
                                    int[] asd = {itemCount, Integer.parseInt(priceStr)};
                                    String json = new Gson().toJson(asd);
                                    editorP.putString(phoneName, json);
                                    editorP.commit();
                                });
                            }
                        }).addOnFailureListener(e -> {
                            Log.e(TAG, "updateAlertIcon: ", e);
                        });
                    }
                }
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "updateAlertIcon: ", e);
        });

    }

    public int getCartItemCount(String phoneName){
        SharedPreferences sharedPreferences = getSharedPreferences("phones", MODE_PRIVATE);
        Map<String, ?> phones = sharedPreferences.getAll();
        int count = 0;
        String jsonString;

        for (Map.Entry<String, ?> entry : phones.entrySet()) {
            if (phoneName.equals(entry.getKey())) {
                jsonString = (String) entry.getValue();
                int[] json = new Gson().fromJson(jsonString, int[].class);
                count = json[0];
            }
        }
        return count;
    }

    public int getCartItemCount() {
        SharedPreferences sharedPreferences = getSharedPreferences("cart", MODE_PRIVATE);
        return sharedPreferences.getInt("cartItemCount", 0);
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshAlertIcon();
    }
}