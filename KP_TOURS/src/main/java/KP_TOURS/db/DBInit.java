package KP_TOURS.db;

import KP_TOURS.util.LoggerUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBInit {

    public static void initialize() {

        createApplicationDirectories();

        String purchaseSalesTable =
                "CREATE TABLE IF NOT EXISTS purchase_sales (" +

                        "uuid TEXT PRIMARY KEY," +

                        "bill_no TEXT UNIQUE NOT NULL," +

                        "entry_date TEXT NOT NULL," +

                        "purchase_type TEXT NOT NULL," +

                        "purchase_from TEXT," +
                        "customer_uuid TEXT," +

                        "description TEXT," +
                        "purchase_remark TEXT," +
                        "sales_remark TEXT," +

                        "qty INTEGER DEFAULT 1," +

                        "purchase_rate REAL DEFAULT 0," +
                        "sell_rate REAL DEFAULT 0," +

                        "total_purchase REAL DEFAULT 0," +
                        "total_sale REAL DEFAULT 0," +
                        "profit REAL DEFAULT 0," +

                        "payment_mode TEXT," +

                        "pnr_no TEXT," +
                        "sector TEXT," +
                        "airline_name TEXT," +
                        "travel_date TEXT," +

                        "linked_trip_uuid TEXT," +

                        "status TEXT DEFAULT 'ACTIVE'," +

                        "created_at TEXT," +
                        "updated_at TEXT" +

                        ");";



        String tripsTable =
                "CREATE TABLE IF NOT EXISTS trips (" +

                        "uuid TEXT PRIMARY KEY," +

                        "id INTEGER UNIQUE," +

                        "purchase_sales_uuid TEXT," +

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

        String accountsTable =
                "CREATE TABLE IF NOT EXISTS accounts (" +

                        "uuid TEXT PRIMARY KEY," +

                        "account_no TEXT UNIQUE NOT NULL," +

                        "name TEXT NOT NULL," +
                        "address TEXT," +
                        "city TEXT," +
                        "phone_no TEXT," +
                        "email TEXT," +

                        "account_group TEXT NOT NULL," +

                        "created_at TEXT," +
                        "updated_at TEXT" +

                        ");";

        String appSettingsTable =
                "CREATE TABLE IF NOT EXISTS app_settings (" +

                        "setting_key TEXT PRIMARY KEY," +

                        "setting_value TEXT," +

                        "created_at TEXT," +
                        "updated_at TEXT" +

                        ");";


        String payReceiveTable =
                "CREATE TABLE IF NOT EXISTS pay_receive (" +
                        "uuid TEXT PRIMARY KEY," +
                        "voucher_no TEXT UNIQUE NOT NULL," +
                        "entry_type TEXT NOT NULL," + // PAY / RECEIVE
                        "entry_date TEXT NOT NULL," +

                        "account_uuid TEXT NOT NULL," +
                        "payment_mode TEXT NOT NULL," +

                        "total_amount REAL NOT NULL," +

                        "reference_no TEXT," +
                        "remark TEXT," +

                        "created_at TEXT," +
                        "updated_at TEXT" +
                        ");";

        String payReceiveBillAdjustmentTable =
                "CREATE TABLE IF NOT EXISTS pay_receive_bill_adjustment (" +
                        "uuid TEXT PRIMARY KEY," +

                        "pay_receive_uuid TEXT NOT NULL," +
                        "purchase_sales_uuid TEXT NOT NULL," +

                        "bill_no TEXT," +
                        "adjusted_amount REAL NOT NULL," +

                        "created_at TEXT," +
                        "updated_at TEXT" +
                        ");";




        String idxPurchaseSalesDate =
                "CREATE INDEX IF NOT EXISTS idx_purchase_sales_date " +
                        "ON purchase_sales(entry_date);";

        String idxPurchaseSalesBillNo =
                "CREATE INDEX IF NOT EXISTS idx_purchase_sales_bill_no " +
                        "ON purchase_sales(bill_no);";

        String idxPurchaseSalesCustomer =
                "CREATE INDEX IF NOT EXISTS idx_purchase_sales_customer " +
                        "ON purchase_sales(customer_uuid);";

        String idxPurchaseSalesTrip =
                "CREATE INDEX IF NOT EXISTS idx_purchase_sales_trip " +
                        "ON purchase_sales(linked_trip_uuid);";

        String idxAccountNo =
                "CREATE INDEX IF NOT EXISTS idx_account_no " +
                        "ON accounts(account_no);";

        String idxAccountName =
                "CREATE INDEX IF NOT EXISTS idx_account_name " +
                        "ON accounts(name);";

        String idxAccountGroup =
                "CREATE INDEX IF NOT EXISTS idx_account_group " +
                        "ON accounts(account_group);";

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


            stmt.execute(payReceiveTable);
            stmt.execute(payReceiveBillAdjustmentTable);
            stmt.execute(documentsTable);
            stmt.execute(accountsTable);
            stmt.execute(appSettingsTable);
            stmt.execute(purchaseSalesTable);
            stmt.execute(idxTripDate);
            stmt.execute(idxPnr);
            stmt.execute(idxName);
            stmt.execute(idxStatus);
            stmt.execute(idxDocumentsTripUuid);
            stmt.execute(idxAccountNo);
            stmt.execute(idxAccountName);
            stmt.execute(idxAccountGroup);
            stmt.execute(idxPurchaseSalesDate);
            stmt.execute(idxPurchaseSalesBillNo);
            stmt.execute(idxPurchaseSalesCustomer);
            stmt.execute(idxPurchaseSalesTrip);

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