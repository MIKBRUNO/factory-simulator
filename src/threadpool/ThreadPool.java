package threadpool;

import events.EventObserver;
import events.EventPublisher;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

public class ThreadPool {
    public ThreadPool(int threads) {
        Threads = Stream
                .generate(() -> new Thread(new ThreadPoolWorker(Tasks)))
                .limit(threads)
                .toList();
        Threads.forEach(Thread::start);
    }

    public int getPending() {
        return Tasks.size();
    }

    public <T extends InterruptibleTask> void addTask(T task) {
        ThreadPoolTask<T> poolTask = new ThreadPoolTask<>(task);
        Tasks.add(poolTask);
        publisher.publishEvent(new PoolEvent(getPending()));
    }

    public <T extends InterruptibleTask> void addTask(T task, EventObserver<TaskEvent<T>> observer) {
        ThreadPoolTask<T> poolTask = new ThreadPoolTask<>(task);
        poolTask.addListener(observer);
        Tasks.add(poolTask);
        publisher.publishEvent(new PoolEvent(getPending()));
    }

    public void stop() {
        Threads.forEach(Thread::interrupt);
    }

    private final EventPublisher<PoolEvent> publisher = new EventPublisher<>();
    private final List<Thread> Threads;
    private final BlockingQueue<ThreadPoolTask<?>> Tasks = new LinkedBlockingQueue<>();
}
