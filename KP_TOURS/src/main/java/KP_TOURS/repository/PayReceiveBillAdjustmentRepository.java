package KP_TOURS.repository;
import KP_TOURS.db.DBConnection;
import KP_TOURS.model.PayReceiveBillAdjustment;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PayReceiveBillAdjustmentRepository {

    public List<PayReceiveBillAdjustment> findByPayReceiveUuid(String payReceiveUuid) {

        List<PayReceiveBillAdjustment> list = new ArrayList<>();

        String sql = """
                SELECT *
                FROM pay_receive_bill_adjustment
                WHERE pay_receive_uuid = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, payReceiveUuid);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                PayReceiveBillAdjustment adjustment = new PayReceiveBillAdjustment();

                adjustment.setUuid(rs.getString("uuid"));
                adjustment.setPayReceiveUuid(rs.getString("pay_receive_uuid"));
                adjustment.setPurchaseSalesUuid(rs.getString("purchase_sales_uuid"));
                adjustment.setBillNo(rs.getString("bill_no"));
                adjustment.setAdjustedAmount(rs.getDouble("adjusted_amount"));

                list.add(adjustment);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}