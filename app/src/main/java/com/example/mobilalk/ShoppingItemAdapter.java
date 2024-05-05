package com.example.mobilalk;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class ShoppingItemAdapter extends RecyclerView.Adapter<ShoppingItemAdapter.ViewHolder> implements Filterable {

    public static final String TAG = ShoppingItemAdapter.class.getName();
    private ArrayList<ShoppingItem> mShoppingItemData = new ArrayList<>();
    private ArrayList<ShoppingItem> mShoppingItemDataAll = new ArrayList<>();
    private Context mContext;

    ShoppingItemAdapter(Context context, ArrayList<ShoppingItem> itemsData) {
        this.mShoppingItemData = itemsData;
        this.mShoppingItemDataAll = itemsData;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.list_items, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShoppingItem currentItem = mShoppingItemData.get(position);

        holder.bindTo(currentItem);
    }

    @Override
    public int getItemCount() {
        return mShoppingItemData.size();
    }

    @Override
    public Filter getFilter() {
        return shoppingFilter;
    }

    private Filter shoppingFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<ShoppingItem> filteredList = new ArrayList<>();
            FilterResults results = new FilterResults();

            if(charSequence == null || charSequence.length() == 0) {
                results.count = mShoppingItemDataAll.size();
                results.values = mShoppingItemDataAll;
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for(ShoppingItem item : mShoppingItemDataAll) {
                    if(item.getName().toLowerCase().contains(filterPattern)){
                        filteredList.add(item);
                    }
                }

                results.count = filteredList.size();
                results.values = filteredList;
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mShoppingItemData = (ArrayList)filterResults.values;
            notifyDataSetChanged();
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTitleText;
        private TextView mInfoText;
        private TextView mPriceText;
        private ImageView mItemImage;
        private TextView mCount;

        public ViewHolder(View itemView) {
            super(itemView);

            mTitleText = itemView.findViewById(R.id.itemTitle);
            mInfoText = itemView.findViewById(R.id.description);
            mItemImage = itemView.findViewById(R.id.itemImage);
            mPriceText = itemView.findViewById(R.id.price);
            mCount = itemView.findViewById(R.id.count);

            itemView.findViewById(R.id.add_to_cart).setOnClickListener(view -> {
                ShoppingItem currentItem = mShoppingItemData.get(getAdapterPosition());
                ((ShopActivity)mContext).updateAlertIcon(currentItem);
            });

            itemView.setOnClickListener(view -> {
                Intent intent = new Intent(mContext, ShoppingItemActivity.class);
                intent.putExtra("item", mShoppingItemData.get(getAdapterPosition()));
                mContext.startActivity(intent);
            });
        }

        void bindTo(ShoppingItem currentItem){
            mTitleText.setText(currentItem.getName());
            mInfoText.setText(currentItem.getInfo());
            mPriceText.setText(currentItem.getPrice());

            if(currentItem.getCount() < 10){
                mCount.setText("Már csak " + currentItem.getCount() + " db maradt!");
            }

            Glide.with(mContext).load(currentItem.getImageResource()).into(mItemImage);
        }
    }
}
