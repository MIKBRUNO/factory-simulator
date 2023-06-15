package events;

public interface EventObserver<Event> {
    void onEvent(Event event);
}
