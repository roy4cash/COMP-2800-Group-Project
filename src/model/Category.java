/**
 * File: Category.java
 * Purpose: Plain model object representing one expense category.
 *
 * Category objects are stored directly in combo boxes so the UI can keep the
 * display name and database ID together.
 */
package model;

/**
 * Represents an expense category (e.g., Food, Transport).
 * toString() is used by JComboBox to display the name.
 */
public class Category {

    private final int    id;
    private final String name;

    /** Creates a category object from a database row or fallback seed data. */
    public Category(int id, String name) {
        this.id   = id;
        this.name = name;
    }

    /** Returns the category ID used in expense inserts. */
    public int    getId()   { return id; }
    /** Returns the category name shown in the UI. */
    public String getName() { return name; }

    @Override
    /** Returns the display label so Swing components can render the name directly. */
    public String toString() { return name; }
}
