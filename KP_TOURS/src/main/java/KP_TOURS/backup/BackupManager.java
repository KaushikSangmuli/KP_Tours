package KP_TOURS.backup;

import KP_TOURS.cache.TripCacheManager;
import KP_TOURS.db.DBConnection;
import KP_TOURS.model.Trip;
import KP_TOURS.repository.TripRepository;
import KP_TOURS.util.LoggerUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

public class BackupManager {

    // =========================================================
    // CREATE BACKUP
    // =========================================================

    public static void createBackup() {

        try {

            File backupDir =
                    new File(DBConnection.getBackupDirectory());

            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            LocalDateTime now = LocalDateTime.now();

            String fileName = String.format(
                    "%02d_%02d_%d_%02d_%02d_%02d_backup.json",
                    now.getDayOfMonth(),
                    now.getMonthValue(),
                    now.getYear(),
                    now.getHour(),
                    now.getMinute(),
                    now.getSecond()
            );

            File backupFile =
                    new File(backupDir, fileName);

            BackupData backupData =
                    buildBackupData();

            ObjectMapper mapper =
                    new ObjectMapper();

            mapper.registerModule(
                    new JavaTimeModule()
            );

            try (FileWriter writer =
                         new FileWriter(backupFile)) {

                writer.write(
                        mapper.writerWithDefaultPrettyPrinter()
                                .writeValueAsString(backupData)
                );
            }

            showInfo(
                    "Backup saved successfully:\n"
                            + backupFile.getAbsolutePath()
            );

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed while creating backup"
            );

            showError("Backup failed");
        }
    }

    // =========================================================
    // BUILD BACKUP DATA
    // =========================================================

    private static BackupData buildBackupData() {

        BackupData data = new BackupData();

        List<Trip> trips =
                TripCacheManager.getTripCache();

        data.setTrips(trips);

        return data;
    }

    // =========================================================
    // RESTORE BACKUP
    // =========================================================

    public static void restoreBackup() {

        Connection conn = null;

        try {

            File backupDir =
                    new File(DBConnection.getBackupDirectory());

            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            FileChooser chooser =
                    new FileChooser();

            chooser.setTitle("Select Backup File");

            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            "JSON Files",
                            "*.json"
                    )
            );

            chooser.setInitialDirectory(backupDir);

            File selectedFile =
                    chooser.showOpenDialog(null);

            if (selectedFile == null) {
                return;
            }

            ObjectMapper mapper =
                    new ObjectMapper();

            mapper.registerModule(
                    new JavaTimeModule()
            );

            BackupData data =
                    mapper.readValue(
                            selectedFile,
                            BackupData.class
                    );

            conn = DBConnection.getConnection();

            conn.setAutoCommit(false);

            TripRepository repository =
                    new TripRepository();

            if (data.getTrips() != null) {

                for (Trip trip : data.getTrips()) {

                    if (!repository.exists(trip.getId())) {

                        repository.save(trip);
                    }
                }
            }

            conn.commit();

            // Reload Cache
            TripCacheManager.initialize(
                    repository.findAll()
            );

            showInfo(
                    "Restore completed successfully"
            );

        } catch (Exception e) {

            try {

                if (conn != null) {
                    conn.rollback();
                }

            } catch (Exception rollbackException) {

                LoggerUtil.logError(
                        rollbackException,
                        "Failed while rollback"
                );
            }

            LoggerUtil.logError(
                    e,
                    "Failed while restoring backup"
            );

            showError("Restore failed");

        } finally {

            try {

                if (conn != null) {
                    conn.close();
                }

            } catch (Exception e) {

                LoggerUtil.logError(
                        e,
                        "Failed while closing DB connection"
                );
            }
        }
    }

    // =========================================================
    // ALERTS
    // =========================================================

    private static void showInfo(String message) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }

    private static void showError(String message) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }
}