package KP_TOURS.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

public class LoggerUtil {

    private static final String LOG_DIR =
            System.getProperty("user.home")
                    + "/PrabalAppData/logs";

    private static final String LOG_FILE =
            LOG_DIR + "/app.log";

    static {

        try {

            Files.createDirectories(
                    Paths.get(LOG_DIR)
            );

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    // =========================================================
    // INFO LOG
    // =========================================================

    public static void logInfo(String message) {

        writeLog(
                "[INFO]",
                message,
                null
        );
    }

    // =========================================================
    // ERROR LOG
    // =========================================================

    public static void logError(
            Exception exception,
            String message
    ) {

        writeLog(
                "[ERROR]",
                message,
                exception
        );
    }

    // =========================================================
    // INTERNAL LOGGER
    // =========================================================

    private static void writeLog(
            String level,
            String message,
            Exception exception
    ) {

        try {

            String logMessage =
                    LocalDate.now()
                            + " "
                            + level
                            + " "
                            + message;

            if (exception != null) {

                logMessage +=
                        " | Exception: "
                                + exception.getMessage();
            }

            logMessage += System.lineSeparator();

            Files.writeString(
                    Path.of(LOG_FILE),
                    logMessage,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}
