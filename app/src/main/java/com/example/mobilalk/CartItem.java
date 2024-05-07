package com.example.mobilalk;

public class CartItem {
    private String name;
    private int count;

    public CartItem() {
    }

    public CartItem(String name, int count) {
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return this.name;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
