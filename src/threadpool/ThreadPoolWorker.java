package threadpool;

import java.util.concurrent.BlockingQueue;

public class ThreadPoolWorker implements Runnable {
    public ThreadPoolWorker(BlockingQueue<ThreadPoolTask<?>> tasks) {
        Tasks = tasks;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Tasks.take().perform();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private final BlockingQueue<ThreadPoolTask<?>> Tasks;
}
