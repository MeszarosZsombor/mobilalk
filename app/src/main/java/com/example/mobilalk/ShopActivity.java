package com.example.mobilalk;

import android.annotation.SuppressLint;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ShopActivity extends AppCompatActivity {

    private static final String LOG_TAG = ShopActivity.class.getName();
    private FirebaseUser user;

    private RecyclerView recyclerView;
    private ArrayList<ShoppingItem> itemList;
    private ShoppingItemAdapter adapter;
    private FirebaseFirestore firestore;
    private CollectionReference items;

    private FrameLayout redCircle;
    private TextView countTV;
    private int cartItems = 0;
    private int gridNumber = 2;

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
            Log.d(LOG_TAG, "User logged in");
        }else{
            Log.d(LOG_TAG, "User not logged in");
            finish();
        }

        recyclerView = findViewById(R.id.shopListView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, gridNumber));
        itemList = new ArrayList<>();

        adapter = new ShoppingItemAdapter(this, itemList);
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        items = firestore.collection("Phones");

        queryData();
    }


    private void initializeData(){
        String[] itemsList = getResources().getStringArray(R.array.phone_name);
        Log.d(LOG_TAG, itemsList.toString());
        String[] itemsInfo = getResources().getStringArray(R.array.phone_info);
        Log.d(LOG_TAG, itemsInfo.toString());
        String[] itemsPrice = getResources().getStringArray(R.array.phone_price);
        Log.d(LOG_TAG, itemsPrice.toString());
        String[] itemsDesc = getResources().getStringArray(R.array.phone_desc);
        Log.d(LOG_TAG, itemsDesc.toString());
        TypedArray itemsImageResource = getResources().obtainTypedArray(R.array.phone_image);
        Log.d(LOG_TAG, itemsImageResource.toString());

        for (int i = 0; i < itemsList.length; i++){
            items.add(new ShoppingItem(
                    itemsList[i],
                    itemsInfo[i],
                    itemsDesc[i],
                    itemsPrice[i],
                    itemsImageResource.getResourceId(i, 0)
            ));
        }

        itemsImageResource.recycle();
    }


    private void queryData() {
        Log.d(LOG_TAG, itemList.toString());
        itemList.clear();
        Log.d(LOG_TAG, itemList.toString());

        Log.d(LOG_TAG, items.toString());

        items.orderBy("name").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Log.d(LOG_TAG, "ASD");
                ShoppingItem item = document.toObject(ShoppingItem.class);
                itemList.add(item);
            }

            if (itemList.size() == 0) {
                Log.d(LOG_TAG, "size 0");
                initializeData();
                queryData();
            }

            adapter.notifyDataSetChanged();
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.shop_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searchview = (SearchView) MenuItemCompat.getActionView(menuItem);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.log_out){
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;
        } else if (item.getItemId() == R.id.settings) {
            Log.d(LOG_TAG, "Settings");
            return true;
        } else if (item.getItemId() == R.id.cart) {
            Log.d(LOG_TAG, "Cart");
            return true;
        } else{
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        final MenuItem alertMI = menu.findItem(R.id.cart);
            FrameLayout rootView = (FrameLayout) alertMI.getActionView();

            redCircle = (FrameLayout) rootView.findViewById(R.id.circle);
            countTV = (TextView) rootView.findViewById(R.id.count);

            rootView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    onOptionsItemSelected(alertMI);
                }
            });
            return super.onPrepareOptionsMenu(menu);
    }

    public void updateAlertIcon() {
        cartItems += 1;
        if (0 < cartItems){
            countTV.setText(String.valueOf(cartItems));
        } else{
            countTV.setText("");
        }

        redCircle.setVisibility((cartItems > 0) ? View.VISIBLE : View.GONE);
    }
}