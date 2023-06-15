package factory;

import factory.products.Product;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class Producer<T extends Product> implements Runnable {
    public Producer(Storage<T> storage, long millisStartDelay, long millisInterval, Supplier<T> builder) {
        if (millisInterval <= 0 || millisStartDelay <= 0) {
            throw new IllegalArgumentException("bad time intervals");
        }
        Builder = builder;
        ProducerStorage = storage;
        StartDelay = millisStartDelay;
        Interval.set(millisInterval);
        ProducerThread = new Thread(this);
        ProducerThread.start();
    }

    public void setInterval(long newInterval) {
        if (newInterval <= 0) {
            throw new IllegalArgumentException("bad time interval");
        }
        Interval.set(newInterval);
    }

    @Override
    public void run() {
        try {
            Thread.sleep(StartDelay);
        } catch (InterruptedException e) {
            ProducerThread.interrupt();
        }
        while (!ProducerThread.isInterrupted()) {
            try {
                ProducerStorage.put(Builder.get());
                Thread.sleep(Interval.get());
            } catch (InterruptedException e) {
                ProducerThread.interrupt();
            }
        }
    }

    public void stop() {
        ProducerThread.interrupt();
    }

    private final Supplier<T> Builder;
    private final Storage<T> ProducerStorage;
    private final Thread ProducerThread;
    private final long StartDelay;
    private final AtomicLong Interval = new AtomicLong();
}
