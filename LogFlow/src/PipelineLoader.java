import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class PipelineLoader {

    private final StageRegistry registry;

    public PipelineLoader() {

        registry = new StageRegistry();

        registerStages();
    }

    private void registerStages() {

        // PARSE
        registry.register("parse", params -> {

            String format = getString(params, "format");

            if (!format.equals("clf")) {
                throw new IllegalArgumentException(
                    "Desteklenmeyen log formati: " + format
                );
            }

            return new ParserStage();
        });

        // FILTER
        registry.register("filter", params -> {

            String field = getString(params, "field");
            String op = getString(params, "op");
            Object value = getRequired(params, "value");

            if (field.equals("status") && op.equals(">=")) {

                if (!(value instanceof Integer)) {
                    throw new IllegalArgumentException(
                        "status filtresi icin value tam sayi olmali."
                    );
                }

                int minStatus = (Integer) value;

                return new PredicateFilterStage(
                    r -> r.status() >= minStatus
                );
            }

            if (field.equals("path")
                    && op.equals("startsWith")) {

                if (!(value instanceof String)) {
                    throw new IllegalArgumentException(
                        "path filtresi icin value metin olmali."
                    );
                }

                String prefix = (String) value;

                return new PredicateFilterStage(
                    r -> r.path().startsWith(prefix)
                );
            }

            throw new IllegalArgumentException(
                "Desteklenmeyen filtre: " + field + " " + op
            );
        });

        // PROJECT
        registry.register("project", params -> {

            Object value = getRequired(params, "fields");

            if (!(value instanceof List<?>)) {
                throw new IllegalArgumentException(
                    "fields parametresi liste olmali."
                );
            }

            List<?> list = (List<?>) value;

            if (list.isEmpty()) {
                throw new IllegalArgumentException(
                    "fields listesi bos olamaz."
                );
            }

            List<String> fields = new ArrayList<>();

            for (Object item : list) {

                if (!(item instanceof String)
                        || ((String) item).isBlank()) {

                    throw new IllegalArgumentException(
                        "fields sadece metin alan adlari icermeli."
                    );
                }

                fields.add((String) item);
            }

            return new ProjectionStage(
                fields.toArray(new String[0])
            );
        });
    }

    public Pipeline load(String configPath) {

        Map<String, Object> root;

        // YAML dosyasini oku
        try (FileInputStream input =
                     new FileInputStream(configPath)) {

            Yaml yaml = new Yaml();

            root = asMap(
                yaml.load(input),
                "YAML kok nesnesi"
            );

        } catch (IOException e) {

            throw new IllegalArgumentException(
                "YAML dosyasi okunamadi: " + configPath,
                e
            );
        }

        Map<String, Object> config = asMap(
            getRequired(root, "pipeline"),
            "pipeline"
        );

        // SOURCE
        Map<String, Object> sourceConfig = asMap(
            getRequired(config, "source"),
            "source"
        );

        String sourceType =
            getString(sourceConfig, "type");

        if (!sourceType.equals("file")) {
            throw new IllegalArgumentException(
                "Desteklenmeyen source turu: " + sourceType
            );
        }

        Map<String, Object> sourceParams = asMap(
            getRequired(sourceConfig, "params"),
            "source.params"
        );

        String inputPath =
            getString(sourceParams, "path");

        // SINK
        Map<String, Object> sinkConfig = asMap(
            getRequired(config, "sink"),
            "sink"
        );

        String sinkType =
            getString(sinkConfig, "type");

        if (!sinkType.equals("file")) {
            throw new IllegalArgumentException(
                "Desteklenmeyen sink turu: " + sinkType
            );
        }

        Map<String, Object> sinkParams = asMap(
            getRequired(sinkConfig, "params"),
            "sink.params"
        );

        String outputPath =
            getString(sinkParams, "path");

        // STAGES
        Object stagesObject =
            getRequired(config, "stages");

        if (!(stagesObject instanceof List<?>)) {
            throw new IllegalArgumentException(
                "stages bir liste olmali."
            );
        }

        List<?> stageConfigs =
            (List<?>) stagesObject;

        if (stageConfigs.isEmpty()) {
            throw new IllegalArgumentException(
                "Pipeline bos olamaz."
            );
        }

        // Tum asamalari veri islenmeden once olustur
        List<Stage<?, ?>> stages = new ArrayList<>();

        boolean parserFound = false;
        boolean projectionFound = false;

        for (Object item : stageConfigs) {

            Map<String, Object> stageConfig =
                asMap(item, "stage");

            String type =
                getString(stageConfig, "type");

            Map<String, Object> params = asMap(
                getRequired(stageConfig, "params"),
                type + ".params"
            );

            if (!parserFound && !type.equals("parse")) {
                throw new IllegalArgumentException(
                    "Ilk asama parse olmali."
                );
            }

            if (type.equals("parse")) {

                if (parserFound) {
                    throw new IllegalArgumentException(
                        "Birden fazla parse asamasi olamaz."
                    );
                }

                parserFound = true;
            }

            if (projectionFound) {
                throw new IllegalArgumentException(
                    "Project asamasindan sonra "
                    + "yeni asama eklenemez."
                );
            }

            Stage<?, ?> stage =
                registry.create(type, params);

            stages.add(stage);

            if (type.equals("project")) {
                projectionFound = true;
            }
        }

        if (!parserFound) {
            throw new IllegalArgumentException(
                "Parse asamasi bulunamadi."
            );
        }

        // PipelineBuilder ile pipeline olustur
        PipelineBuilder<?> builder =
            PipelineBuilder.from(
                new FileLineSource(inputPath)
            );

        for (Stage<?, ?> stage : stages) {
            builder = appendStage(builder, stage);
        }

        return finishPipeline(builder, outputPath);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static PipelineBuilder<?> appendStage(
            PipelineBuilder<?> builder,
            Stage<?, ?> stage) {

        return ((PipelineBuilder) builder)
            .via((Stage) stage);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Pipeline finishPipeline(
            PipelineBuilder<?> builder,
            String outputPath) {

        return ((PipelineBuilder) builder)
            .to(new FileSink<>(outputPath));
    }

    private static Object getRequired(
            Map<String, Object> map,
            String key) {

        Object value = map.get(key);

        if (value == null) {
            throw new IllegalArgumentException(
                "Zorunlu parametre eksik: " + key
            );
        }

        return value;
    }

    private static String getString(
            Map<String, Object> map,
            String key) {

        Object value = getRequired(map, key);

        if (!(value instanceof String)
                || ((String) value).isBlank()) {

            throw new IllegalArgumentException(
                key + " metin olmali."
            );
        }

        return (String) value;
    }

    private static Map<String, Object> asMap(
            Object value,
            String name) {

        if (!(value instanceof Map<?, ?>)) {
            throw new IllegalArgumentException(
                name + " nesne olmali."
            );
        }

        Map<?, ?> raw = (Map<?, ?>) value;

        Map<String, Object> result =
            new java.util.LinkedHashMap<>();

        for (Map.Entry<?, ?> entry : raw.entrySet()) {

            if (!(entry.getKey() instanceof String)) {
                throw new IllegalArgumentException(
                    name + " anahtarlari metin olmali."
                );
            }

            result.put(
                (String) entry.getKey(),
                entry.getValue()
            );
        }

        return result;
    }
}