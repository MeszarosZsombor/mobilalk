package com.example.mobilalk;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {

    private static final String TAG = CartItemAdapter.class.getName();

    private Context mContext;
    private List<CartItem> mCartItems;

    CartItemAdapter(Context context, List<CartItem> cartItems){
        this.mContext = context;
        this.mCartItems = cartItems;
    }
    @NonNull
    @Override
    public CartItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemAdapter.ViewHolder holder, int position) {
        CartItem currentItem = mCartItems.get(position);
        holder.bindTo(currentItem);
    }

    @Override
    public int getItemCount() {
        return mCartItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitleText;
        private TextView mPriceText;
        private ImageView mItemImage;
        private TextView mCount;


        public ViewHolder(View itemView) {
            super(itemView);

            mTitleText = itemView.findViewById(R.id.itemTitle);
            mItemImage = itemView.findViewById(R.id.itemImage);
            mPriceText = itemView.findViewById(R.id.price);
            mCount = itemView.findViewById(R.id.count);

//            itemView.setOnClickListener(view -> {
//                Intent intent = new Intent(mContext, ShoppingItemActivity.class);
//                intent.putExtra("item", mCartItems.get(getAdapterPosition()));
//                mContext.startActivity(intent);
//            });
        }

        void bindTo(CartItem currentItem){
            FirebaseFirestore.getInstance().collection("Phones").whereEqualTo("name", currentItem.getName()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        ShoppingItem item = doc.toObject(ShoppingItem.class);
                        mTitleText.setText(item.getName());

                        String priceStr = item.getPrice();
                        int price = Integer.parseInt(priceStr.replace(" Ft", "").replace(" ", ""));
                        int count = currentItem.getCount();

                        mPriceText.setText(String.format("%,d Ft", price * count).replace(",", " "));
                        mCount.setText(String.valueOf(count) + " db");
                        Glide.with(mContext).load(item.getImageResource()).into(mItemImage);
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            });
        }
    }
}