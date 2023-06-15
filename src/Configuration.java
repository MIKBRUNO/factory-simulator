import java.io.InputStream;
import java.util.*;

public class Configuration {
    public interface Parser<T> {
        T parse(String s);
    }

    public void readConfiguration(InputStream configurationStream) {
        Scanner scanner = new Scanner(configurationStream);
        while (scanner.hasNext()) {
            String[] pair = scanner.nextLine().split("\\s*=\\s*");
            if (pair.length < 2)
                throw new RuntimeException("bad configuration stream");
            Fields.put(pair[0].toLowerCase(), pair[1]);
        }
    }

    public String getFieldValue(String field) {
        return Fields.get(field.toLowerCase());
    }

    public <T> T getFieldParsed(String field, Parser<T> parser) {
        String value = getFieldValue(field.toLowerCase());
        if (value == null) {
            return null;
        }
        else {
            return parser.parse(value);
        }
    }

    private final Map<String, String> Fields = Collections.synchronizedMap(new HashMap<>());
}
