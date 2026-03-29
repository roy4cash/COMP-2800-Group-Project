package model;

/**
 * Represents an expense category (e.g., Food, Transport).
 * toString() is used by JComboBox to display the name.
 */
public class Category {

    private final int    id;
    private final String name;

    public Category(int id, String name) {
        this.id   = id;
        this.name = name;
    }

    public int    getId()   { return id; }
    public String getName() { return name; }

    @Override
    public String toString() { return name; }
}
