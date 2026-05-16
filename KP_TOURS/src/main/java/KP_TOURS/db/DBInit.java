package KP_TOURS.db;

import KP_TOURS.util.LoggerUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBInit {

    public static void initialize() {

        createApplicationDirectories();

        String tripsTable =
                "CREATE TABLE IF NOT EXISTS trips (" +

                        "id TEXT PRIMARY KEY," +

                        "trip_date TEXT NOT NULL," +

                        "name TEXT," +
                        "sector TEXT," +
                        "airline_name TEXT," +

                        "sell_amount REAL," +
                        "purchase_amount REAL," +
                        "profit REAL," +

                        "booked_by TEXT," +
                        "pnr_no TEXT," +

                        "status TEXT," +

                        "description TEXT," +

                        "document_path TEXT," +

                        "created_at TEXT," +
                        "updated_at TEXT" +

                        ");";

        String documentsTable =
                "CREATE TABLE IF NOT EXISTS documents (" +

                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +

                        "uuid TEXT UNIQUE NOT NULL," +

                        "trip_uuid TEXT NOT NULL," +

                        "file_name TEXT," +
                        "file_path TEXT NOT NULL," +

                        "created_at TEXT" +

                        ");";

        String idxTripDate =
                "CREATE INDEX IF NOT EXISTS idx_trip_date " +
                        "ON trips(trip_date);";

        String idxPnr =
                "CREATE INDEX IF NOT EXISTS idx_pnr_no " +
                        "ON trips(pnr_no);";

        String idxName =
                "CREATE INDEX IF NOT EXISTS idx_name " +
                        "ON trips(name);";

        String idxStatus =
                "CREATE INDEX IF NOT EXISTS idx_status " +
                        "ON trips(status);";

        String idxDocumentsTripUuid =
                "CREATE INDEX IF NOT EXISTS idx_documents_trip_uuid " +
                        "ON documents(trip_uuid);";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(tripsTable);

            addColumnIfNotExists(
                    stmt,
                    "trips",
                    "description",
                    "TEXT"
            );

            stmt.execute(documentsTable);

            stmt.execute(idxTripDate);
            stmt.execute(idxPnr);
            stmt.execute(idxName);
            stmt.execute(idxStatus);
            stmt.execute(idxDocumentsTripUuid);

            LoggerUtil.logInfo("Database initialized successfully");

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Not able to initialize database"
            );
        }
    }

    private static void addColumnIfNotExists(
            Statement stmt,
            String tableName,
            String columnName,
            String columnType
    ) {

        try {

            ResultSet rs =
                    stmt.executeQuery(
                            "PRAGMA table_info(" + tableName + ")"
                    );

            boolean exists = false;

            while (rs.next()) {

                String existingColumn =
                        rs.getString("name");

                if (columnName.equalsIgnoreCase(existingColumn)) {

                    exists = true;
                    break;
                }
            }

            rs.close();

            if (!exists) {

                stmt.execute(
                        "ALTER TABLE "
                                + tableName
                                + " ADD COLUMN "
                                + columnName
                                + " "
                                + columnType
                );
            }

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed while adding column: " + columnName
            );
        }
    }

    private static void createApplicationDirectories() {

        new File(DBConnection.getAppDirectory()).mkdirs();

        new File(DBConnection.getUploadsDirectory()).mkdirs();

        new File(DBConnection.getBackupDirectory()).mkdirs();

        new File(DBConnection.getLogsDirectory()).mkdirs();
    }
}