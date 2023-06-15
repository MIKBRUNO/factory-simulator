package factory;

import events.EventObserver;
import events.EventPublisher;
import events.ListenerRegister;
import factory.products.Car;
import threadpool.TaskEvent;
import threadpool.ThreadPool;

import java.util.function.Predicate;

public class CarStorageController implements ListenerRegister<ControllerEvent> {
    public CarStorageController(Storage<Car> storage, ThreadPool workers, WorkerTask task) {
        CarStorage = storage;
        Workers = workers;
        Task = task;
        CarStorage.addListener(this::onStorageEvent);
        onStorageEvent(null);
    }

    @Override
    public void addListener(EventObserver<ControllerEvent> listener) {
        publisher.addListener(listener);
    }

    private void onStorageEvent(StorageEvent event) {
        if (isNeededToRequest.test(CarStorage)) {
            publisher.publishEvent(ControllerEvent.TASK_REQUEST);
            Workers.addTask(Task, (taskEvent) -> {
                if (taskEvent.state() == TaskEvent.State.FINISHED) {
                    publisher.publishEvent(ControllerEvent.TASK_FINISHED);
                }
            });
        }
    }

    private final static Predicate<Storage<Car>> isNeededToRequest = (storage) ->
            storage.getCurrentCount() < .1 * storage.getCapacity();
    private final EventPublisher<ControllerEvent> publisher = new EventPublisher<>();
    private final ThreadPool Workers;
    private final WorkerTask Task;
    private final Storage<Car> CarStorage;
}
