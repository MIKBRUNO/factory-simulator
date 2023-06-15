package events;

public interface ListenerRegister<Event> {
    void addListener(EventObserver<Event> listener);
}
