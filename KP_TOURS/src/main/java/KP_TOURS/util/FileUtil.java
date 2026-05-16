package KP_TOURS.util;

import KP_TOURS.db.DBConnection;
import KP_TOURS.model.Trip;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileUtil {

    private static final DateTimeFormatter FILE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static String saveDocument(
            File sourceFile,
            Trip trip
    ) {

        try {

            if (sourceFile == null || trip == null) {
                return null;
            }

            File uploadDir =
                    new File(DBConnection.getUploadsDirectory());

            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String originalName =
                    sourceFile.getName();

            String extension =
                    getExtension(originalName);

            String baseName =
                    removeExtension(originalName);

            String customerName =
                    sanitize(trip.getName());

            String pnr =
                    sanitize(trip.getPnrNo());

            String timestamp =
                    LocalDateTime.now().format(FILE_TIME_FORMAT);

            String finalFileName =
                    customerName
                            + "_"
                            + pnr
                            + "_"
                            + sanitize(baseName)
                            + "_"
                            + timestamp
                            + extension;

            Path destination =
                    uploadDir.toPath().resolve(finalFileName);

            Files.copy(
                    sourceFile.toPath(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return destination.toAbsolutePath().toString();

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed while saving document"
            );

            return null;
        }
    }

    private static String sanitize(String value) {

        if (value == null || value.isBlank()) {
            return "NA";
        }

        return value
                .trim()
                .replaceAll("[^a-zA-Z0-9-_]", "_")
                .replaceAll("_+", "_");
    }

    private static String getExtension(String fileName) {

        int index =
                fileName.lastIndexOf(".");

        if (index == -1) {
            return "";
        }

        return fileName.substring(index);
    }

    private static String removeExtension(String fileName) {

        int index =
                fileName.lastIndexOf(".");

        if (index == -1) {
            return fileName;
        }

        return fileName.substring(0, index);
    }
}