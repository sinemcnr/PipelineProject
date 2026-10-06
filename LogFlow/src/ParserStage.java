import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common Log Format (CLF) satirlarini LogRecord nesnesine donusturur.
 *
 * Bu artimda hatali satirlar atlanir ve sayilir.
 * Hatalarin daha ayrintili ele alinmasi ilerleyen
 * artimlarda gelistirilecektir.
 */
public class ParserStage implements Stage<String, LogRecord> {

    /*
     * Ornek desteklenen satir:
     *
     * 127.0.0.1 - - [29/Sep/2026:10:15:01]
     * "GET /index.html HTTP/1.1" 200 512
     *
     * Opsiyonel olarak satirin sonunda tirnak icinde
     * User-Agent bilgisi de bulunabilir.
     */
    private static final Pattern LOG_PATTERN = Pattern.compile(
            "^(\\S+)\\s+\\S+\\s+\\S+\\s+\\[([^\\]]+)]\\s+\"(\\S+)\\s+([^\\s\"]+)(?:\\s+[^\"]+)?\"\\s+(\\S+)\\s+(\\S+)(?:\\s+\"([^\"]*)\")?\\s*$"
    );

    /*
     * access-small.log dosyasinda zaman dilimi bilgisi
     * bulunmadigi icin tarih bu formata gore okunur.
     */
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                    "dd/MMM/yyyy:HH:mm:ss",
                    Locale.ENGLISH
            );

    private int errorCount = 0;

    @Override
    public void process(String input, Emitter<LogRecord> out)
            throws StageException {

        /*
         * Bos veya null satir gecersiz kabul edilir.
         * Bu hafta hatali satirlar yalnizca atlanir ve sayilir.
         */
        if (input == null || input.trim().isEmpty()) {
            errorCount++;
            return;
        }

        Matcher matcher = LOG_PATTERN.matcher(input);

        /*
         * Satir beklenen log bicimine uymuyorsa
         * kayit uretilmeden atlanir.
         */
        if (!matcher.matches()) {
            errorCount++;
            return;
        }

        try {

            String clientIp = matcher.group(1);

            /*
             * Log dosyasinda timezone bilgisi bulunmadigi icin
             * UTC varsayimi kullanilmistir.
             */
            Instant timestamp = LocalDateTime.parse(
                    matcher.group(2),
                    DATE_FORMAT
            ).toInstant(ZoneOffset.UTC);

            String method = matcher.group(3);

            String path = matcher.group(4);

            int status = Integer.parseInt(
                    matcher.group(5)
            );

            String bytesText = matcher.group(6);

            long bytes;

            if ("-".equals(bytesText)) {
                bytes = 0L;
            } else {
                bytes = Long.parseLong(bytesText);
            }

            String userAgent = matcher.group(7);

            /*
             * User-Agent bilgisi bulunmayan standart
             * CLF satirlarinda bos String kullanilir.
             */
            if (userAgent == null) {
                userAgent = "";
            }

            /*
             * Ayrıştırilan bilgiler immutable LogRecord
             * nesnesine aktarilir.
             *
             * attributes ilerleyen haftalarda yeni verilerin
             * eklenebilmesi icin genisleme noktasidir.
             */
            LogRecord record = new LogRecord(
                    timestamp,
                    clientIp,
                    method,
                    path,
                    status,
                    bytes,
                    userAgent,
                    Collections.emptyMap(),
                    input
            );

            /*
             * Basarili kayit pipeline'in sonraki
             * asamasina gonderilir.
             */
            out.emit(record);

        } catch (Exception e) {

            /*
             * Tarih, status veya bytes gibi alanlar
             * ayrıştırılamazsa satir hatali kabul edilir.
             */
            errorCount++;
        }
    }

    /**
     * Ayrıştırma sırasında atlanan toplam
     * hatali satir sayisini dondurur.
     */
    public int getErrorCount() {
        return errorCount;
    }
}