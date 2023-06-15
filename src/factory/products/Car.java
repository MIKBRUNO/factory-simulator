package factory.products;

public class Car extends Product {
    public Car(Body body, Engine engine, Accessory accessory) {
        if (body == null || engine == null || accessory == null) {
            throw new IllegalArgumentException("can not create Car with null details");
        }
        CarBody = body;
        CarEngine = engine;
        CarAccessory = accessory;
    }

    @Override
    public String toString() {
        return super.toString() + "(" + CarEngine + "; " + CarBody + "; " + CarAccessory + ")";
    }

    @Override
    protected String productType() {
        return "Car";
    }

    private final Body CarBody;
    private final Engine CarEngine;
    private final Accessory CarAccessory;
}
