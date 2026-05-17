package KP_TOURS.backup;

import KP_TOURS.db.DBConnection;
import KP_TOURS.util.LoggerUtil;
import javafx.application.Platform;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;

public class AutoBackupScheduler {

    private static final LocalTime BACKUP_TIME =
            LocalTime.of(00, 0); // 12:00 PM

    private static Timer timer;

    public static void start() {

        try {

            runMissedBackupIfRequired();

            scheduleNextBackup();

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed while starting auto backup scheduler"
            );
        }
    }

    private static void runMissedBackupIfRequired() {

        LocalDate today =
                LocalDate.now();

        LocalTime now =
                LocalTime.now();

        if (now.isAfter(BACKUP_TIME)
                && !todayBackupExists(today)) {

            BackupManager.createAutoBackup();

            LoggerUtil.logInfo(
                    "Missed daily backup created for: " + today
            );
        }
    }

    private static void scheduleNextBackup() {

        if (timer != null) {
            timer.cancel();
        }

        timer =
                new Timer(true);

        LocalDateTime now =
                LocalDateTime.now();

        LocalDateTime nextRun =
                LocalDateTime.of(
                        LocalDate.now(),
                        BACKUP_TIME
                );

        if (now.isAfter(nextRun)) {

            nextRun =
                    nextRun.plusDays(1);
        }

        long delay =
                Duration.between(
                        now,
                        nextRun
                ).toMillis();

        timer.schedule(
                new TimerTask() {

                    @Override
                    public void run() {

                        Platform.runLater(() -> {

                            try {

                                if (!todayBackupExists(LocalDate.now())) {

                                    BackupManager.createAutoBackup();

                                    LoggerUtil.logInfo(
                                            "Daily backup created automatically"
                                    );
                                }

                                scheduleNextBackup();

                            } catch (Exception e) {

                                LoggerUtil.logError(
                                        e,
                                        "Failed while running daily backup"
                                );
                            }
                        });
                    }
                },
                delay
        );
    }

    private static boolean todayBackupExists(
            LocalDate date
    ) {

        File backupDirectory =
                new File(
                        DBConnection.getBackupDirectory()
                );

        if (!backupDirectory.exists()) {
            return false;
        }

        File[] files =
                backupDirectory.listFiles();

        if (files == null) {
            return false;
        }

        String todayPrefix =
                "auto_backup_" + date;

        for (File file : files) {

            if (file.getName().startsWith(todayPrefix)
                    && file.getName().endsWith(".zip")) {

                return true;
            }
        }

        return false;
    }
}