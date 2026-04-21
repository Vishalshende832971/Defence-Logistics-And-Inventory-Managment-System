package com.defence.manager;

import com.defence.db.DatabaseManager;
import com.defence.model.Transaction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TransactionManager — MySQL-backed operations on the transactions table.
 */
public class TransactionManager {

    // ── Record a new transaction → INSERT INTO transactions ────
    public boolean recordTransaction(Transaction tx) {
        String sql = "INSERT INTO transactions (item_id, action, quantity, "
                   + "performed_by) VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return false;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tx.getItemId());
            ps.setString(2, tx.getAction());
            ps.setInt(3, tx.getQuantity());
            ps.setString(4, tx.getPerformedBy());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ── Get all transactions (with item name via JOIN) ─────────
    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, e.item_name FROM transactions t "
                   + "JOIN equipment e ON t.item_id = e.item_id "
                   + "ORDER BY t.timestamp DESC";
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return list;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── Clear all transactions (ADMIN only) ────────────────────
    public boolean clearTransactions() {
        String sql = "DELETE FROM transactions";
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return false;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Export all transactions to a CSV file.
     * @param filePath  absolute path chosen via JFileChooser
     * @return true on success
     */
    public boolean exportToCSV(String filePath) {
        List<Transaction> txns = getAllTransactions();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            // CSV header
            bw.write("Transaction ID,Item ID,Item Name,Action,Quantity,"
                   + "Performed By,Timestamp");
            bw.newLine();

            for (Transaction tx : txns) {
                bw.write(String.format("%d,%d,\"%s\",%s,%d,\"%s\",\"%s\"",
                    tx.getTransactionId(),
                    tx.getItemId(),
                    escapeCsv(tx.getItemName()),
                    tx.getAction(),
                    tx.getQuantity(),
                    escapeCsv(tx.getPerformedBy()),
                    tx.getTimestamp()));
                bw.newLine();
            }

            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ── Map ResultSet row to Transaction ───────────────────────
    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction tx = new Transaction();
        tx.setTransactionId(rs.getInt("transaction_id"));
        tx.setItemId(rs.getInt("item_id"));
        tx.setAction(rs.getString("action"));
        tx.setQuantity(rs.getInt("quantity"));
        tx.setPerformedBy(rs.getString("performed_by"));
        tx.setTimestamp(rs.getTimestamp("timestamp"));
        tx.setItemName(rs.getString("item_name"));
        return tx;
    }

    // ── Escape double-quotes in CSV fields ─────────────────────
    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
