package factory.products;

import java.util.UUID;

public abstract class Product {
    public Product() {
        ID = UUID.randomUUID();
    }

    @Override
    public String toString() {
        return productType() + ": <" + ID + ">";
    }

    protected abstract String productType();

    private final UUID ID;
}
