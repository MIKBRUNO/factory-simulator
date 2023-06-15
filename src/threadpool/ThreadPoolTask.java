package threadpool;

import events.EventObserver;
import events.EventPublisher;
import events.ListenerRegister;

public class ThreadPoolTask<T extends InterruptibleTask> implements ListenerRegister<TaskEvent<T>> {
    public ThreadPoolTask(T task) {
        AssociatedTask = task;
    }

    public void perform() {
        publisher.publishEvent(new TaskEvent<>(TaskEvent.State.STARTED, AssociatedTask));
        try {
            AssociatedTask.run();
        } catch (InterruptedException e) {
            publisher.publishEvent(new TaskEvent<>(TaskEvent.State.INTERRUPTED, AssociatedTask));
            Thread.currentThread().interrupt();
        }
        publisher.publishEvent(new TaskEvent<>(TaskEvent.State.FINISHED, AssociatedTask));
    }

    @Override
    public void addListener(EventObserver<TaskEvent<T>> listener) {
        publisher.addListener(listener);
    }

    private final T AssociatedTask;
    private final EventPublisher<TaskEvent<T>> publisher = new EventPublisher<>();
}
