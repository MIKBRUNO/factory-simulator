package factory;

public record StorageEvent(Storage<?> storage, State state) {
    public enum State {
        PUT,
        GET
    }
}
