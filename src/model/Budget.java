package model;

/**
 * Represents a user's budget for a specific month and year.
 */
public class Budget {

    private final int id;
    private final int userId;
    private final int month;
    private final int year;
    private double amount;

    public Budget(int id, int userId, int month, int year, double amount) {
        this.id     = id;
        this.userId = userId;
        this.month  = month;
        this.year   = year;
        this.amount = amount;
    }

    public int    getId()     { return id; }
    public int    getUserId() { return userId; }
    public int    getMonth()  { return month; }
    public int    getYear()   { return year; }
    public double getAmount() { return amount; }

    public void setAmount(double amount) { this.amount = amount; }
}
