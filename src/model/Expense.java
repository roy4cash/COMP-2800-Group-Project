/**
 * File: Expense.java
 * Purpose: Plain model object representing a single expense record.
 *
 * The object includes both database IDs and display-ready category text so
 * the UI can render complete rows without extra lookups.
 */
package model;

import java.time.LocalDate;

/**
 * Represents a single expense entry.
 * categoryName is fetched via a JOIN in the DAO so the UI
 * does not need an extra lookup.
 */
public class Expense {

    private final int       id;
    private final int       userId;
    private final int       categoryId;
    private final String    categoryName;
    private final String    description;
    private final double    amount;
    private final LocalDate date;

    /** Creates a full expense object from joined expense/category query results. */
    public Expense(int id, int userId, int categoryId, String categoryName,
                   String description, double amount, LocalDate date) {
        this.id           = id;
        this.userId       = userId;
        this.categoryId   = categoryId;
        this.categoryName = categoryName;
        this.description  = description;
        this.amount       = amount;
        this.date         = date;
    }

    /** Returns the expense row's primary key. */
    public int       getId()           { return id; }
    /** Returns the user ID that owns this expense. */
    public int       getUserId()       { return userId; }
    /** Returns the category foreign key. */
    public int       getCategoryId()   { return categoryId; }
    /** Returns the user-friendly category name from the JOIN query. */
    public String    getCategoryName() { return categoryName; }
    /** Returns the expense description entered by the user. */
    public String    getDescription()  { return description; }
    /** Returns the amount spent. */
    public double    getAmount()       { return amount; }
    /** Returns the calendar date used for monthly reporting. */
    public LocalDate getDate()         { return date; }
}
