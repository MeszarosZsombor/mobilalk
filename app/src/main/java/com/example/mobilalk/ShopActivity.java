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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kotlinx.coroutines.ObsoleteCoroutinesApi;

public class ShopActivity extends AppCompatActivity {

    private static final String TAG = ShopActivity.class.getName();
    private FirebaseUser user;

    private RecyclerView recyclerView;
    private Menu menuGlobal;
    private ArrayList<ShoppingItem> itemList;
    private ShoppingItemAdapter adapter;
    private FirebaseFirestore firestore;
    private CollectionReference collection;

    private FrameLayout redCircle;
    private TextView countTV;
    private int cartItems;
    private int gridNumber = 2;
    private int limit = 4;
    private boolean initialDataLoaded = false;
    private Button viewMore;

    private SearchView searchview;

    private AlarmManager manager;
    // 1 = sortByAlpha , 0 = sortByNumber
    private int sortBy = 1;

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

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Áruház");
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

        setAlarmManager();
        queryData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.shop_list_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.search_bar);
        searchview = (SearchView) MenuItemCompat.getActionView(menuItem);

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

        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(@NonNull MenuItem item) {
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(@NonNull MenuItem item) {
                return false;
            }
        });

        return true;
    }


    private void initializeData() {
        String[] itemsList = getResources().getStringArray(R.array.phone_name);
        String[] itemsInfo = getResources().getStringArray(R.array.phone_info);
        String[] itemsPrice = getResources().getStringArray(R.array.phone_price);
        String[] itemsDesc = getResources().getStringArray(R.array.phone_desc);
        int[] itemsCount = getResources().getIntArray(R.array.count);
        ArrayList<Integer> imageList = new ArrayList<>();

        for (int i = 0; i < itemsList.length; i++) {
            String imageName = itemsList[i].toLowerCase().replace(" ", "") + "img";
            int imageId = getResources().getIdentifier(imageName, "drawable", getPackageName());
            imageList.add(imageId);

            collection.add(new ShoppingItem(
                    itemsList[i],
                    itemsInfo[i],
                    itemsDesc[i],
                    itemsPrice[i],
                    imageId,
                    itemsCount[i]
            ));
        }
    }


    private void queryData() {
        itemList.clear();

        if (sortBy == 1) {
            collection.orderBy("name").get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    ShoppingItem item = document.toObject(ShoppingItem.class);
                    if (item.getCount() > 0) {
                        itemList.add(item);
                    }
                }

                if (itemList.size() == 0 && !initialDataLoaded) {
                    collection.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                initializeData();
                                initialDataLoaded = true;
                            }
                        } else {
                            Log.d(TAG, "Error checking if collection exists: ", task.getException());
                        }
                    });
                }

                displayData();
            });
        } else {
            if (itemList.size() == 0 && !initialDataLoaded) {
                collection.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            initializeData();
                            initialDataLoaded = true;
                        }
                    } else {
                        Log.d(TAG, "Error checking if collection exists: ", task.getException());
                    }
                });
            }

            displayData();
        }

        viewMore.setOnClickListener(v -> {
            limit += 4;
            displayData();
        });
    }

    private void displayData() {
        List<ShoppingItem> displayList = new ArrayList<>(itemList.subList(0, Math.min(limit, itemList.size())));
        adapter.setData(displayList);
        adapter.notifyDataSetChanged();

        if (limit >= itemList.size()) {
            viewMore.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onoptions");
        if (item.getItemId() == R.id.log_out) {
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;
        } else if (item.getItemId() == R.id.greaterThan) {
            MenuItem sort = menuGlobal.findItem(R.id.sortByAlphabet);
            sortBy = 0;
            queryData();
            item.setVisible(false);
            sort.setVisible(true);
            return true;
        } else if (item.getItemId() == R.id.sortByAlphabet) {
            MenuItem greaterThanItem = menuGlobal.findItem(R.id.greaterThan);
            sortBy = 1;
            queryData();
            item.setVisible(false);
            greaterThanItem.setVisible(true);
            return true;
        } else if (item.getItemId() == R.id.cart) {
            Intent intent = new Intent(ShopActivity.this, CartActivity.class);
            startActivity(intent);
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        menuGlobal = menu;
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

    @Override
    public void onPause() {
        super.onPause();

        onOptionsMenuClosed(menuGlobal);
    }
}