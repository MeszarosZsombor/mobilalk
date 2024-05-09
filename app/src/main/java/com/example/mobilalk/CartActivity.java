package com.example.mobilalk;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CartActivity extends AppCompatActivity {
    public static final String TAG = CartActivity.class.getName();

    private int cartItems;
    private TextView totalTV;

    private List<CartItem> cartItemsList;

    private CollectionReference collection;

    private CartItemAdapter adapter;

    private LinearLayout layout;
    private Button payButton;
    private TextView thankYouTV;

    private MutableLiveData<List<CartItem>> shoppingItemsLiveData = new MutableLiveData<>();

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

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Kosár");
        }

        cartItemsList = new ArrayList<>();

        layout = findViewById(R.id.layout);
        payButton = (Button) findViewById(R.id.toPay);
        thankYouTV = findViewById(R.id.thank_you);

        totalTV = findViewById(R.id.sumPrice);
        updateItemsList();

        adapter = new CartItemAdapter(this, cartItemsList);
        recyclerView.setAdapter(adapter);

        collection = FirebaseFirestore.getInstance().collection("Phones");

        getShoppingItems().observe(this,cartItemsList -> {
            adapter.setShoppingItems(cartItemsList);
            adapter.notifyDataSetChanged();
        });

        payButton.setOnClickListener(v -> {
            pay();
        });
    }

    public LiveData<List<CartItem>> getShoppingItems() {
        return shoppingItemsLiveData;
    }

    public void updateShoppingItems() {
        shoppingItemsLiveData.setValue(cartItemsList);
    }


    public void setTotalAmount() {
        int totalAmount = 0;

        for (CartItem item: cartItemsList) {
            totalAmount += item.sum();
        }

        if(totalAmount > 0) {
            layout.setVisibility(View.VISIBLE);
            totalTV.setText(String.valueOf(String.format("%,d Ft", totalAmount).replace(",", " ")));
        }else{
            layout.setVisibility(View.INVISIBLE);
        }
    }

    public void updateItemsList(){
        SharedPreferences sharedPreferences = getSharedPreferences("phones", MODE_PRIVATE);
        Map<String, ?> phones = sharedPreferences.getAll();

        cartItemsList.clear();
        for (Map.Entry<String, ?> entry : phones.entrySet()) {
            String phoneName = entry.getKey();
            String jsonString = (String) entry.getValue();
            int[] json = new Gson().fromJson(jsonString, int[].class);
            int count = json[0];
            int price = json[1];
            if(count != 0) {
                cartItemsList.add(new CartItem(phoneName, count, price));
            }
        }

        setTotalAmount();
    }

    public void updateCart(CartItem currentItem, int i) {
        cartItems = getCartItemCount();
        Log.d(TAG, "updateCart: " + cartItems);

        cartItemsList.stream()
                .filter(item -> item.getName().equals(currentItem.getName()))
                .findFirst()
                .ifPresent(item -> {
                    int currentCount = item.getCount();
                    Log.d(TAG, "currentcount: " + currentCount);
                    if (i == 1) {
                        currentCount++;
                        item.setCount(currentCount);
                    } else {
                        if(currentCount > 0) {
                            currentCount--;
                            item.setCount(currentCount);
                        }
                    }

                    SharedPreferences sharedPreferences = getSharedPreferences("cart", MODE_PRIVATE);
                    int countItem = cartItems + (i == 1 ? 1 : -1);
                    sharedPreferences.edit().putInt("cartItemCount", countItem).apply();

                    collection.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                if (doc.toObject(ShoppingItem.class).getName().equals(currentItem.getName())) {
                                    String id = doc.getId();
                                    DocumentReference itemRef = collection.document(id);

                                    itemRef.get().addOnSuccessListener(documentSnapshot -> {
                                        Long count = documentSnapshot.getLong("count");
                                        if (count != null && count > 0) {
                                            itemRef.update("count", count + (i == 1 ? -1 : 1)).addOnSuccessListener(v -> {adapter.notifyDataSetChanged();});
                                        }
                                    });
                                }
                            }
                        }
                    });

                    SharedPreferences sharedPreferencesP = getSharedPreferences("phones", MODE_PRIVATE);
                    SharedPreferences.Editor editorP = sharedPreferencesP.edit();
                    int price = item.getPrice();
                    int[] asd = {currentCount, price};
                    String json = new Gson().toJson(asd);
                    editorP.putString(currentItem.getName(), json).apply();

                    updateShoppingItems();
                    updateItemsList();
                });
    }

    public int getCartItemCount() {
        SharedPreferences sharedPreferences = getSharedPreferences("cart", MODE_PRIVATE);
        return sharedPreferences.getInt("cartItemCount", 0);
    }

    private void pay() {
        collection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    if (doc.toObject(ShoppingItem.class).getCount() == 0) {
                        String id = doc.getId();
                        DocumentReference itemRef = collection.document(id);

                        itemRef.get().addOnSuccessListener(documentSnapshot -> {
                            itemRef.delete();
                        });
                    }
                }
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("cart", MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();

        SharedPreferences sharedPreferencesP = getSharedPreferences("phones", MODE_PRIVATE);
        sharedPreferencesP.edit().clear().apply();

        cartItemsList.clear();
        setTotalAmount();
        adapter.notifyDataSetChanged();

        thankYouTV.setVisibility(View.VISIBLE);
    }
}