package com.example.mobilalk;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ShopActivity extends AppCompatActivity {

    private static final String LOG_TAG = ShopActivity.class.getName();
    private FirebaseUser user;

    private RecyclerView recyclerView;
    private ArrayList<ShoppingItem> itemList;
    private ShoppingItemAdapter adapter;
    private FirebaseFirestore firestore;
    private CollectionReference items;

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

    public void updateAlertIcon() {

        /* TODO */

    }

    private void initializeData(){
        String[] itemsList = getResources().getStringArray(R.array.phone_name);
        String[] itemsInfo = getResources().getStringArray(R.array.phone_info);
        String[] itemsPrice = getResources().getStringArray(R.array.phone_price);
        String[] itemsDesc = getResources().getStringArray(R.array.phone_desc);
        TypedArray itemsImageResource = getResources().obtainTypedArray(R.array.phone_image);

        for (int i = 0; i < itemsList.length; i++){
            items.add(new ShoppingItem(
                    itemsList[i],
                    itemsInfo[i],
                    itemsPrice[i],
                    itemsDesc[i],
                    itemsImageResource.getResourceId(i, 0)
            ));
        }

        itemsImageResource.recycle();
    }


    private void queryData() {
        itemList.clear();

        items.orderBy("name").limit(10).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                ShoppingItem item = document.toObject(ShoppingItem.class);
                itemList.add(item);
            }

            if (itemList.isEmpty()) {
                initializeData();
                queryData();
            }

            adapter.notifyDataSetChanged();
        });
    }

    /* TODO innentol lefele*/
}