package com.example.mobilalk;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShopActivity extends AppCompatActivity {

    private static final String TAG = ShopActivity.class.getName();
    private FirebaseUser user;

    private RecyclerView recyclerView;
    private ArrayList<ShoppingItem> itemList;
    private ShoppingItemAdapter adapter;
    private FirebaseFirestore firestore;
    private CollectionReference collection;

    private FrameLayout redCircle;
    private TextView countTV;
    private int cartItems;
    private int gridNumber = 2;
    private int limit = 4;
    private Button viewMore;

    private AlarmManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            Log.d(TAG, "User logged in");
        }else{
            Log.d(TAG, "User not logged in");
            finish();
        }

        recyclerView = findViewById(R.id.shopListView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, gridNumber));
        itemList = new ArrayList<>();

        adapter = new ShoppingItemAdapter(this, itemList);
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        collection = firestore.collection("Phones");

        manager = (AlarmManager) getSystemService(ALARM_SERVICE);

        viewMore = findViewById(R.id.viewMore);

        SharedPreferences sharedPreferences = getSharedPreferences("cart", MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();

        SharedPreferences sharedPreferencesP = getSharedPreferences("phones", MODE_PRIVATE);
        sharedPreferencesP.edit().clear().apply();

        setAlarmManager();
        queryData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.shop_list_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searchview = (SearchView) MenuItemCompat.getActionView(menuItem);

        MenuItem menuItem2 = menu.findItem(R.id.cart);
        View view = LayoutInflater.from(this).inflate(R.layout.custom_menu_item, null);
        menuItem2.setActionView(view);
        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }


    private void initializeData(){
        String[] itemsList = getResources().getStringArray(R.array.phone_name);
        String[] itemsInfo = getResources().getStringArray(R.array.phone_info);
        String[] itemsPrice = getResources().getStringArray(R.array.phone_price);
        String[] itemsDesc = getResources().getStringArray(R.array.phone_desc);
        int[] itemsCount = getResources().getIntArray(R.array.count);
        TypedArray itemsImageResource = getResources().obtainTypedArray(R.array.phone_image);
        Log.d(TAG, itemsImageResource.toString());

        for (int i = 0; i < itemsList.length; i++){
                collection.add(new ShoppingItem(
                        itemsList[i],
                        itemsInfo[i],
                        itemsDesc[i],
                        itemsPrice[i],
                        itemsImageResource.getResourceId(i, 0),
                        itemsCount[i]
                ));
        }

        itemsImageResource.recycle();
    }


    private void queryData(){
        itemList.clear();

        collection.orderBy("name").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                ShoppingItem item = document.toObject(ShoppingItem.class);
                if(item.getCount() > 0) {
                    itemList.add(item);
                }
            }

            if (itemList.size() == 0) {
                initializeData();
                queryData();
            } else {
                displayData();
            }
        });

        viewMore.setOnClickListener(v -> {
            limit += 4;
            displayData();
        });
    }

    private void displayData() {
        List<ShoppingItem> displayList = new ArrayList<>(itemList.subList(0, Math.min(limit, itemList.size())));
        adapter.setData(displayList);
        adapter.notifyDataSetChanged();

        if(limit >= itemList.size()){
            viewMore.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG,"onoptions");
        if (item.getItemId() == R.id.log_out){
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;
        } else if (item.getItemId() == R.id.settings) {
            return true;
        } else if (item.getItemId() == R.id.cart) {
            Intent intent = new Intent(ShopActivity.this, CartActivity.class);
            startActivity(intent);
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

        refreshAlertIcon();

        return super.onPrepareOptionsMenu(menu);
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
                                    queryData();

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

    private void setAlarmManager(){
        long repeatInterval = 6000;
        long triggerTime = SystemClock.elapsedRealtime() + repeatInterval;

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE);

        manager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                repeatInterval,
                pendingIntent
        );

        manager.cancel(pendingIntent);
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshAlertIcon();
    }
}