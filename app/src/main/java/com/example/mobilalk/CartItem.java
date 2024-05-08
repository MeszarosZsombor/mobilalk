package com.example.mobilalk;

public class CartItem {
    private String name;
    private int count;
    private int price;

    public CartItem() {
    }

    public CartItem(String name, int count, int price) {
        this.name = name;
        this.count = count;
        this.price = price;
    }

    public String getName() {
        return this.name;
    }

    public int getCount() {
        return this.count;
    }

    public int getPrice(){
        return this.price;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int sum(){
        return this.price * this.count;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "phoneName='" + name + '\'' +
                ", count=" + count +
                ", price=" + price + '}';
    }
}
