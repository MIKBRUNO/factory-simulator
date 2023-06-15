package events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventPublisher<Event> implements ListenerRegister<Event> {
    @Override
    public void addListener(EventObserver<Event> listener) {
        Listeners.add(listener);
    }

    public void publishEvent(Event event) {
        synchronized (Listeners) {
            for (var l : Listeners) {
                l.onEvent(event);
            }
        }
    }

    private final List<EventObserver<Event>> Listeners = Collections.synchronizedList(new ArrayList<>());
}
