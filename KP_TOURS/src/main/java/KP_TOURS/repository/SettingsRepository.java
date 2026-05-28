package KP_TOURS.repository;

import KP_TOURS.db.DBConnection;
import KP_TOURS.util.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;

public class SettingsRepository {

    public String getValue(String key, String defaultValue) {

        String sql =
                "SELECT setting_value FROM app_settings WHERE setting_key = ?";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(1, key);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("setting_value");
            }

        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed to fetch setting: " + key);
        }

        return defaultValue;
    }

    public boolean saveOrUpdate(String key, String value) {

        String existing =
                getValue(key, null);

        String now =
                LocalDateTime.now().toString();

        String sql;

        if (existing == null) {

            sql =
                    "INSERT INTO app_settings " +
                            "(setting_key, setting_value, created_at, updated_at) " +
                            "VALUES (?, ?, ?, ?)";

        } else {

            sql =
                    "UPDATE app_settings SET " +
                            "setting_value = ?, " +
                            "updated_at = ? " +
                            "WHERE setting_key = ?";
        }

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            if (existing == null) {

                stmt.setString(1, key);
                stmt.setString(2, value);
                stmt.setString(3, now);
                stmt.setString(4, now);

            } else {

                stmt.setString(1, value);
                stmt.setString(2, now);
                stmt.setString(3, key);
            }

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed to save setting: " + key);
            return false;
        }
    }
}