package threadpool;

public record TaskEvent<T extends InterruptibleTask>(State state, T task) {
    public enum State {
        FINISHED,
        STARTED,
        INTERRUPTED
    }
}
