package KP_TOURS.util;

import KP_TOURS.db.DBConnection;

import java.awt.Desktop;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileUtil {

    // =========================================================
    // SAVE UPLOADED FILE
    // =========================================================

    public static String saveDocument(File sourceFile) {

        try {

            if (sourceFile == null || !sourceFile.exists()) {
                return null;
            }

            File uploadsDir =
                    new File(DBConnection.getUploadsDirectory());

            uploadsDir.mkdirs();

            String originalFileName =
                    sourceFile.getName();

            String extension = "";

            int dotIndex =
                    originalFileName.lastIndexOf(".");

            if (dotIndex >= 0) {

                extension =
                        originalFileName.substring(dotIndex);
            }

            String newFileName =
                    UUID.randomUUID() + extension;

            Path targetPath =
                    uploadsDir.toPath()
                            .resolve(newFileName);

            Files.copy(
                    sourceFile.toPath(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return targetPath.toString();

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed while saving document"
            );

            return null;
        }
    }

    // =========================================================
    // OPEN FILE
    // =========================================================

    public static void openFile(String filePath) {

        try {

            if (filePath == null || filePath.isBlank()) {
                return;
            }

            File file = new File(filePath);

            if (!file.exists()) {
                return;
            }

            Desktop.getDesktop().open(file);

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed while opening file"
            );
        }
    }

    // =========================================================
    // DOWNLOAD FILE
    // =========================================================

    public static boolean downloadFile(
            String sourcePath,
            File destination
    ) {

        try {

            if (sourcePath == null || destination == null) {
                return false;
            }

            Files.copy(
                    Path.of(sourcePath),
                    destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );

            return true;

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed while downloading file"
            );

            return false;
        }
    }

    // =========================================================
    // DELETE FILE
    // =========================================================

    public static boolean deleteFile(String filePath) {

        try {

            if (filePath == null || filePath.isBlank()) {
                return false;
            }

            return Files.deleteIfExists(
                    Path.of(filePath)
            );

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed while deleting file"
            );

            return false;
        }
    }
}