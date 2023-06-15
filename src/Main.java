import events.EventObserver;
import factory.*;
import factory.products.Accessory;
import factory.products.Body;
import factory.products.Car;
import factory.products.Engine;
import threadpool.ThreadPool;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.*;
import java.util.stream.Stream;

public class Main {
    private static final String LOGGER_FILE = "sales-log.txt";
    private static final String CONFIG_FILE = "config.txt";
    public static final Logger LOGGER = Logger.getLogger("Factory");
    static {
        LOGGER.setUseParentHandlers(false);
    }

    public static void main(String[] args) throws InterruptedException {
        JFrame frame = new JFrame("Factory Sim");
        /* Reading configurations */

        String configFile;
        if (args.length < 2) {
            configFile = CONFIG_FILE;
        }
        else {
            configFile = args[1];
        }
        int BODY_STORAGE_SIZE;
        int ENGINE_STORAGE_SIZE;
        int ACCESSORY_STORAGE_SIZE;
        int DEALER_COUNT;
        int WORKER_COUNT;
        int ACCESSORY_PRODUCER_COUNT;
        int CAR_STORAGE_SIZE;
        boolean LOGGING;
        try (InputStream configStream = Main.class.getResourceAsStream(configFile)) {
            if (configStream == null) {
                JOptionPane.showMessageDialog(frame, "Config file " + configFile + " not found");
                System.exit(1);
                return;
            }
            Configuration configuration = new Configuration();
            configuration.readConfiguration(configStream);
            BODY_STORAGE_SIZE = configuration.getFieldParsed("StorageBodySize", Integer::parseInt);
            ENGINE_STORAGE_SIZE = configuration.getFieldParsed("StorageEngineSize", Integer::parseInt);
            ACCESSORY_STORAGE_SIZE = configuration.getFieldParsed("StorageAccessorySize", Integer::parseInt);
            CAR_STORAGE_SIZE = configuration.getFieldParsed("StorageCarSize", Integer::parseInt);
            DEALER_COUNT = configuration.getFieldParsed("Dealers", Integer::parseInt);
            WORKER_COUNT = configuration.getFieldParsed("Workers", Integer::parseInt);
            ACCESSORY_PRODUCER_COUNT = configuration.getFieldParsed("AccessoryProducers", Integer::parseInt);
            LOGGING = configuration.getFieldParsed("LogSale", Boolean::parseBoolean);
        } catch (NullPointerException | IOException e) {
            JOptionPane.showMessageDialog(frame, "Error while reading config file " + configFile);
            System.exit(1);
            return;
        }

        /* setting LOGGER */

        if (!LOGGING) {
            LOGGER.setLevel(Level.OFF);
        } else {
            String logger_file = (args.length >= 3) ? args[2] : LOGGER_FILE;
            try {
                FileHandler fh = new FileHandler(logger_file);
                LOGGER.addHandler(fh);
                Formatter formatter = new Formatter() {
                    @Override
                    public String format(LogRecord record) {
                        LocalTime time = LocalTime.ofInstant(record.getInstant(), ZoneId.systemDefault());
                        DateTimeFormatter dt = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);
                        return "<" + time.format(dt) + ">: " + record.getMessage() + "\n";
                    }
                };
                fh.setFormatter(formatter);

            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "bad logging file " + logger_file);
                System.exit(1);
                return;
            }
        }

        /* Creating Storages */

        Storage<Engine> engineStorage = new Storage<>(ENGINE_STORAGE_SIZE);
        Storage<Accessory> accessoryStorage = new Storage<>(ACCESSORY_STORAGE_SIZE);
        Storage<Body> bodyStorage = new Storage<>(BODY_STORAGE_SIZE);
        Storage<Car> carStorage = new Storage<>(CAR_STORAGE_SIZE);

        /* Creating Producers and Dealers */

        Producer<Engine> producerEngine = new Producer<>(engineStorage, 1000, 500, Engine::new);
        Producer<Body> producerBody = new Producer<>(bodyStorage, 1000, 500, Body::new);
        List<Producer<Accessory>> producersAccessory = Stream
                .generate(() -> new Producer<>(accessoryStorage, 1000, 500, Accessory::new))
                .limit(ACCESSORY_PRODUCER_COUNT)
                .toList();
        List<Dealer> dealers = Stream
                .generate(() -> new Dealer(carStorage, 1000, 500))
                .limit(DEALER_COUNT)
                .toList();
        Function<Integer, Long> scale = i -> i * 100L + 100;
        FactoryGUI factoryGUI = new FactoryGUI(
                i -> dealers.forEach(dealer -> dealer.setInterval(scale.apply(i))),
                i -> producersAccessory.forEach(producerAccessory -> producerAccessory.setInterval(scale.apply(i))),
                i -> producerEngine.setInterval(scale.apply(i)),
                i -> producerBody.setInterval(scale.apply(i))
        );
        producerEngine.setInterval(scale.apply(factoryGUI.getENGINES().getValue()));
        producerBody.setInterval(scale.apply(factoryGUI.getBODIES().getValue()));
        producersAccessory.forEach(
                producerAccessory -> producerAccessory.setInterval(scale.apply(factoryGUI.getACCESSORIES().getValue()))
        );
        dealers.forEach(
                dealer -> dealer.setInterval(scale.apply(factoryGUI.getDEALERS().getValue()))
        );

        /* Creating Workers */

        ThreadPool pool = new ThreadPool(WORKER_COUNT);
        CarStorageController storageController = new CarStorageController(carStorage, pool, new WorkerTask(
                carStorage, bodyStorage,
                engineStorage, accessoryStorage
        ));

        /* Setting GUI to Listen for Storage and Workers changes */

        engineStorage.addListener(new GUIStorageListener(
                factoryGUI.ENGINES_IN_STORAGE::setText, factoryGUI.ENGINES_ALLTIME::setText)
        );
        accessoryStorage.addListener(new GUIStorageListener(
                factoryGUI.ACCESSORIES_IN_STORAGE::setText, factoryGUI.ACCESSORIES_ALL_TIME::setText)
        );
        bodyStorage.addListener(new GUIStorageListener(
                factoryGUI.BODIES_IN_STORAGE::setText, factoryGUI.BODIES_ALL_TIME::setText)
        );
        carStorage.addListener(new EventObserver<>() {
            private final AtomicInteger allTimeSaled = new AtomicInteger(0);

            @Override
            public void onEvent(StorageEvent storageEvent) {
                if (storageEvent.state() == StorageEvent.State.GET) {
                    allTimeSaled.incrementAndGet();
                }
                factoryGUI.getCARS_SALED().setText(allTimeSaled.toString());
                factoryGUI.getCARS_IN_STORAGE().setText(String.valueOf(storageEvent.storage().getCurrentCount()));
            }
        });
        storageController.addListener(new EventObserver<>() {
            final AtomicInteger allTime = new AtomicInteger(0);
            final AtomicInteger pending = new AtomicInteger(1);
            // there's always one initial task for car

            @Override
            public void onEvent(ControllerEvent controllerEvent) {
                if (controllerEvent == ControllerEvent.TASK_REQUEST) {
                    pending.incrementAndGet();
                } else if (controllerEvent == ControllerEvent.TASK_FINISHED) {
                    pending.decrementAndGet();
                    allTime.incrementAndGet();
                }
                factoryGUI.READY_CARS.setText(allTime.toString());
                factoryGUI.getCARS_TO_MAKE().setText(pending.toString());
            }
        });

        /* Stop all on window close */

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                producerEngine.stop();
                producerBody.stop();
                producersAccessory.forEach(Producer::stop);
                dealers.forEach(Dealer::stop);
                pool.stop();
                System.exit(0);
            }
        });

        frame.setContentPane(factoryGUI.getMainPanel());
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
