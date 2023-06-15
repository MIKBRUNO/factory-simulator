package factory;

import events.EventObserver;
import events.EventPublisher;
import events.ListenerRegister;
import factory.products.Product;

import java.util.LinkedList;
import java.util.Queue;

// like BlockingQueue
public class Storage<T extends Product> implements ListenerRegister<StorageEvent> {
    public Storage(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Storage capacity must be more than zero");
        }
        Capacity = capacity;
    }

    public synchronized void put(T product) throws InterruptedException {
        if (product == null) {
            throw new IllegalArgumentException("null product");
        }
        while (Objects.size() >= Capacity) {
            wait();
        }
        if (!Objects.offer(product)) {
            throw new RuntimeException("storage underlying container error (can not put product)");
        }
        ++AllTimeCount;
        publisher.publishEvent(new StorageEvent(this, StorageEvent.State.PUT));
        notifyAll();
    }

    public synchronized T get() throws InterruptedException {
        while (Objects.isEmpty()) {
            wait();
        }
        T product = Objects.poll();
        publisher.publishEvent(new StorageEvent(this, StorageEvent.State.GET));
        notifyAll();
        return product;
    }

    public synchronized int getCurrentCount() {
        return Objects.size();
    }

    public synchronized int getCapacity() {
        return Capacity;
    }

    @Override
    public void addListener(EventObserver<StorageEvent> listener) {
        publisher.addListener(listener);
    }

    private int AllTimeCount = 0;
    private final EventPublisher<StorageEvent> publisher = new EventPublisher<>();
    private final int Capacity;
    private final Queue<T> Objects = new LinkedList<>();
}
