package KP_TOURS.db;



import KP_TOURS.util.LoggerUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;

public class DBInit {

    public static void initialize() {

        createApplicationDirectories();

        String tripsTable =
                "CREATE TABLE IF NOT EXISTS trips (" +

                        "id TEXT PRIMARY KEY," +

                        "trip_date TEXT NOT NULL," +

                        "naam TEXT," +
                        "sector TEXT," +
                        "airline_name TEXT," +

                        "sell_amount REAL," +
                        "purchase_amount REAL," +
                        "profit REAL," +

                        "booked_by TEXT," +
                        "pnr_no TEXT," +

                        "status TEXT," +

                        "document_path TEXT," +

                        "created_at TEXT," +
                        "updated_at TEXT" +

                        ");";

        // =====================================================
        // Indexes
        // =====================================================

        String idxTripDate =
                "CREATE INDEX IF NOT EXISTS idx_trip_date " +
                        "ON trips(trip_date);";

        String idxPnr =
                "CREATE INDEX IF NOT EXISTS idx_pnr_no " +
                        "ON trips(pnr_no);";

        String idxNaam =
                "CREATE INDEX IF NOT EXISTS idx_naam " +
                        "ON trips(naam);";

        String idxStatus =
                "CREATE INDEX IF NOT EXISTS idx_status " +
                        "ON trips(status);";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(tripsTable);

            stmt.execute(idxTripDate);
            stmt.execute(idxPnr);
            stmt.execute(idxNaam);
            stmt.execute(idxStatus);

            LoggerUtil.logInfo("Database initialized successfully");

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Not able to initialize database"
            );
        }
    }

    // =========================================================
    // App Folder Setup
    // =========================================================

    private static void createApplicationDirectories() {

        new File(DBConnection.getAppDirectory()).mkdirs();

        new File(DBConnection.getUploadsDirectory()).mkdirs();

        new File(DBConnection.getBackupDirectory()).mkdirs();

        new File(DBConnection.getLogsDirectory()).mkdirs();
    }
}