package KP_TOURS.repository;

import KP_TOURS.db.DBConnection;
import KP_TOURS.model.Trip;
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
import java.util.UUID;

public class TripRepository {

    public boolean save(Trip trip) {

        try (Connection conn = DBConnection.getConnection()) {
            return save(conn, trip);
        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed while saving trip");
            return false;
        }
    }

    public boolean save(Connection conn, Trip trip) {

        String sql =
                "INSERT INTO trips (" +
                        "uuid, " +
                        "id, " +
                        "purchase_sales_uuid, " +
                        "trip_date, " +
                        "name, " +
                        "sector, " +
                        "airline_name, " +
                        "sell_amount, " +
                        "purchase_amount, " +
                        "profit, " +
                        "booked_by, " +
                        "pnr_no, " +
                        "status, " +
                        "description, " +
                        "created_at, " +
                        "updated_at" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            LocalDateTime now = LocalDateTime.now();

            if (trip.getUuid() == null || trip.getUuid().isBlank()) {
                trip.setUuid(UUID.randomUUID().toString());
            }

            if (trip.getId() == null) {
                trip.setId(generateTripId(conn));
            }

            if (trip.getCreatedAt() == null) {
                trip.setCreatedAt(now);
            }

            trip.setUpdatedAt(now);

            ps.setString(1, trip.getUuid());
            ps.setInt(2, trip.getId());
            ps.setString(3, trip.getPurchaseSalesUuid());
            ps.setString(4, trip.getTripDate() != null ? trip.getTripDate().toString() : null);
            ps.setString(5, trip.getName());
            ps.setString(6, trip.getSector());
            ps.setString(7, trip.getAirlineName());
            ps.setDouble(8, trip.getSellAmount());
            ps.setDouble(9, trip.getPurchaseAmount());
            ps.setDouble(10, trip.getProfit());
            ps.setString(11, trip.getBookedBy());
            ps.setString(12, trip.getPnrNo());
            ps.setString(13, trip.getStatus() != null ? trip.getStatus().name() : null);
            ps.setString(14, trip.getDescription());
            ps.setString(15, trip.getCreatedAt() != null ? trip.getCreatedAt().toString() : null);
            ps.setString(16, trip.getUpdatedAt() != null ? trip.getUpdatedAt().toString() : null);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed while saving trip");
            throw new RuntimeException(e);
        }
    }

    public boolean update(Trip trip) {

        try (Connection conn = DBConnection.getConnection()) {
            return update(conn, trip);
        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed while updating trip");
            return false;
        }
    }

    public boolean update(Connection conn, Trip trip) {

        String sql =
                "UPDATE trips SET " +
                        "id = ?, " +
                        "purchase_sales_uuid = ?, " +
                        "trip_date = ?, " +
                        "name = ?, " +
                        "sector = ?, " +
                        "airline_name = ?, " +
                        "sell_amount = ?, " +
                        "purchase_amount = ?, " +
                        "profit = ?, " +
                        "booked_by = ?, " +
                        "pnr_no = ?, " +
                        "status = ?, " +
                        "description = ?, " +
                        "updated_at = ? " +
                        "WHERE uuid = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            trip.touch();

            ps.setInt(1, trip.getId());
            ps.setString(2, trip.getPurchaseSalesUuid());
            ps.setString(3, trip.getTripDate() != null ? trip.getTripDate().toString() : null);
            ps.setString(4, trip.getName());
            ps.setString(5, trip.getSector());
            ps.setString(6, trip.getAirlineName());
            ps.setDouble(7, trip.getSellAmount());
            ps.setDouble(8, trip.getPurchaseAmount());
            ps.setDouble(9, trip.getProfit());
            ps.setString(10, trip.getBookedBy());
            ps.setString(11, trip.getPnrNo());
            ps.setString(12, trip.getStatus() != null ? trip.getStatus().name() : null);
            ps.setString(13, trip.getDescription());
            ps.setString(14, trip.getUpdatedAt().toString());
            ps.setString(15, trip.getUuid());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed while updating trip");
            throw new RuntimeException(e);
        }
    }

    public boolean delete(String uuid) {

        String sql = "DELETE FROM trips WHERE uuid = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed while deleting trip");
            return false;
        }
    }

    public List<Trip> findAll() {

        List<Trip> trips = new ArrayList<>();

        String sql = "SELECT * FROM trips ORDER BY trip_date DESC, id DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                trips.add(mapResultSetToTrip(rs));
            }

        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed while fetching trips");
        }

        return trips;
    }

    public Trip findByUuid(String uuid) {

        String sql = "SELECT * FROM trips WHERE uuid = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToTrip(rs);
            }

        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed while finding trip by uuid");
        }

        return null;
    }

    public Trip findByPurchaseSalesUuid(String purchaseSalesUuid) {

        String sql = "SELECT * FROM trips WHERE purchase_sales_uuid = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, purchaseSalesUuid);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToTrip(rs);
            }

        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed while finding trip by purchase sales uuid");
        }

        return null;
    }

    public boolean exists(String uuid) {

        String sql = "SELECT COUNT(*) FROM trips WHERE uuid = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid);

            ResultSet rs = ps.executeQuery();

            return rs.next() && rs.getInt(1) > 0;

        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed while checking trip existence");
            return false;
        }
    }

    public boolean exists(Connection conn, String uuid) {

        String sql = "SELECT COUNT(*) FROM trips WHERE uuid = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid);

            ResultSet rs = ps.executeQuery();

            return rs.next() && rs.getInt(1) > 0;

        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed while checking trip existence");
            throw new RuntimeException(e);
        }
    }

    public int count() {

        String sql = "SELECT COUNT(*) FROM trips";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            return rs.next() ? rs.getInt(1) : 0;

        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed while counting trips");
            return 0;
        }
    }

    private int generateTripId(Connection conn) {

        String sql = "SELECT COALESCE(MAX(id), 0) + 1 FROM trips";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed while generating trip id");
        }

        return 1;
    }

    private Trip mapResultSetToTrip(ResultSet rs) throws Exception {

        Trip trip = new Trip();

        trip.setUuid(rs.getString("uuid"));
        trip.setId(rs.getInt("id"));
        trip.setPurchaseSalesUuid(rs.getString("purchase_sales_uuid"));

        String tripDate = rs.getString("trip_date");

        if (tripDate != null && !tripDate.isBlank()) {
            trip.setTripDate(LocalDate.parse(tripDate));
        }

        trip.setName(rs.getString("name"));
        trip.setSector(rs.getString("sector"));
        trip.setAirlineName(rs.getString("airline_name"));

        trip.setSellAmount(rs.getDouble("sell_amount"));
        trip.setPurchaseAmount(rs.getDouble("purchase_amount"));
        trip.setProfit(rs.getDouble("profit"));

        trip.setBookedBy(rs.getString("booked_by"));
        trip.setPnrNo(rs.getString("pnr_no"));

        String status = rs.getString("status");

        if (status != null && !status.isBlank()) {
            trip.setStatus(TripStatus.valueOf(status));
        }

        trip.setDescription(rs.getString("description"));

        String createdAt = rs.getString("created_at");

        if (createdAt != null && !createdAt.isBlank()) {
            trip.setCreatedAt(LocalDateTime.parse(createdAt));
        }

        String updatedAt = rs.getString("updated_at");

        if (updatedAt != null && !updatedAt.isBlank()) {
            trip.setUpdatedAt(LocalDateTime.parse(updatedAt));
        }

        return trip;
    }
}