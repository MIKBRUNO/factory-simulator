package factory;

import factory.products.Accessory;
import factory.products.Body;
import factory.products.Car;
import factory.products.Engine;
import threadpool.InterruptibleTask;

public class WorkerTask implements InterruptibleTask {
    public WorkerTask(
            Storage<Car> carStorage,
            Storage<Body> bodyStorage,
            Storage<Engine> engineStorage,
            Storage<Accessory> accessoryStorage
    ) {

        CarStorage = carStorage;
        BodyStorage = bodyStorage;
        EngineStorage = engineStorage;
        AccessoryStorage = accessoryStorage;
    }

    @Override
    public void run() throws InterruptedException {
        Body body = BodyStorage.get();
        Engine engine = EngineStorage.get();
        Accessory accessory = AccessoryStorage.get();
        Car car = new Car(body, engine, accessory);
        CarStorage.put(car);
    }

    private final Storage<Car> CarStorage;
    private final Storage<Body> BodyStorage;
    private final Storage<Engine> EngineStorage;
    private final Storage<Accessory> AccessoryStorage;

}
