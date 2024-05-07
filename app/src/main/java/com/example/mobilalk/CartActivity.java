package com.example.mobilalk;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CartActivity extends AppCompatActivity {
    public static final String TAG = CartActivity.class.getName();

    private int cartItems;

    private List<CartItem> cartItemsList;

    private CollectionReference collection;

    private CartItemAdapter adapter;

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

        cartItemsList = new ArrayList<>();

        updateItemsList();

        adapter = new CartItemAdapter(this, cartItemsList);
        recyclerView.setAdapter(adapter);

        collection = FirebaseFirestore.getInstance().collection("Phones");

        getShoppingItems().observe(this,cartItemsList -> {
            adapter.setShoppingItems(cartItemsList);
            adapter.notifyDataSetChanged();
        });
    }

    public LiveData<List<CartItem>> getShoppingItems() {
        return shoppingItemsLiveData;
    }

    public void updateShoppingItems() {
        shoppingItemsLiveData.setValue(cartItemsList);
    }

    public void updateItemsList(){
        SharedPreferences sharedPreferences = getSharedPreferences("phones", MODE_PRIVATE);
        Map<String, ?> phones = sharedPreferences.getAll();

        Log.d(TAG, "updateItemsList: " + phones);

        cartItemsList.clear();
        for (Map.Entry<String, ?> entry : phones.entrySet()) {
            String phoneName = entry.getKey();
            int count = (Integer) entry.getValue();
            if(count != 0) {
                cartItemsList.add(new CartItem(phoneName, count));
            }
        }
        Log.d(TAG, "updateItemsList: " + cartItemsList);
    }

    public void updateCart(CartItem currentItem, int i) {
        cartItems = getCartItemCount();

        cartItemsList.stream()
                .filter(item -> item.getName().equals(currentItem.getName()))
                .findFirst()
                .ifPresent(item -> {
                    int currentCount = item.getCount();
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
                    sharedPreferences.edit().putInt("cartItemCount", cartItems + (i == 1 ? 1 : -1)).apply();

                    collection.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                if (doc.toObject(ShoppingItem.class).getName().equals(currentItem.getName())) {
                                    String id = doc.getId();
                                    DocumentReference itemRef = collection.document(id);

                                    itemRef.get().addOnSuccessListener(documentSnapshot -> {
                                        Long count = documentSnapshot.getLong("count");
                                        if (count != null && count > 0) {
                                            itemRef.update("count", count + (i == 1 ? 1 : -1)).addOnSuccessListener(v -> {
                                                adapter.notifyDataSetChanged();

                                            });
                                        }
                                    });
                                }
                            }
                        }
                    });
                    SharedPreferences sharedPreferencesP = getSharedPreferences("phones", MODE_PRIVATE);
                    SharedPreferences.Editor editorP = sharedPreferencesP.edit();
                    editorP.putInt(currentItem.getName(), currentCount).apply();

                    updateShoppingItems();
                    updateItemsList();
                });
    }

    public int getCartItemCount(String phoneName){
        SharedPreferences sharedPreferences = getSharedPreferences("phones", MODE_PRIVATE);
        return sharedPreferences.getInt(phoneName, 0);
    }

    public int getCartItemCount() {
        SharedPreferences sharedPreferences = getSharedPreferences("cart", MODE_PRIVATE);
        return sharedPreferences.getInt("cartItemCount", 0);
    }
}