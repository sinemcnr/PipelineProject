import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

        if (args.length == 0) {
            printUsage();
            return;
        }

        try {
            // 4. aşama: YAML yapılandırmasıyla çalıştırma
            if ("--config".equals(args[0])) {

                if (args.length != 2) {
                    throw new IllegalArgumentException(
                        "Kullanim: java -cp out Main --config <dosya.yaml>"
                    );
                }

                PipelineLoader loader = new PipelineLoader();
                Pipeline pipeline = loader.load(args[1]);

                pipeline.run();

                System.out.println(
                    "Yapilandirma basariyla yuklendi: " + args[1]
                );
                System.out.println("Pipeline basariyla calisti.");
                return;
            }

            // 3. aşama: Eski komut satırı desteği
            runLegacy(args);

        } catch (IllegalArgumentException e) {
            System.err.println(
                "Yapilandirma/parametre hatasi: " + e.getMessage()
            );
        } catch (RuntimeException e) {
            System.err.println(
                "Calistirma hatasi: " + e.getMessage()
            );
        }
    }

    private static void runLegacy(String[] args) {

        String inputFile = args[0];
        String outputFile = "output.log";

        Integer minStatus = null;
        String pathPrefix = null;

        String[] fields = {
            "timestamp", "clientIp", "method",
            "path", "status", "bytes"
        };

        for (int i = 1; i < args.length; i++) {

            switch (args[i]) {

                case "--min-status":
                    minStatus = Integer.parseInt(
                        requireValue(args, ++i)
                    );
                    break;

                case "--path-prefix":
                    pathPrefix = requireValue(args, ++i);
                    break;

                case "--fields":
                    fields = Arrays.stream(
                        requireValue(args, ++i).split(",")
                    ).map(String::trim)
                     .toArray(String[]::new);
                    break;

                case "--output":
                    outputFile = requireValue(args, ++i);
                    break;

                default:
                    throw new IllegalArgumentException(
                        "Bilinmeyen parametre: " + args[i]
                    );
            }
        }

        ParserStage parser = new ParserStage();

        PipelineBuilder<LogRecord> builder =
            PipelineBuilder
                .from(new FileLineSource(inputFile))
                .via(parser);

        if (minStatus != null) {
            final int threshold = minStatus;

            builder = builder.via(
                new PredicateFilterStage(
                    r -> r.status() >= threshold
                )
            );
        }

        if (pathPrefix != null) {
            final String prefix = pathPrefix;

            builder = builder.via(
                new PredicateFilterStage(
                    r -> r.path().startsWith(prefix)
                )
            );
        }

        Pipeline pipeline = builder
            .via(new ProjectionStage(fields))
            .to(new FileSink<>(outputFile));

        pipeline.run();

        System.out.println(
            "Islem tamamlandi. Cikti: " + outputFile
        );

        System.out.println(
            "Toplam hatali satir sayisi: "
                + parser.getErrorCount()
        );
    }

    private static String requireValue(
        String[] args, int index
    ) {
        if (index >= args.length ||
            args[index].startsWith("--")) {

            throw new IllegalArgumentException(
                "Parametre degeri eksik."
            );
        }

        return args[index];
    }

    private static void printUsage() {

        System.out.println("LogFlow - Pipeline Sistemi");

        System.out.println(
            "YAML: java -cp out Main --config configs/error-report.yaml"
        );

        System.out.println(
            "Eski kullanim: java -cp out Main <log-dosyasi> "
            + "[--min-status 400] "
            + "[--path-prefix /api] "
            + "[--fields timestamp,clientIp,status] "
            + "[--output output.log]"
        );
    }
}