package KP_TOURS.backup;

import KP_TOURS.cache.TripCacheManager;
import KP_TOURS.db.DBConnection;
import KP_TOURS.model.Trip;
import KP_TOURS.model.TripDocument;
import KP_TOURS.model.TripStatus;
import KP_TOURS.repository.TripDocumentRepository;
import KP_TOURS.repository.TripRepository;
import KP_TOURS.util.LoggerUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BackupManager {

    private static final ObjectMapper objectMapper =
            new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private static final DateTimeFormatter BACKUP_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private static final String BACKUP_JSON_NAME = "backup.json";
    private static final String DOCUMENTS_FOLDER = "documents/";

    // =========================================================
    // CREATE BACKUP ZIP
    // =========================================================

    public static void createBackup() {

        try {

            File backupDirectory =
                    new File(DBConnection.getBackupDirectory());

            if (!backupDirectory.exists()) {
                backupDirectory.mkdirs();
            }

            String fileName =
                    "prabal_backup_"
                            + LocalDateTime.now().format(BACKUP_TIME_FORMAT)
                            + ".zip";

            File backupZip =
                    new File(backupDirectory, fileName);

            createBackupZip(backupZip);

            alert("Backup created successfully");

        } catch (Exception e) {

            LoggerUtil.logError(e, "Failed while creating backup");
            alert("Failed to create backup");
        }
    }

    public static void createAutoBackup() {

        try {

            File backupDirectory =
                    new File(DBConnection.getBackupDirectory());

            if (!backupDirectory.exists()) {
                backupDirectory.mkdirs();
            }

            String fileName =
                    "auto_backup_"
                            + LocalDate.now()
                            + "_"
                            + LocalDateTime.now().format(BACKUP_TIME_FORMAT)
                            + ".zip";

            File backupZip =
                    new File(backupDirectory, fileName);

            createBackupZip(backupZip);

        } catch (Exception e) {

            LoggerUtil.logError(e, "Failed while creating auto backup");
        }
    }

    private static void createBackupZip(File backupZip) throws Exception {

        TripRepository tripRepository =
                new TripRepository();

        TripDocumentRepository documentRepository =
                new TripDocumentRepository();

        List<Trip> trips =
                tripRepository.findAll();

        List<TripDocument> documents =
                documentRepository.findAll();

        BackupData backupData =
                new BackupData();

        for (Trip trip : trips) {
            backupData.getTrips().add(toTripBackupData(trip));
        }

        for (TripDocument document : documents) {
            backupData.getDocuments().add(toDocumentBackupData(document));
        }

        try (ZipOutputStream zos =
                     new ZipOutputStream(
                             new FileOutputStream(backupZip)
                     )) {

            // backup.json
            zos.putNextEntry(new ZipEntry(BACKUP_JSON_NAME));

            byte[] jsonBytes =
                    objectMapper.writeValueAsBytes(backupData);

            zos.write(jsonBytes);

            zos.closeEntry();

            // documents/
            for (TripDocument document : documents) {

                if (document.getFilePath() == null
                        || document.getFilePath().isBlank()) {
                    continue;
                }

                File sourceFile =
                        new File(document.getFilePath());

                if (!sourceFile.exists()) {
                    continue;
                }

                String zipDocumentName =
                        DOCUMENTS_FOLDER
                                + document.getUuid()
                                + "_"
                                + safeFileName(document.getFileName());

                zos.putNextEntry(
                        new ZipEntry(zipDocumentName)
                );

                Files.copy(
                        sourceFile.toPath(),
                        zos
                );

                zos.closeEntry();
            }
        }
    }

    // =========================================================
    // RESTORE BACKUP ZIP - MERGE
    // =========================================================

    public static void restoreBackup() {

        try {

            File backupDirectory =
                    new File(DBConnection.getBackupDirectory());

            if (!backupDirectory.exists()) {
                backupDirectory.mkdirs();
            }

            FileChooser chooser =
                    new FileChooser();

            chooser.setTitle("Restore Backup");
            chooser.setInitialDirectory(backupDirectory);

            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            "ZIP Backup File",
                            "*.zip"
                    )
            );

            File backupZip =
                    chooser.showOpenDialog(null);

            if (backupZip == null) {
                return;
            }

            restoreFromZip(backupZip);

            alert("Backup restored successfully");

        } catch (Exception e) {

            LoggerUtil.logError(e, "Failed while restoring backup");
            alert("Failed to restore backup");
        }
    }

    private static void restoreFromZip(File backupZip) throws Exception {

        File tempRestoreDir =
                new File(
                        DBConnection.getBackupDirectory(),
                        "temp_restore"
                );

        if (tempRestoreDir.exists()) {
            deleteDirectory(tempRestoreDir);
        }

        tempRestoreDir.mkdirs();

        unzip(backupZip, tempRestoreDir);

        File backupJson =
                new File(tempRestoreDir, BACKUP_JSON_NAME);

        if (!backupJson.exists()) {
            throw new IllegalStateException("backup.json not found in zip");
        }

        BackupData backupData =
                objectMapper.readValue(
                        backupJson,
                        BackupData.class
                );

        TripRepository tripRepository =
                new TripRepository();

        TripDocumentRepository documentRepository =
                new TripDocumentRepository();

        // Trips merge
        if (backupData.getTrips() != null) {

            for (BackupData.TripBackupData tripBackupData
                    : backupData.getTrips()) {

                Trip trip =
                        toTrip(tripBackupData);

                if (tripRepository.exists(trip.getId())) {

                    tripRepository.update(trip);

                } else {

                    tripRepository.save(trip);
                }
            }
        }

        // Documents merge
        if (backupData.getDocuments() != null) {

            File documentsDir =
                    new File(tempRestoreDir, "documents");

            File uploadsDir =
                    new File(DBConnection.getUploadsDirectory());

            if (!uploadsDir.exists()) {
                uploadsDir.mkdirs();
            }

            for (BackupData.DocumentBackupData documentBackupData
                    : backupData.getDocuments()) {

                TripDocument document =
                        toTripDocument(documentBackupData);

                if (documentRepository.exists(document.getUuid())) {
                    continue;
                }

                File restoredFile =
                        findRestoredDocumentFile(
                                documentsDir,
                                document.getUuid()
                        );

                if (restoredFile != null && restoredFile.exists()) {

                    File destination =
                            new File(
                                    uploadsDir,
                                    restoredFile.getName()
                            );

                    Files.copy(
                            restoredFile.toPath(),
                            destination.toPath(),
                            StandardCopyOption.REPLACE_EXISTING
                    );

                    document.setFilePath(
                            destination.getAbsolutePath()
                    );

                    document.setFileName(
                            destination.getName()
                    );
                }

                documentRepository.save(document);
            }
        }

        TripCacheManager.initialize(
                tripRepository.findAll()
        );

        deleteDirectory(tempRestoreDir);
    }

    // =========================================================
    // ZIP HELPERS
    // =========================================================

    private static void unzip(
            File zipFile,
            File destinationDir
    ) throws Exception {

        try (ZipInputStream zis =
                     new ZipInputStream(
                             new FileInputStream(zipFile)
                     )) {

            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {

                File newFile =
                        new File(
                                destinationDir,
                                entry.getName()
                        );

                if (entry.isDirectory()) {

                    newFile.mkdirs();

                } else {

                    File parent =
                            newFile.getParentFile();

                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }

                    Files.copy(
                            zis,
                            newFile.toPath(),
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }

                zis.closeEntry();
            }
        }
    }

    private static File findRestoredDocumentFile(
            File documentsDir,
            String documentUuid
    ) {

        if (documentsDir == null
                || !documentsDir.exists()
                || documentUuid == null) {
            return null;
        }

        File[] files =
                documentsDir.listFiles();

        if (files == null) {
            return null;
        }

        for (File file : files) {

            if (file.getName().startsWith(documentUuid + "_")) {
                return file;
            }
        }

        return null;
    }

    private static void deleteDirectory(File directory) {

        if (directory == null || !directory.exists()) {
            return;
        }

        File[] files =
                directory.listFiles();

        if (files != null) {

            for (File file : files) {

                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }

        directory.delete();
    }

    // =========================================================
    // MAPPERS
    // =========================================================

    private static BackupData.TripBackupData toTripBackupData(
            Trip trip
    ) {

        BackupData.TripBackupData data =
                new BackupData.TripBackupData();

        data.setId(trip.getId());

        data.setTripDate(
                trip.getTripDate() == null
                        ? null
                        : trip.getTripDate().toString()
        );

        data.setName(trip.getName());
        data.setSector(trip.getSector());
        data.setAirlineName(trip.getAirlineName());
        data.setSellAmount(trip.getSellAmount());
        data.setPurchaseAmount(trip.getPurchaseAmount());
        data.setProfit(trip.getProfit());
        data.setBookedBy(trip.getBookedBy());
        data.setPnrNo(trip.getPnrNo());

        data.setStatus(
                trip.getStatus() == null
                        ? null
                        : trip.getStatus().name()
        );

        data.setDescription(trip.getDescription());

        data.setCreatedAt(
                trip.getCreatedAt() == null
                        ? null
                        : trip.getCreatedAt().toString()
        );

        data.setUpdatedAt(
                trip.getUpdatedAt() == null
                        ? null
                        : trip.getUpdatedAt().toString()
        );

        return data;
    }

    private static BackupData.DocumentBackupData toDocumentBackupData(
            TripDocument document
    ) {

        BackupData.DocumentBackupData data =
                new BackupData.DocumentBackupData();

        data.setUuid(document.getUuid());
        data.setTripUuid(document.getTripUuid());
        data.setFileName(document.getFileName());
        data.setFilePath(document.getFilePath());

        data.setCreatedAt(
                document.getCreatedAt() == null
                        ? null
                        : document.getCreatedAt().toString()
        );

        return data;
    }

    private static Trip toTrip(
            BackupData.TripBackupData data
    ) throws Exception {

        Trip trip =
                new Trip();

        setField(trip, "id", data.getId());

        if (data.getTripDate() != null
                && !data.getTripDate().isBlank()) {

            trip.setTripDate(
                    LocalDate.parse(data.getTripDate())
            );
        }

        trip.setName(data.getName());
        trip.setSector(data.getSector());
        trip.setAirlineName(data.getAirlineName());
        trip.setSellAmount(data.getSellAmount());
        trip.setPurchaseAmount(data.getPurchaseAmount());
        trip.setBookedBy(data.getBookedBy());
        trip.setPnrNo(data.getPnrNo());

        if (data.getStatus() != null
                && !data.getStatus().isBlank()) {

            trip.setStatus(
                    TripStatus.valueOf(data.getStatus())
            );
        }

        trip.setDescription(data.getDescription());

        if (data.getCreatedAt() != null
                && !data.getCreatedAt().isBlank()) {

            setField(
                    trip,
                    "createdAt",
                    LocalDateTime.parse(data.getCreatedAt())
            );
        }

        if (data.getUpdatedAt() != null
                && !data.getUpdatedAt().isBlank()) {

            setField(
                    trip,
                    "updatedAt",
                    LocalDateTime.parse(data.getUpdatedAt())
            );
        }

        return trip;
    }

    private static TripDocument toTripDocument(
            BackupData.DocumentBackupData data
    ) throws Exception {

        TripDocument document =
                new TripDocument();

        setField(document, "uuid", data.getUuid());

        document.setTripUuid(data.getTripUuid());
        document.setFileName(data.getFileName());
        document.setFilePath(data.getFilePath());

        if (data.getCreatedAt() != null
                && !data.getCreatedAt().isBlank()) {

            setField(
                    document,
                    "createdAt",
                    LocalDateTime.parse(data.getCreatedAt())
            );
        }

        return document;
    }

    private static void setField(
            Object target,
            String fieldName,
            Object value
    ) throws Exception {

        var field =
                target.getClass()
                        .getDeclaredField(fieldName);

        field.setAccessible(true);

        field.set(target, value);
    }

    private static String safeFileName(String fileName) {

        if (fileName == null || fileName.isBlank()) {
            return "document";
        }

        return fileName
                .replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", "_");
    }

    private static void alert(String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setContentText(message);

        alert.showAndWait();
    }
}