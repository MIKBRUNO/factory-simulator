package factory.products;

import java.util.UUID;

public class Engine extends Product {
    @Override
    protected String productType() {
        return "Engine";
    }
}
