package KP_TOURS.repository;

import KP_TOURS.db.DBConnection;
import KP_TOURS.model.Account;
import KP_TOURS.util.LoggerUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AccountRepository {

    // ── Preloaded cache for zero-cost UUID lookups ────────────────────────────
    // Call loadCache() once when you need it (e.g. before rendering a table).
    // The cache is NOT auto-refreshed — call loadCache() again after saves/updates/deletes.

    private Map<String, Account> uuidCache = null;

    /** Preload all accounts into memory. Call once before bulk UUID lookups. */
    public void loadCache() {
        List<Account> all = findAll();
        uuidCache = new HashMap<>(all.size() * 2);
        for (Account a : all) {
            uuidCache.put(a.getUuid(), a);
        }
    }

    /** Clear the cache (e.g. after a save/update/delete). */
    public void clearCache() {
        uuidCache = null;
    }

    /**
     * Find account by UUID.
     * Uses in-memory cache if loadCache() was called, otherwise hits the DB once.
     */
    public Account findByUuid(String uuid) {
        if (uuid == null || uuid.isBlank()) return null;

        // Use cache if available
        if (uuidCache != null) {
            return uuidCache.get(uuid);
        }

        // Fallback: single DB query
        String sql = "SELECT * FROM accounts WHERE uuid = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapAccount(rs);
            }
        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed to find account by UUID");
        }
        return null;
    }

    // ── All existing methods below — unchanged ────────────────────────────────

    public boolean save(Account account) {

        String sql =
                "INSERT INTO accounts (" +
                        "uuid," +
                        "account_no," +
                        "name," +
                        "address," +
                        "city," +
                        "phone_no," +
                        "email," +
                        "account_group," +
                        "created_at," +
                        "updated_at" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            LocalDateTime now = LocalDateTime.now();

            account.setUuid(
                    UUID.randomUUID().toString()
            );

            account.setAccountNo(
                    generateAccountNo()
            );

            account.setCreatedAt(now);
            account.setUpdatedAt(now);

            stmt.setString(1, account.getUuid());
            stmt.setString(2, account.getAccountNo());
            stmt.setString(3, account.getName());
            stmt.setString(4, account.getAddress());
            stmt.setString(5, account.getCity());
            stmt.setString(6, account.getPhoneNo());
            stmt.setString(7, account.getEmail());
            stmt.setString(8, account.getAccountGroup());
            stmt.setString(9, account.getCreatedAt().toString());
            stmt.setString(10, account.getUpdatedAt().toString());

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed to save account"
            );

            return false;
        }
    }

    public List<Account> findAll() {

        List<Account> accounts = new ArrayList<>();

        String sql =
                "SELECT * FROM accounts " +
                        "ORDER BY created_at DESC";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            var rs = stmt.executeQuery();

            while (rs.next()) {

                Account account = new Account();

                account.setUuid(
                        rs.getString("uuid")
                );

                account.setAccountNo(
                        rs.getString("account_no")
                );

                account.setName(
                        rs.getString("name")
                );

                account.setAddress(
                        rs.getString("address")
                );

                account.setCity(
                        rs.getString("city")
                );

                account.setPhoneNo(
                        rs.getString("phone_no")
                );

                account.setEmail(
                        rs.getString("email")
                );

                account.setAccountGroup(
                        rs.getString("account_group")
                );

                accounts.add(account);
            }

        } catch (Exception e) {

            LoggerUtil.logError(
                    e,
                    "Failed to fetch accounts"
            );
        }

        return accounts;
    }

    public List<Account> findAllDebtors() {

        List<Account> accounts = new ArrayList<>();

        String sql =
                "SELECT * FROM accounts " +
                        "WHERE LOWER(TRIM(account_group)) = 'debtor' " +
                        "ORDER BY created_at DESC";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                accounts.add(mapAccount(rs));
            }

        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed to fetch debtors");
        }

        return accounts;
    }

    public List<Account> findAllCreditors() {

        List<Account> accounts = new ArrayList<>();

        String sql =
                "SELECT * FROM accounts " +
                        "WHERE LOWER(TRIM(account_group)) = 'creditor' " +
                        "ORDER BY created_at DESC";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                accounts.add(mapAccount(rs));
            }

        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed to fetch creditors");
        }

        return accounts;
    }

    public List<Account> searchByName(String keyword) {

        List<Account> accounts = new ArrayList<>();

        String sql =
                "SELECT * FROM accounts " +
                        "WHERE LOWER(name) LIKE ? " +
                        "ORDER BY name ASC";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, "%" + keyword.toLowerCase() + "%");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Account account = mapAccount(rs);
                accounts.add(account);
            }

        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed to search accounts");
        }

        return accounts;
    }

    public long countAll() {
        return countByQuery("SELECT COUNT(*) FROM accounts");
    }

    public long countByGroup(String group) {
        return countByQuery(
                "SELECT COUNT(*) FROM accounts WHERE account_group = '" + group + "'"
        );
    }

    private long countByQuery(String sql) {

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getLong(1);
            }

        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed to count accounts");
        }

        return 0;
    }

    private Account mapAccount(ResultSet rs) throws Exception {

        Account account = new Account();

        account.setUuid(rs.getString("uuid"));
        account.setAccountNo(rs.getString("account_no"));
        account.setName(rs.getString("name"));
        account.setAddress(rs.getString("address"));
        account.setCity(rs.getString("city"));
        account.setPhoneNo(rs.getString("phone_no"));
        account.setEmail(rs.getString("email"));
        account.setAccountGroup(rs.getString("account_group"));

        return account;
    }

    private String generateAccountNo() {

        String sql =
                "SELECT COALESCE(MAX(CAST(account_no AS INTEGER)),0) + 1 " +
                        "FROM accounts";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                return String.valueOf(
                        rs.getInt(1)
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            LoggerUtil.logError(
                    e,
                    "Failed to generate account number"
            );
        }

        return String.valueOf(
                System.currentTimeMillis()
        );
    }

    public boolean update(Account account) {

        String sql =
                "UPDATE accounts SET " +
                        "name = ?, " +
                        "address = ?, " +
                        "city = ?, " +
                        "phone_no = ?, " +
                        "email = ?, " +
                        "account_group = ?, " +
                        "updated_at = ? " +
                        "WHERE uuid = ?";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(1, account.getName());
            stmt.setString(2, account.getAddress());
            stmt.setString(3, account.getCity());
            stmt.setString(4, account.getPhoneNo());
            stmt.setString(5, account.getEmail());
            stmt.setString(6, account.getAccountGroup());
            stmt.setString(7, LocalDateTime.now().toString());
            stmt.setString(8, account.getUuid());

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed to update account");
            return false;
        }
    }

    public boolean delete(String uuid) {

        String sql = "DELETE FROM accounts WHERE uuid = ?";

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(1, uuid);

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            LoggerUtil.logError(e, "Failed to delete account");
            return false;
        }
    }
}