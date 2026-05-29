package KP_TOURS.repository;

import KP_TOURS.db.DBConnection;
import KP_TOURS.model.PurchaseSales;
import KP_TOURS.util.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PurchaseSalesRepository {

    public boolean save(PurchaseSales purchaseSales) {

        String sql =
                "INSERT INTO purchase_sales (" +
                        "uuid, bill_no, entry_date, purchase_type, purchase_from, customer_uuid, " +
                        "description, purchase_remark, sales_remark, qty, purchase_rate, sell_rate, " +
                        "total_purchase, total_sale, profit, payment_mode, pnr_no, sector, airline_name, " +
                        "travel_date, linked_trip_uuid, status, created_at, updated_at" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            purchaseSales.calculateTotals();

            if (purchaseSales.getBillNo() == null || purchaseSales.getBillNo().isBlank()) {
                purchaseSales.setBillNo(generateBillNo(conn));
            }

            ps.setString(1, purchaseSales.getUuid());
            ps.setString(2, purchaseSales.getBillNo());
            ps.setString(3, purchaseSales.getEntryDate() != null ? purchaseSales.getEntryDate().toString() : null);
            ps.setString(4, purchaseSales.getPurchaseType());
            ps.setString(5, purchaseSales.getPurchaseFrom());
            ps.setString(6, purchaseSales.getCustomerUuid());
            ps.setString(7, purchaseSales.getDescription());
            ps.setString(8, purchaseSales.getPurchaseRemark());
            ps.setString(9, purchaseSales.getSalesRemark());
            ps.setInt(10, purchaseSales.getQty());
            ps.setDouble(11, purchaseSales.getPurchaseRate());
            ps.setDouble(12, purchaseSales.getSellRate());
            ps.setDouble(13, purchaseSales.getTotalPurchase());
            ps.setDouble(14, purchaseSales.getTotalSale());
            ps.setDouble(15, purchaseSales.getProfit());
            ps.setString(16, purchaseSales.getPaymentMode());
            ps.setString(17, purchaseSales.getPnrNo());
            ps.setString(18, purchaseSales.getSector());
            ps.setString(19, purchaseSales.getAirlineName());
            ps.setString(20, purchaseSales.getTravelDate() != null ? purchaseSales.getTravelDate().toString() : null);
            ps.setString(21, purchaseSales.getLinkedTripUuid());
            ps.setString(22, purchaseSales.getStatus());
            ps.setString(23, purchaseSales.getCreatedAt() != null ? purchaseSales.getCreatedAt().toString() : null);
            ps.setString(24, purchaseSales.getUpdatedAt() != null ? purchaseSales.getUpdatedAt().toString() : null);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed while saving purchase/sales entry");
            return false;
        }
    }

    public boolean update(PurchaseSales purchaseSales) {

        String sql =
                "UPDATE purchase_sales SET " +
                        "bill_no = ?, entry_date = ?, purchase_type = ?, purchase_from = ?, customer_uuid = ?, " +
                        "description = ?, purchase_remark = ?, sales_remark = ?, qty = ?, purchase_rate = ?, " +
                        "sell_rate = ?, total_purchase = ?, total_sale = ?, profit = ?, payment_mode = ?, " +
                        "pnr_no = ?, sector = ?, airline_name = ?, travel_date = ?, linked_trip_uuid = ?, " +
                        "status = ?, updated_at = ? " +
                        "WHERE uuid = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            purchaseSales.calculateTotals();
            purchaseSales.touch();

            ps.setString(1, purchaseSales.getBillNo());
            ps.setString(2, purchaseSales.getEntryDate() != null ? purchaseSales.getEntryDate().toString() : null);
            ps.setString(3, purchaseSales.getPurchaseType());
            ps.setString(4, purchaseSales.getPurchaseFrom());
            ps.setString(5, purchaseSales.getCustomerUuid());
            ps.setString(6, purchaseSales.getDescription());
            ps.setString(7, purchaseSales.getPurchaseRemark());
            ps.setString(8, purchaseSales.getSalesRemark());
            ps.setInt(9, purchaseSales.getQty());
            ps.setDouble(10, purchaseSales.getPurchaseRate());
            ps.setDouble(11, purchaseSales.getSellRate());
            ps.setDouble(12, purchaseSales.getTotalPurchase());
            ps.setDouble(13, purchaseSales.getTotalSale());
            ps.setDouble(14, purchaseSales.getProfit());
            ps.setString(15, purchaseSales.getPaymentMode());
            ps.setString(16, purchaseSales.getPnrNo());
            ps.setString(17, purchaseSales.getSector());
            ps.setString(18, purchaseSales.getAirlineName());
            ps.setString(19, purchaseSales.getTravelDate() != null ? purchaseSales.getTravelDate().toString() : null);
            ps.setString(20, purchaseSales.getLinkedTripUuid());
            ps.setString(21, purchaseSales.getStatus());
            ps.setString(22, purchaseSales.getUpdatedAt().toString());
            ps.setString(23, purchaseSales.getUuid());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed while updating purchase/sales entry");
            return false;
        }
    }

    public boolean cancel(String uuid) {

        String sql =
                "UPDATE purchase_sales SET status = ?, updated_at = ? WHERE uuid = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "CANCELLED");
            ps.setString(2, LocalDateTime.now().toString());
            ps.setString(3, uuid);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed while cancelling purchase/sales entry");
            return false;
        }
    }

    public PurchaseSales findByUuid(String uuid) {

        String sql = "SELECT * FROM purchase_sales WHERE uuid = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToPurchaseSales(rs);
            }

        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed while finding purchase/sales by uuid");
        }

        return null;
    }

    public List<PurchaseSales> findAll() {

        List<PurchaseSales> entries = new ArrayList<>();

        String sql =
                "SELECT * FROM purchase_sales ORDER BY entry_date DESC, bill_no DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                entries.add(mapResultSetToPurchaseSales(rs));
            }

        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed while fetching purchase/sales entries");
        }

        return entries;
    }

    public List<PurchaseSales> search(String keyword) {

        List<PurchaseSales> entries = new ArrayList<>();

        String sql =
                "SELECT * FROM purchase_sales " +
                        "WHERE LOWER(bill_no) LIKE ? " +
                        "OR LOWER(description) LIKE ? " +
                        "OR LOWER(pnr_no) LIKE ? " +
                        "OR LOWER(sector) LIKE ? " +
                        "ORDER BY entry_date DESC, bill_no DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String value = "%" + keyword.toLowerCase() + "%";

            ps.setString(1, value);
            ps.setString(2, value);
            ps.setString(3, value);
            ps.setString(4, value);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                entries.add(mapResultSetToPurchaseSales(rs));
            }

        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed while searching purchase/sales entries");
        }

        return entries;
    }

    private String generateBillNo(Connection conn) {

        String sql =
                "SELECT COALESCE(MAX(CAST(bill_no AS INTEGER)), 0) + 1 FROM purchase_sales " +
                        "WHERE bill_no GLOB '[0-9]*'";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int next = rs.getInt(1);
                return String.format("%04d", next);
            }

        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed while generating bill number");
        }

        return "0001";
    }

    private PurchaseSales mapResultSetToPurchaseSales(ResultSet rs) throws Exception {

        PurchaseSales purchaseSales = new PurchaseSales();

        purchaseSales.setUuid(rs.getString("uuid"));
        purchaseSales.setBillNo(rs.getString("bill_no"));

        String entryDate = rs.getString("entry_date");
        if (entryDate != null && !entryDate.isBlank()) {
            purchaseSales.setEntryDate(LocalDate.parse(entryDate));
        }

        purchaseSales.setPurchaseType(rs.getString("purchase_type"));
        purchaseSales.setPurchaseFrom(rs.getString("purchase_from"));
        purchaseSales.setCustomerUuid(rs.getString("customer_uuid"));
        purchaseSales.setDescription(rs.getString("description"));
        purchaseSales.setPurchaseRemark(rs.getString("purchase_remark"));
        purchaseSales.setSalesRemark(rs.getString("sales_remark"));

        purchaseSales.setQty(rs.getInt("qty"));
        purchaseSales.setPurchaseRate(rs.getDouble("purchase_rate"));
        purchaseSales.setSellRate(rs.getDouble("sell_rate"));

        purchaseSales.setPaymentMode(rs.getString("payment_mode"));
        purchaseSales.setPnrNo(rs.getString("pnr_no"));
        purchaseSales.setSector(rs.getString("sector"));
        purchaseSales.setAirlineName(rs.getString("airline_name"));

        String travelDate = rs.getString("travel_date");
        if (travelDate != null && !travelDate.isBlank()) {
            purchaseSales.setTravelDate(LocalDate.parse(travelDate));
        }

        purchaseSales.setLinkedTripUuid(rs.getString("linked_trip_uuid"));
        purchaseSales.setStatus(rs.getString("status"));

        String createdAt = rs.getString("created_at");
        if (createdAt != null && !createdAt.isBlank()) {
            purchaseSales.setCreatedAt(LocalDateTime.parse(createdAt));
        }

        String updatedAt = rs.getString("updated_at");
        if (updatedAt != null && !updatedAt.isBlank()) {
            purchaseSales.setUpdatedAt(LocalDateTime.parse(updatedAt));
        }

        purchaseSales.calculateTotals();

        return purchaseSales;
    }

    public String getNextBillNo() {
        try (Connection conn = DBConnection.getConnection()) {
            return generateBillNo(conn);
        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed while getting next bill no");
            return "0001";
        }
    }


}