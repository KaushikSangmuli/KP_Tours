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
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class BackupManager {

    private static final ObjectMapper objectMapper =
            new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static void createBackup() {

        try {

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

            FileChooser chooser =
                    new FileChooser();

            chooser.setTitle("Save Backup");

            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            "JSON Backup File",
                            "*.json"
                    )
            );

            chooser.setInitialFileName(
                    "prabal_backup_" + System.currentTimeMillis() + ".json"
            );

            File file =
                    chooser.showSaveDialog(null);

            if (file == null) {
                return;
            }

            objectMapper.writeValue(file, backupData);

            alert("Backup created successfully");

        } catch (Exception e) {

            LoggerUtil.logError(e, "Failed while creating backup");

            alert("Failed to create backup");
        }
    }

    public static void restoreBackup() {

        try {

            FileChooser chooser =
                    new FileChooser();

            chooser.setTitle("Restore Backup");

            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            "JSON Backup File",
                            "*.json"
                    )
            );

            File file =
                    chooser.showOpenDialog(null);

            if (file == null) {
                return;
            }

            BackupData backupData =
                    objectMapper.readValue(file, BackupData.class);

            clearExistingData();

            TripRepository tripRepository =
                    new TripRepository();

            TripDocumentRepository documentRepository =
                    new TripDocumentRepository();

            if (backupData.getTrips() != null) {

                for (BackupData.TripBackupData tripBackupData
                        : backupData.getTrips()) {

                    Trip trip =
                            toTrip(tripBackupData);

                    tripRepository.save(trip);
                }
            }

            if (backupData.getDocuments() != null) {

                for (BackupData.DocumentBackupData documentBackupData
                        : backupData.getDocuments()) {

                    TripDocument document =
                            toTripDocument(documentBackupData);

                    documentRepository.save(document);
                }
            }

            TripCacheManager.initialize(
                    tripRepository.findAll()
            );

            alert("Backup restored successfully");

        } catch (Exception e) {

            LoggerUtil.logError(e, "Failed while restoring backup");

            alert("Failed to restore backup");
        }
    }

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

    private static void clearExistingData() {

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("DELETE FROM documents");
            stmt.executeUpdate("DELETE FROM trips");

        } catch (Exception e) {

            LoggerUtil.logError(e, "Failed while clearing existing data");
        }
    }

    private static void setField(
            Object target,
            String fieldName,
            Object value
    ) throws Exception {

        var field =
                target.getClass().getDeclaredField(fieldName);

        field.setAccessible(true);

        field.set(target, value);
    }

    private static void alert(String message) {

        javafx.scene.control.Alert alert =
                new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.INFORMATION
                );

        alert.setContentText(message);

        alert.showAndWait();
    }
}