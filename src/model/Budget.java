/**
 * File: Budget.java
 * Purpose: Plain model object representing one monthly budget row.
 *
 * The project keeps this class intentionally lightweight so it can move
 * cleanly between the DAO layer, manager layer, and Swing UI.
 */
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

    /** Creates one monthly budget object from database or in-memory values. */
    public Budget(int id, int userId, int month, int year, double amount) {
        this.id     = id;
        this.userId = userId;
        this.month  = month;
        this.year   = year;
        this.amount = amount;
    }

    /** Returns the budget row's primary key. */
    public int    getId()     { return id; }
    /** Returns the owning user ID. */
    public int    getUserId() { return userId; }
    /** Returns the calendar month the budget applies to. */
    public int    getMonth()  { return month; }
    /** Returns the calendar year the budget applies to. */
    public int    getYear()   { return year; }
    /** Returns the stored budget amount. */
    public double getAmount() { return amount; }

    /** Updates the in-memory budget amount after a save or refresh. */
    public void setAmount(double amount) { this.amount = amount; }
}
