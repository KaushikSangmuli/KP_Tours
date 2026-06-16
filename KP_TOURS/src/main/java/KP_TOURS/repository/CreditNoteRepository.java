package KP_TOURS.repository;

import KP_TOURS.db.DBConnection;
import KP_TOURS.enums.PayReceiveType;
import KP_TOURS.model.CreditNote;
import KP_TOURS.model.PayReceive;
import KP_TOURS.model.PayReceiveBillAdjustment;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreditNoteRepository {

    private final PayReceiveRepository payReceiveRepository =
            new PayReceiveRepository();


    // ── Next credit note number ───────────────────────────────────────
    public String getNextCreditNoteNo() {
        String sql = """
            SELECT credit_note_no
            FROM credit_notes
            ORDER BY CAST(credit_note_no AS INTEGER) DESC
            LIMIT 1
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int last = Integer.parseInt(rs.getString("credit_note_no"));
                return String.valueOf(last + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "1001";
    }

    // ── Save credit note + post PayReceive entries ────────────────────
    public boolean save(CreditNote cn) {

        String insertCN = """
            INSERT INTO credit_notes (
                uuid, credit_note_no, entry_date,
                purchase_sales_uuid, bill_no, bill_date,
                customer_uuid, creditor_uuid,
                amount, bank_refund, party_refund, diff,
                ticket_no, remark, particulars, payment_mode,
                status, created_at, updated_at,qty, rate, purchase_amount
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """;

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            LocalDateTime now = LocalDateTime.now();
            cn.setCreatedAt(now);
            cn.setUpdatedAt(now);
            cn.calculateDiff();

            // 1. Insert credit note
            try (PreparedStatement ps = conn.prepareStatement(insertCN)) {
                ps.setString(1,  cn.getUuid());
                ps.setString(2,  cn.getCreditNoteNo());
                ps.setString(3,  cn.getEntryDate().toString());
                ps.setString(4,  cn.getPurchaseSalesUuid());
                ps.setString(5,  cn.getBillNo());
                ps.setString(6,  cn.getBillDate() != null
                        ? cn.getBillDate().toString() : null);
                ps.setString(7,  cn.getCustomerUuid());
                ps.setString(8,  cn.getCreditorUuid());
                ps.setDouble(9,  cn.getAmount());
                ps.setDouble(10, cn.getBankRefund());
                ps.setDouble(11, cn.getPartyRefund());
                ps.setDouble(12, cn.getDiff());
                ps.setString(13, cn.getTicketNo());
                ps.setString(14, cn.getRemark());
                ps.setString(15, cn.getParticulars());
                ps.setString(16, cn.getPaymentMode());
                ps.setString(17, cn.getStatus());
                ps.setString(18, now.toString());
                ps.setString(19, now.toString());
                ps.setInt(20,  cn.getQty());
                ps.setDouble( 21, cn.getRate());
                ps.setDouble(22, cn.getPurchaseAmount());
                ps.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);
            conn.close();
            conn = null;

            // 2. PAY entry — party refund going back to customer
            if (cn.getPartyRefund() > 0 && cn.getCustomerUuid() != null) {
                PayReceive pay = new PayReceive();
                pay.setUuid(UUID.randomUUID().toString());
                pay.setVoucherNo(payReceiveRepository
                        .generateNextVoucherNo(PayReceiveType.PAY));
                pay.setEntryType(PayReceiveType.PAY);
                pay.setEntryDate(cn.getEntryDate());
                pay.setAccountUuid(cn.getCustomerUuid());
                pay.setPaymentMode(cn.getPaymentMode() != null
                        ? cn.getPaymentMode() : "CASH");
                pay.setTotalAmount(cn.getPartyRefund());
                pay.setReferenceNo("CN-" + cn.getCreditNoteNo());
                pay.setRemark("Credit Note refund to party | Bill: "
                        + cn.getBillNo());
                pay.setStatus("ACTIVE");

                PayReceiveBillAdjustment payAdj = new PayReceiveBillAdjustment();
                payAdj.setUuid(UUID.randomUUID().toString());
                payAdj.setPurchaseSalesUuid(cn.getPurchaseSalesUuid());
                payAdj.setBillNo(cn.getBillNo());
                payAdj.setAdjustedAmount(cn.getPartyRefund());

                payReceiveRepository.save(pay, List.of(payAdj));
            }

            // 3. RECEIVE entry — bank refund coming back from creditor
            if (cn.getBankRefund() > 0 && cn.getCreditorUuid() != null) {
                PayReceive receive = new PayReceive();
                receive.setUuid(UUID.randomUUID().toString());
                receive.setVoucherNo(payReceiveRepository
                        .generateNextVoucherNo(PayReceiveType.RECEIVE));
                receive.setEntryType(PayReceiveType.RECEIVE);
                receive.setEntryDate(cn.getEntryDate());
                receive.setAccountUuid(cn.getCreditorUuid());
                receive.setPaymentMode(cn.getPaymentMode() != null
                        ? cn.getPaymentMode() : "CASH");
                receive.setTotalAmount(cn.getBankRefund());
                receive.setReferenceNo("CN-" + cn.getCreditNoteNo());
                receive.setRemark("Credit Note bank refund | Bill: "
                        + cn.getBillNo());
                receive.setStatus("ACTIVE");

                PayReceiveBillAdjustment recAdj =
                        new PayReceiveBillAdjustment();
                recAdj.setUuid(UUID.randomUUID().toString());
                recAdj.setPurchaseSalesUuid(cn.getPurchaseSalesUuid());
                recAdj.setBillNo(cn.getBillNo());
                recAdj.setAdjustedAmount(cn.getBankRefund());

                payReceiveRepository.save(receive, List.of(recAdj));
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); conn.setAutoCommit(true); conn.close(); }
                catch (Exception ex) { ex.printStackTrace(); }
            }
            return false;
        }
    }

    // ── Find all ──────────────────────────────────────────────────────
    public List<CreditNote> findAll() {
        List<CreditNote> list = new ArrayList<>();
        String sql = """
            SELECT * FROM credit_notes
            ORDER BY CAST(credit_note_no AS INTEGER) DESC
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ── Search ────────────────────────────────────────────────────────
    public List<CreditNote> search(String keyword) {
        List<CreditNote> list = new ArrayList<>();
        String sql = """
            SELECT * FROM credit_notes
            WHERE LOWER(credit_note_no) LIKE ?
               OR LOWER(bill_no)        LIKE ?
               OR LOWER(ticket_no)      LIKE ?
               OR LOWER(remark)         LIKE ?
               OR LOWER(particulars)    LIKE ?
            ORDER BY CAST(credit_note_no AS INTEGER) DESC
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String v = "%" + keyword.toLowerCase() + "%";
            for (int i = 1; i <= 5; i++) ps.setString(i, v);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ── Cancel ────────────────────────────────────────────────────────
    public boolean cancel(String uuid) {
        String sql = """
            UPDATE credit_notes SET status = 'CANCELLED', updated_at = ?
            WHERE uuid = ?
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, LocalDateTime.now().toString());
            ps.setString(2, uuid);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ── Map ResultSet ─────────────────────────────────────────────────
    private CreditNote map(ResultSet rs) throws Exception {
        CreditNote cn = new CreditNote();
        cn.setUuid(rs.getString("uuid"));
        cn.setCreditNoteNo(rs.getString("credit_note_no"));
        cn.setEntryDate(LocalDate.parse(rs.getString("entry_date")));
        cn.setPurchaseSalesUuid(rs.getString("purchase_sales_uuid"));
        cn.setBillNo(rs.getString("bill_no"));
        String bd = rs.getString("bill_date");
        if (bd != null && !bd.isBlank()) cn.setBillDate(LocalDate.parse(bd));
        cn.setCustomerUuid(rs.getString("customer_uuid"));
        cn.setCreditorUuid(rs.getString("creditor_uuid"));
        cn.setAmount(rs.getDouble("amount"));
        cn.setBankRefund(rs.getDouble("bank_refund"));
        cn.setPartyRefund(rs.getDouble("party_refund"));
        cn.setTicketNo(rs.getString("ticket_no"));
        cn.setRemark(rs.getString("remark"));
        cn.setParticulars(rs.getString("particulars"));
        cn.setPaymentMode(rs.getString("payment_mode"));
        cn.setStatus(rs.getString("status"));
        cn.setQty(rs.getInt("qty"));
        cn.setRate(rs.getDouble("rate"));
        cn.setPurchaseAmount(rs.getDouble("purchase_amount"));
        String cat = rs.getString("created_at");
        if (cat != null) cn.setCreatedAt(LocalDateTime.parse(cat));
        String uat = rs.getString("updated_at");
        if (uat != null) cn.setUpdatedAt(LocalDateTime.parse(uat));
        return cn;
    }
}