package threadpool;

public interface InterruptibleTask {
    void run() throws InterruptedException;
}
