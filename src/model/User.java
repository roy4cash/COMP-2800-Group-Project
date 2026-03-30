/**
 * File: User.java
 * Purpose: Plain model object representing an application user.
 *
 * The current project uses a single default user, but keeping a User model
 * makes the schema and foreign-key relationships easier to understand.
 */
package model;

/**
 * Represents a user of the application.
 * Kept simple — only one user exists in this version.
 */
public class User {

    private final int    id;
    private final String username;

    /** Creates a simple user object used to mirror the users table. */
    public User(int id, String username) {
        this.id       = id;
        this.username = username;
    }

    /** Returns the user's database ID. */
    public int    getId()       { return id; }
    /** Returns the username stored for the local session. */
    public String getUsername() { return username; }
}
