package KP_TOURS.repository;

import KP_TOURS.db.DBConnection;
import KP_TOURS.model.LedgerRow;
import KP_TOURS.model.LedgerSummaryDTO;
import KP_TOURS.util.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AccountLedgerRepository {

    public LedgerSummaryDTO generateLedger(
            String accountUuid,
            String accountGroup,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        LedgerSummaryDTO summary =
                new LedgerSummaryDTO();

        List<LedgerRow> rows =
                new ArrayList<>();

        boolean isDebtor =
                "Debtor".equalsIgnoreCase(accountGroup);

        boolean isCreditor =
                "Creditor".equalsIgnoreCase(accountGroup);

        rows.addAll(
                fetchPurchaseSalesRows(
                        accountUuid,
                        isDebtor,
                        isCreditor,
                        fromDate,
                        toDate
                )
        );

        rows.addAll(
                fetchPayReceiveRows(
                        accountUuid,
                        isDebtor,
                        isCreditor,
                        fromDate,
                        toDate
                )
        );

        rows.sort(
                Comparator.comparing(LedgerRow::getDate)
        );

        double runningBalance = 0;
        double totalProfit = 0;
        double totalDebit = 0;
        double totalCredit = 0;

        for (LedgerRow row : rows) {

            totalDebit += row.getDebit();
            totalCredit += row.getCredit();

            runningBalance =
                    runningBalance
                            + row.getDebit()
                            - row.getCredit();

            row.setRunningBalance(
                    runningBalance
            );
        }

        if (isDebtor) {
            totalProfit =
                    calculateDebtorProfit(
                            accountUuid,
                            fromDate,
                            toDate
                    );
        }

        summary.setRows(rows);

        summary.setOutstanding(runningBalance);

        summary.setClosingBalance(runningBalance);

        summary.setTotalProfit(totalProfit);

        summary.setTotalDebit(totalDebit);

        summary.setTotalCredit(totalCredit);

        return summary;
    }

    private List<LedgerRow> fetchPurchaseSalesRows(
            String accountUuid,
            boolean isDebtor,
            boolean isCreditor,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        List<LedgerRow> rows =
                new ArrayList<>();

        String columnName;
        String amountColumn;
        String remarkColumn;

        if (isDebtor) {
            columnName = "customer_uuid";
            amountColumn = "total_sale";
            remarkColumn = "sales_remark";
        } else if (isCreditor) {
            columnName = "purchase_from";
            amountColumn = "total_purchase";
            remarkColumn = "purchase_remark";
        } else {
            return rows;
        }

        String sql =
                "SELECT " +
                        "entry_date, " +
                        "bill_no, " +
                        "description, " +
                        amountColumn + " AS amount, " +
                        remarkColumn + " AS remark " +
                        "FROM purchase_sales " +
                        "WHERE " + columnName + " = ? " +
                        "AND IFNULL(status, 'ACTIVE') = 'ACTIVE' " +
                        "AND date(entry_date) BETWEEN date(?) AND date(?) " +
                        "ORDER BY date(entry_date), bill_no";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountUuid);
            ps.setString(2, fromDate.toString());
            ps.setString(3, toDate.toString());

            ResultSet rs =
                    ps.executeQuery();

            while (rs.next()) {

                LedgerRow row =
                        new LedgerRow();

                row.setDate(
                        rs.getString("entry_date")
                );

                row.setParticulars(
                        rs.getString("description")
                );

                row.setRemark(
                        rs.getString("remark")
                );

                row.setVoucherType(
                        isDebtor ? "BILL" : "PURCHASE"
                );

                row.setVoucherNo(
                        rs.getString("bill_no")
                );

                double amount =
                        rs.getDouble("amount");

                if (isDebtor) {

                    row.setDebit(amount);
                    row.setCredit(0);

                } else {

                    row.setDebit(0);
                    row.setCredit(amount);
                }

                rows.add(row);
            }

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed while fetching purchase/sales ledger rows"
            );

            e.printStackTrace();
        }

        return rows;
    }

    private List<LedgerRow> fetchPayReceiveRows(
            String accountUuid,
            boolean isDebtor,
            boolean isCreditor,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        List<LedgerRow> rows =
                new ArrayList<>();

        String sql =
                "SELECT " +
                        "voucher_no, " +
                        "entry_type, " +
                        "entry_date, " +
                        "payment_mode, " +
                        "total_amount, " +
                        "remark " +
                        "FROM pay_receive " +
                        "WHERE account_uuid = ? " +
                        "AND IFNULL(status, 'ACTIVE') = 'ACTIVE' " +
                        "AND date(entry_date) BETWEEN date(?) AND date(?) " +
                        "ORDER BY date(entry_date), voucher_no";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountUuid);
            ps.setString(2, fromDate.toString());
            ps.setString(3, toDate.toString());

            ResultSet rs =
                    ps.executeQuery();

            while (rs.next()) {

                String entryType =
                        rs.getString("entry_type");

                double amount =
                        rs.getDouble("total_amount");

                LedgerRow row =
                        new LedgerRow();

                row.setDate(
                        rs.getString("entry_date")
                );

                row.setParticulars(
                        rs.getString("payment_mode")
                                + " "
                                + ("RECEIVE".equalsIgnoreCase(entryType)
                                ? "Received"
                                : "Paid")
                );

                row.setRemark(
                        rs.getString("remark")
                );

                row.setVoucherType(
                        entryType
                );

                row.setVoucherNo(
                        rs.getString("voucher_no")
                );

                if (isDebtor) {

                    row.setDebit(0);
                    row.setCredit(amount);

                } else if (isCreditor) {

                    row.setDebit(amount);
                    row.setCredit(0);
                }

                rows.add(row);
            }

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed while fetching pay/receive ledger rows"
            );

            e.printStackTrace();
        }

        return rows;
    }

    private double calculateDebtorProfit(
            String accountUuid,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        String sql =
                "SELECT COALESCE(SUM(profit), 0) AS total_profit " +
                        "FROM purchase_sales " +
                        "WHERE customer_uuid = ? " +
                        "AND IFNULL(status, 'ACTIVE') = 'ACTIVE' " +
                        "AND date(entry_date) BETWEEN date(?) AND date(?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountUuid);
            ps.setString(2, fromDate.toString());
            ps.setString(3, toDate.toString());

            ResultSet rs =
                    ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total_profit");
            }

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed while calculating debtor profit"
            );

            e.printStackTrace();
        }

        return 0;
    }
}