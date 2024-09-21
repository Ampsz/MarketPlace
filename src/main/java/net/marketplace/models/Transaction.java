package net.marketplace.models;

public class Transaction {

    private final String itemName;
    private final double price;
    private final String date;

    public Transaction(String itemName, double price, String date) {
        this.itemName = itemName;
        this.price = price;
        this.date = date;
    }

    public String getItemName() {
        return itemName;
    }

    public double getPrice() {
        return price;
    }

    public String getDate() {
        return date;
    }
}
