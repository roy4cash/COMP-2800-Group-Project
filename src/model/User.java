package model;

/**
 * Represents a user of the application.
 * Kept simple — only one user exists in this version.
 */
public class User {

    private final int    id;
    private final String username;

    public User(int id, String username) {
        this.id       = id;
        this.username = username;
    }

    public int    getId()       { return id; }
    public String getUsername() { return username; }
}
