package factory;

import factory.products.Car;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class Dealer implements Runnable {
    public Dealer(Storage<Car> storage, long millisStartDelay, long millisInterval) {
        if (millisInterval <= 0 || millisStartDelay <= 0) {
            throw new IllegalArgumentException("bad time intervals");
        }
        DealerStorage = storage;
        StartDelay = millisStartDelay;
        Interval.set(millisInterval);
        DealerThread = new Thread(this);
        DealerThread.start();
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
            DealerThread.interrupt();
        }
        while (!DealerThread.isInterrupted()) {
            try {
                LOGGER.info(DealerStorage.get().toString());
                Thread.sleep(Interval.get());
            } catch (InterruptedException e) {
                DealerThread.interrupt();
            }
        }
    }

    public void stop() {
        DealerThread.interrupt();
    }

    private static final Logger LOGGER = Logger.getLogger("Factory");
    private final Storage<Car> DealerStorage;
    private final Thread DealerThread;
    private final long StartDelay;
    private final AtomicLong Interval = new AtomicLong();
}
