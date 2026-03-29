package observer;

/**
 * Subject interface — any class that produces events
 * other components want to listen to implements this.
 */
public interface Subject {
    void addObserver(Observer o);
    void removeObserver(Observer o);
    void notifyObservers();
}
