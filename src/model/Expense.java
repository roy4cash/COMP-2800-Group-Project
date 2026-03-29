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

    public int       getId()           { return id; }
    public int       getUserId()       { return userId; }
    public int       getCategoryId()   { return categoryId; }
    public String    getCategoryName() { return categoryName; }
    public String    getDescription()  { return description; }
    public double    getAmount()       { return amount; }
    public LocalDate getDate()         { return date; }
}
