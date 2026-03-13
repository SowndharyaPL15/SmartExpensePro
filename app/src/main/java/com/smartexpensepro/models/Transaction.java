package com.smartexpensepro.models;

public class Transaction {
    private int id;
    private double amount;
    private String category;
    private String description;
    private String date;
    private int userId;
    private boolean isAutoDetected;

    public Transaction() {}

    public Transaction(double amount, String category, String description, String date, int userId, boolean isAutoDetected) {
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
        this.userId = userId;
        this.isAutoDetected = isAutoDetected;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public boolean isAutoDetected() { return isAutoDetected; }
    public void setAutoDetected(boolean autoDetected) { isAutoDetected = autoDetected; }
}
