package com.example.mobilalk;

import java.io.Serializable;

public class ShoppingItem implements Serializable {
    private String id;

    private String name;
    private String info;
    private String desc;
    private String price;
    private int imageResource;

    public ShoppingItem() {
    }

    public ShoppingItem(String name, String info, String desc, String price, int imageResource) {
        this.name = name;
        this.info = info;
        this.desc = desc;
        this.price = price;
        this.imageResource = imageResource;
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

    public String __getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }
}
