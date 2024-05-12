package com.example.mobilalk;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ShoppingItem implements Serializable {
    private String name;
    private String info;
    private String desc;
    private String price;
    private int imageResource;
    private int count;

    public ShoppingItem() {
    }

    public ShoppingItem(String name, String info, String desc, String price, int imageResource, int count) {
        this.name = name;
        this.info = info;
        this.desc = desc;
        this.price = price;
        this.imageResource = imageResource;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public String getInfo() {
        return info;
    }

    public String getDesc() {
        return desc;
    }

    public String getPrice() {
        return price;
    }

    public int getImageResource() {
        return imageResource;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int i) {
        this.count = i;
    }
}
