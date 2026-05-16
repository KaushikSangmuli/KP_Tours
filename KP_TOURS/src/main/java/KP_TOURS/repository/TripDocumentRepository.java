package KP_TOURS.repository;

import KP_TOURS.db.DBConnection;
import KP_TOURS.model.TripDocument;
import KP_TOURS.util.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TripDocumentRepository {

    // =========================================================
    // SAVE
    // =========================================================

    public boolean save(TripDocument document) {

        String sql =
                "INSERT INTO documents (" +
                        "uuid, " +
                        "trip_uuid, " +
                        "file_name, " +
                        "file_path, " +
                        "created_at" +
                        ") VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, document.getUuid());

            ps.setString(2, document.getTripUuid());

            ps.setString(3, document.getFileName());

            ps.setString(4, document.getFilePath());

            ps.setString(
                    5,
                    document.getCreatedAt().toString()
            );

            return ps.executeUpdate() > 0;

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed while saving trip document"
            );

            return false;
        }
    }
    public boolean updateFilePath(
            String uuid,
            String fileName,
            String filePath
    ) {

        String sql =
                "UPDATE documents SET " +
                        "file_name = ?, " +
                        "file_path = ? " +
                        "WHERE uuid = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fileName);
            ps.setString(2, filePath);
            ps.setString(3, uuid);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed while updating document file path"
            );

            return false;
        }
    }
    public boolean exists(String uuid) {

        String sql =
                "SELECT COUNT(*) FROM documents WHERE uuid = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid);

            ResultSet rs = ps.executeQuery();

            return rs.next() && rs.getInt(1) > 0;

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed while checking document existence"
            );

            return false;
        }
    }
    // =========================================================
    // FIND BY TRIP UUID
    // =========================================================

    public List<TripDocument> findByTripUuid(
            String tripUuid
    ) {

        List<TripDocument> documents =
                new ArrayList<>();

        String sql =
                "SELECT * FROM documents " +
                        "WHERE trip_uuid = ? " +
                        "ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tripUuid);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                documents.add(
                        mapResultSet(rs)
                );
            }

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed while fetching trip documents"
            );
        }

        return documents;
    }

    // =========================================================
    // DELETE BY UUID
    // =========================================================

    public boolean deleteByUuid(String uuid) {

        String sql =
                "DELETE FROM documents WHERE uuid = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed while deleting document"
            );

            return false;
        }
    }

    // =========================================================
    // DELETE BY TRIP UUID
    // =========================================================

    public boolean deleteByTripUuid(
            String tripUuid
    ) {

        String sql =
                "DELETE FROM documents " +
                        "WHERE trip_uuid = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tripUuid);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed while deleting trip documents"
            );

            return false;
        }
    }

    // =========================================================
    // FIND ALL
    // =========================================================

    public List<TripDocument> findAll() {

        List<TripDocument> documents =
                new ArrayList<>();

        String sql =
                "SELECT * FROM documents";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                documents.add(
                        mapResultSet(rs)
                );
            }

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed while fetching all documents"
            );
        }

        return documents;
    }

    // =========================================================
    // RESULTSET MAPPER
    // =========================================================

    private TripDocument mapResultSet(
            ResultSet rs
    ) throws Exception {

        TripDocument document =
                new TripDocument();

        setField(
                document,
                "uuid",
                rs.getString("uuid")
        );

        document.setTripUuid(
                rs.getString("trip_uuid")
        );

        document.setFileName(
                rs.getString("file_name")
        );

        document.setFilePath(
                rs.getString("file_path")
        );

        String createdAt =
                rs.getString("created_at");

        if (createdAt != null) {

            setField(
                    document,
                    "createdAt",
                    LocalDateTime.parse(createdAt)
            );
        }

        return document;
    }

    // =========================================================
    // REFLECTION FIELD SETTER
    // =========================================================

    private void setField(
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
}