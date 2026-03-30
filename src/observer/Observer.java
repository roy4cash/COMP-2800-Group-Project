/**
 * File: Observer.java
 * Purpose: Defines the observer contract used by refreshable UI components.
 */
package observer;

/**
 * Observer interface — any UI panel that needs to react
 * to data changes implements this.
 */
public interface Observer {
    /** Called by the subject when the observer should reload its display state. */
    void update();
}
