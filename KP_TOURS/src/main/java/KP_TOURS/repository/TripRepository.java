package KP_TOURS.repository;


import KP_TOURS.model.Trip;
import KP_TOURS.db.DBConnection;
import KP_TOURS.model.TripStatus;
import KP_TOURS.util.LoggerUtil;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TripRepository {

    // =========================================================
    // INSERT
    // =========================================================

    public boolean save(Trip trip) {

        String sql =
                "INSERT INTO trips (" +
                        "id, " +
                        "trip_date, " +
                        "naam, " +
                        "sector, " +
                        "airline_name, " +
                        "sell_amount, " +
                        "purchase_amount, " +
                        "profit, " +
                        "booked_by, " +
                        "pnr_no, " +
                        "status, " +
                        "document_path, " +
                        "created_at, " +
                        "updated_at" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, trip.getId());

            ps.setString(2,
                    trip.getTripDate() != null
                            ? trip.getTripDate().toString()
                            : null);

            ps.setString(3, trip.getNaam());
            ps.setString(4, trip.getSector());
            ps.setString(5, trip.getAirlineName());

            ps.setDouble(6, trip.getSellAmount());
            ps.setDouble(7, trip.getPurchaseAmount());
            ps.setDouble(8, trip.getProfit());

            ps.setString(9, trip.getBookedBy());
            ps.setString(10, trip.getPnrNo());

            ps.setString(11,
                    trip.getStatus() != null
                            ? trip.getStatus().name()
                            : null);

            ps.setString(12, trip.getDocumentPath());

            ps.setString(13,
                    trip.getCreatedAt() != null
                            ? trip.getCreatedAt().toString()
                            : null);

            ps.setString(14,
                    trip.getUpdatedAt() != null
                            ? trip.getUpdatedAt().toString()
                            : null);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed while saving trip"
            );

            return false;
        }
    }

    // =========================================================
    // UPDATE
    // =========================================================

    public boolean update(Trip trip) {

        String sql =
                "UPDATE trips SET " +

                        "trip_date = ?, " +
                        "naam = ?, " +
                        "sector = ?, " +
                        "airline_name = ?, " +
                        "sell_amount = ?, " +
                        "purchase_amount = ?, " +
                        "profit = ?, " +
                        "booked_by = ?, " +
                        "pnr_no = ?, " +
                        "status = ?, " +
                        "document_path = ?, " +
                        "updated_at = ? " +

                        "WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1,
                    trip.getTripDate() != null
                            ? trip.getTripDate().toString()
                            : null);

            ps.setString(2, trip.getNaam());
            ps.setString(3, trip.getSector());
            ps.setString(4, trip.getAirlineName());

            ps.setDouble(5, trip.getSellAmount());
            ps.setDouble(6, trip.getPurchaseAmount());
            ps.setDouble(7, trip.getProfit());

            ps.setString(8, trip.getBookedBy());
            ps.setString(9, trip.getPnrNo());

            ps.setString(10,
                    trip.getStatus() != null
                            ? trip.getStatus().name()
                            : null);

            ps.setString(11, trip.getDocumentPath());

            trip.touch();

            ps.setString(12,
                    trip.getUpdatedAt().toString());

            ps.setString(13, trip.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed while updating trip"
            );

            return false;
        }
    }

    // =========================================================
    // DELETE
    // =========================================================

    public boolean delete(String tripId) {

        String sql = "DELETE FROM trips WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tripId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed while deleting trip"
            );

            return false;
        }
    }

    // =========================================================
    // FIND ALL
    // =========================================================

    public List<Trip> findAll() {

        List<Trip> trips = new ArrayList<>();

        String sql = "SELECT * FROM trips";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                trips.add(mapResultSetToTrip(rs));
            }

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed while fetching trips"
            );
        }

        return trips;
    }

    // =========================================================
    // FIND BY ID
    // =========================================================

    public Trip findById(String tripId) {

        String sql = "SELECT * FROM trips WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tripId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToTrip(rs);
            }

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed while finding trip by id"
            );
        }

        return null;
    }

    // =========================================================
    // EXISTS
    // =========================================================

    public boolean exists(String tripId) {

        String sql =
                "SELECT COUNT(*) FROM trips WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tripId);

            ResultSet rs = ps.executeQuery();

            return rs.next() && rs.getInt(1) > 0;

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed while checking trip existence"
            );

            return false;
        }
    }

    // =========================================================
    // COUNT
    // =========================================================

    public int count() {

        String sql = "SELECT COUNT(*) FROM trips";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            return rs.next() ? rs.getInt(1) : 0;

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed while counting trips"
            );

            return 0;
        }
    }

    // =========================================================
    // RESULTSET MAPPER
    // =========================================================

    private Trip mapResultSetToTrip(ResultSet rs) throws Exception {

        Trip trip = new Trip();

        // Reflection-like override of generated UUID
        setField(trip, "id", rs.getString("id"));

        String tripDate = rs.getString("trip_date");

        if (tripDate != null) {
            trip.setTripDate(LocalDate.parse(tripDate));
        }

        trip.setNaam(rs.getString("naam"));
        trip.setSector(rs.getString("sector"));
        trip.setAirlineName(rs.getString("airline_name"));

        trip.setSellAmount(rs.getDouble("sell_amount"));
        trip.setPurchaseAmount(rs.getDouble("purchase_amount"));

        trip.setBookedBy(rs.getString("booked_by"));
        trip.setPnrNo(rs.getString("pnr_no"));

        String status = rs.getString("status");

        if (status != null) {
            trip.setStatus(TripStatus.valueOf(status));
        }

        trip.setDocumentPath(rs.getString("document_path"));

        String createdAt = rs.getString("created_at");

        if (createdAt != null) {
            setField(
                    trip,
                    "createdAt",
                    LocalDateTime.parse(createdAt)
            );
        }

        String updatedAt = rs.getString("updated_at");

        if (updatedAt != null) {
            setField(
                    trip,
                    "updatedAt",
                    LocalDateTime.parse(updatedAt)
            );
        }

        return trip;
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
                target.getClass().getDeclaredField(fieldName);

        field.setAccessible(true);

        field.set(target, value);
    }
}