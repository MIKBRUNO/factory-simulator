import events.EventObserver;
import factory.StorageEvent;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class GUIStorageListener implements EventObserver<StorageEvent> {
    public GUIStorageListener(Consumer<String> current, Consumer<String> allTime) {
        this.current = current;
        this.allTime = allTime;
    }

    @Override
    public void onEvent(StorageEvent state) {
        if (state.state() == StorageEvent.State.PUT) {
            AllTimeCount.incrementAndGet();
        }
        current.accept(String.valueOf(state.storage().getCurrentCount()));
        allTime.accept(String.valueOf(AllTimeCount.get()));
    }

    private final AtomicInteger AllTimeCount = new AtomicInteger(0);
    private final Consumer<String> current;
    private final Consumer<String> allTime;
}