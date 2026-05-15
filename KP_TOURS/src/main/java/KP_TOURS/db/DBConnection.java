package KP_TOURS.db;



import KP_TOURS.util.LoggerUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // =========================================================
    // App Storage Directory
    // =========================================================

    private static final String APP_DIR =
            System.getProperty("user.home") + "/PrabalAppData";

    private static final String DB_URL =
            "jdbc:sqlite:" + APP_DIR + "/prabal.db";

    // =========================================================
    // Connection Provider
    // =========================================================

    public static Connection getConnection() {

        try {

            // Ensure app directory exists
            new File(APP_DIR).mkdirs();

            return DriverManager.getConnection(DB_URL);

        } catch (SQLException e) {

            LoggerUtil.logError(
                    e,
                    "Failed while establishing database connection"
            );

            return null;
        }
    }

    // =========================================================
    // Utility Paths
    // =========================================================

    public static String getAppDirectory() {
        return APP_DIR;
    }

    public static String getUploadsDirectory() {
        return APP_DIR + "/uploads";
    }

    public static String getBackupDirectory() {
        return APP_DIR + "/backups";
    }

    public static String getLogsDirectory() {
        return APP_DIR + "/logs";
    }
}