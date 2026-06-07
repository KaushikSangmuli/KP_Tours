package KP_TOURS.repository;


import KP_TOURS.db.DBConnection;
import KP_TOURS.enums.BillStatus;
import KP_TOURS.enums.PayReceiveType;
import KP_TOURS.model.OutstandingBill;
import KP_TOURS.model.PayReceive;
import KP_TOURS.model.PayReceiveBillAdjustment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PayReceiveRepository {

    public String generateNextVoucherNo(PayReceiveType type) {

        String prefix = type == PayReceiveType.RECEIVE ? "R" : "P";

        String sql = """
                SELECT voucher_no 
                FROM pay_receive 
                WHERE entry_type = ?
                ORDER BY CAST(SUBSTR(voucher_no, 2) AS INTEGER) DESC
                LIMIT 1
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, type.name());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String lastVoucherNo = rs.getString("voucher_no");
                int lastNumber = Integer.parseInt(lastVoucherNo.substring(1));
                return prefix + String.format("%04d", lastNumber + 1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return prefix + "0001";
    }

    public boolean save(
            PayReceive payReceive,
            List<PayReceiveBillAdjustment> adjustments
    ) {
        String insertPayReceive = """
        INSERT INTO pay_receive (
            uuid,
            voucher_no,
            entry_type,
            entry_date,
            account_uuid,
            payment_mode,
            total_amount,
            reference_no,
            remark,
            status,
            created_at,
            updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        String insertAdjustment = """
                INSERT INTO pay_receive_bill_adjustment (
                    uuid,
                    pay_receive_uuid,
                    purchase_sales_uuid,
                    bill_no,
                    adjusted_amount,
                    created_at,
                    updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        Connection connection = null;

        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            LocalDateTime now = LocalDateTime.now();

            if (payReceive.getUuid() == null || payReceive.getUuid().isBlank()) {
                payReceive.setUuid(UUID.randomUUID().toString());
            }

            payReceive.setCreatedAt(now);
            payReceive.setUpdatedAt(now);

            try (PreparedStatement ps = connection.prepareStatement(insertPayReceive)) {

                ps.setString(1, payReceive.getUuid());
                ps.setString(2, payReceive.getVoucherNo());
                ps.setString(3, payReceive.getEntryType().name());
                ps.setString(4, payReceive.getEntryDate().toString());
                ps.setString(5, payReceive.getAccountUuid());
                ps.setString(6, payReceive.getPaymentMode());
                ps.setDouble(7, payReceive.getTotalAmount());
                ps.setString(8, payReceive.getReferenceNo());
                ps.setString(9, payReceive.getRemark());
                ps.setString(10, payReceive.getStatus() == null ? "ACTIVE" : payReceive.getStatus());
                ps.setString(11, now.toString());
                ps.setString(12, now.toString());
                ps.executeUpdate();
            }

            try (PreparedStatement ps = connection.prepareStatement(insertAdjustment)) {

                for (PayReceiveBillAdjustment adjustment : adjustments) {

                    if (adjustment.getUuid() == null || adjustment.getUuid().isBlank()) {
                        adjustment.setUuid(UUID.randomUUID().toString());
                    }

                    adjustment.setPayReceiveUuid(payReceive.getUuid());
                    adjustment.setCreatedAt(now);
                    adjustment.setUpdatedAt(now);

                    ps.setString(1, adjustment.getUuid());
                    ps.setString(2, adjustment.getPayReceiveUuid());
                    ps.setString(3, adjustment.getPurchaseSalesUuid());
                    ps.setString(4, adjustment.getBillNo());
                    ps.setDouble(5, adjustment.getAdjustedAmount());
                    ps.setString(6, now.toString());
                    ps.setString(7, now.toString());

                    ps.addBatch();
                }

                ps.executeBatch();
            }

            connection.commit();
            return true;

        } catch (Exception e) {

            e.printStackTrace();

            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
            }

            return false;

        } finally {

            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public double getTotalAdjustedAmountForBill(String purchaseSalesUuid) {

        String sql = """
                SELECT COALESCE(SUM(adjusted_amount), 0) AS total_adjusted
                FROM pay_receive_bill_adjustment
                WHERE purchase_sales_uuid = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, purchaseSalesUuid);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total_adjusted");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public double getOutstandingForBill(String purchaseSalesUuid, double billTotal) {
        double adjustedAmount = getTotalAdjustedAmountForBill(purchaseSalesUuid);
        return billTotal - adjustedAmount;
    }

    public List<OutstandingBill> getOutstandingBillsForAccount(
            String accountUuid,
            PayReceiveType type
    ) {


        System.out.println("QUERY ACCOUNT UUID : " + accountUuid);
        System.out.println("TYPE               : " + type);

        List<OutstandingBill> bills = new ArrayList<>();

        String accountColumn = type == PayReceiveType.RECEIVE
                ? "customer_uuid"
                : "purchase_from";

        String billAmountColumn = type == PayReceiveType.RECEIVE
                ? "total_sale"
                : "total_purchase";

        String sql = """
            SELECT
                ps.uuid AS purchase_sales_uuid,
                ps.bill_no AS bill_no,
                ps.%s AS account_uuid,
                ps.%s AS bill_amount,
                COALESCE(SUM(
                        CASE
                            WHEN pr.uuid IS NOT NULL THEN adj.adjusted_amount
                            ELSE 0
                        END
                    ), 0) AS adjusted_amount
            FROM purchase_sales ps
                LEFT JOIN pay_receive_bill_adjustment adj
                    ON adj.purchase_sales_uuid = ps.uuid
                LEFT JOIN pay_receive pr
                    ON pr.uuid = adj.pay_receive_uuid
                   AND IFNULL(pr.status, 'ACTIVE') = 'ACTIVE'
            WHERE ps.%s = ?
              AND IFNULL(ps.status, 'ACTIVE') = 'ACTIVE'
              AND ps.payment_mode = 'CREDIT'
            GROUP BY
                ps.uuid,
                ps.bill_no,
                ps.%s,
                ps.%s
            ORDER BY CAST(ps.bill_no AS INTEGER) ASC
            """
                .formatted(
                        accountColumn,
                        billAmountColumn,
                        accountColumn,
                        accountColumn,
                        billAmountColumn
                );

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, accountUuid);

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {

                double billAmount =
                        rs.getDouble("bill_amount");

                double adjustedAmount =
                        rs.getDouble("adjusted_amount");

                double outstandingAmount =
                        billAmount - adjustedAmount;

                BillStatus status;

                if (adjustedAmount <= 0) {

                    status = BillStatus.PENDING;

                } else if (outstandingAmount <= 0) {

                    status = BillStatus.PAYMENT_COMPLETED;

                } else {

                    status = BillStatus.PARTIAL;
                }

                OutstandingBill bill =
                        new OutstandingBill();

                bill.setPurchaseSalesUuid(
                        rs.getString("purchase_sales_uuid")
                );

                bill.setBillNo(
                        rs.getString("bill_no")
                );

                bill.setAccountUuid(
                        rs.getString("account_uuid")
                );

                bill.setAccountName("");

                bill.setBillAmount(
                        billAmount
                );

                bill.setAdjustedAmount(
                        adjustedAmount
                );

                bill.setOutstandingAmount(
                        outstandingAmount
                );

                bill.setStatus(
                        status.name()
                );

                bills.add(bill);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return bills;
    }



    public List<PayReceive> findAll() {

        List<PayReceive> list = new ArrayList<>();

        String sql = """
            SELECT *
            FROM pay_receive
            ORDER BY entry_date DESC, voucher_no DESC
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToPayReceive(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<PayReceive> search(String keyword) {

        List<PayReceive> list = new ArrayList<>();

        String sql = """
            SELECT *
            FROM pay_receive
            WHERE LOWER(voucher_no) LIKE ?
               OR LOWER(entry_type) LIKE ?
               OR LOWER(payment_mode) LIKE ?
               OR LOWER(status) LIKE ?
            ORDER BY entry_date DESC, voucher_no DESC
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            String value = "%" + keyword.toLowerCase() + "%";

            ps.setString(1, value);
            ps.setString(2, value);
            ps.setString(3, value);
            ps.setString(4, value);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToPayReceive(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private PayReceive mapResultSetToPayReceive(ResultSet rs) throws Exception {

        PayReceive payReceive = new PayReceive();

        payReceive.setUuid(rs.getString("uuid"));
        payReceive.setVoucherNo(rs.getString("voucher_no"));
        payReceive.setEntryType(PayReceiveType.valueOf(rs.getString("entry_type")));
        payReceive.setEntryDate(LocalDate.parse(rs.getString("entry_date")));
        payReceive.setAccountUuid(rs.getString("account_uuid"));
        payReceive.setPaymentMode(rs.getString("payment_mode"));
        payReceive.setTotalAmount(rs.getDouble("total_amount"));
        payReceive.setReferenceNo(rs.getString("reference_no"));
        payReceive.setRemark(rs.getString("remark"));
        payReceive.setStatus(rs.getString("status"));

        String createdAt = rs.getString("created_at");
        if (createdAt != null && !createdAt.isBlank()) {
            payReceive.setCreatedAt(LocalDateTime.parse(createdAt));
        }

        String updatedAt = rs.getString("updated_at");
        if (updatedAt != null && !updatedAt.isBlank()) {
            payReceive.setUpdatedAt(LocalDateTime.parse(updatedAt));
        }

        return payReceive;
    }

    public boolean cancelVoucher(String payReceiveUuid) {

        String sql = """
            UPDATE pay_receive
            SET status = 'CANCELLED',
                updated_at = ?
            WHERE uuid = ?
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, LocalDateTime.now().toString());
            ps.setString(2, payReceiveUuid);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
