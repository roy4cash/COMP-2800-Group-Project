/**
 * File: Subject.java
 * Purpose: Defines the subject contract used by ExpenseManager.
 */
package observer;

/**
 * Subject interface — any class that produces events
 * other components want to listen to implements this.
 */
public interface Subject {
    /** Adds a new observer to the notification list. */
    void addObserver(Observer o);
    /** Removes an observer that no longer needs updates. */
    void removeObserver(Observer o);
    /** Pushes a refresh event to all currently registered observers. */
    void notifyObservers();
}
