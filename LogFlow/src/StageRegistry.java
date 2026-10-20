import java.util.HashMap;
import java.util.Map;

public class StageRegistry {

    private final Map<String, StageFactory> factories =
            new HashMap<>();

    public void register(String type, StageFactory factory) {

        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException(
                "Asama turu bos olamaz."
            );
        }

        if (factory == null) {
            throw new IllegalArgumentException(
                "StageFactory null olamaz."
            );
        }

        if (factories.containsKey(type)) {
            throw new IllegalArgumentException(
                "Asama zaten kayitli: " + type
            );
        }

        factories.put(type, factory);
    }

    public Stage<?, ?> create(
            String type,
            Map<String, Object> params) {

        StageFactory factory = factories.get(type);

        if (factory == null) {
            throw new IllegalArgumentException(
                "Bilinmeyen asama turu: " + type
            );
        }

        return factory.create(params);
    }

    public boolean contains(String type) {
        return factories.containsKey(type);
    }
}