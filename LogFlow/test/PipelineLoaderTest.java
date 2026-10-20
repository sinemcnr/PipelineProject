import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PipelineLoaderTest {

    private static int passed = 0;
    private static int failed = 0;

    private static final List<Path> temporaryFiles =
            new ArrayList<>();

    public static void main(String[] args) {

        System.out.println("=== LogFlow PipelineLoader Testleri ===");

        runTest("Gecerli yapilandirma",
                PipelineLoaderTest::testValidConfig);

        runTest("Bilinmeyen asama turu",
                PipelineLoaderTest::testUnknownStage);

        runTest("Eksik parametre",
                PipelineLoaderTest::testMissingParameter);

        runTest("Yanlis parametre turu",
                PipelineLoaderTest::testWrongType);

        runTest("Bos pipeline",
                PipelineLoaderTest::testEmptyPipeline);

        cleanup();

        System.out.println();
        System.out.println("Basarili test: " + passed);
        System.out.println("Basarisiz test: " + failed);

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testValidConfig() throws Exception {

        PipelineLoader loader = new PipelineLoader();

        Pipeline pipeline =
                loader.load("configs/error-report.yaml");

        if (pipeline == null) {
            throw new AssertionError(
                    "Pipeline olusturulamadi."
            );
        }

        pipeline.run();
    }

    private static void testUnknownStage() throws Exception {

        String yaml = validYaml()
                .replace("type: filter", "type: unknown");

        expectConfigError(yaml, "Bilinmeyen asama turu");
    }

    private static void testMissingParameter() throws Exception {

        String yaml = validYaml()
                .replace("value: 400", "");

        expectConfigError(yaml, "Zorunlu parametre eksik");
    }

    private static void testWrongType() throws Exception {

        String yaml = validYaml()
                .replace("value: 400", "value: abc");

        expectConfigError(yaml, "tam sayi olmali");
    }

    private static void testEmptyPipeline() throws Exception {

        String yaml = validYaml().replaceFirst(
                "(?ms)^  stages:.*?(?=^  sink:)",
                "  stages: []\n\n"
        );

        expectConfigError(yaml, "Pipeline bos olamaz");
    }

    private static String validYaml() throws Exception {

        return Files.readString(
                Path.of("configs/error-report.yaml")
        );
    }

    private static void expectConfigError(
            String yaml,
            String expectedMessage) throws Exception {

        Path temp = Files.createTempFile(
                "logflow-test-", ".yaml"
        );

        temporaryFiles.add(temp);
        Files.writeString(temp, yaml);

        try {

            new PipelineLoader().load(temp.toString());

            throw new AssertionError(
                    "Beklenen yapilandirma hatasi olusmadi."
            );

        } catch (IllegalArgumentException e) {

            if (e.getMessage() == null ||
                    !e.getMessage().contains(expectedMessage)) {

                throw new AssertionError(
                        "Beklenmeyen hata: " + e.getMessage()
                );
            }
        }
    }

    private static void runTest(
            String name,
            TestAction action) {

        try {

            action.run();

            passed++;

            System.out.println("[PASS] " + name);

        } catch (Exception | AssertionError e) {

            failed++;

            System.out.println("[FAIL] " + name);
            System.out.println("       " + e.getMessage());
        }
    }

    private static void cleanup() {

        for (Path path : temporaryFiles) {
            try {
                Files.deleteIfExists(path);
            } catch (Exception ignored) {
                System.err.println(
                    "Gecici dosya silinemedi: " + path
                );
            }
        }
    }

    @FunctionalInterface
    private interface TestAction {
        void run() throws Exception;
    }
}