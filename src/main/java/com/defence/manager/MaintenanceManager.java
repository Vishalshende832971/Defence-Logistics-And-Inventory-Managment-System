package com.defence.manager;

import com.defence.db.DatabaseManager;
import com.defence.model.MaintenanceRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MaintenanceManager — MySQL-backed operations on the maintenance table.
 */
public class MaintenanceManager {

    // ── Schedule new maintenance ───────────────────────────────
    public boolean scheduleMaintenance(MaintenanceRecord rec) {
        String sql = "INSERT INTO maintenance (item_id, personnel_id, "
                   + "maintenance_type, status, scheduled_date, remarks) "
                   + "VALUES (?, ?, ?, 'PENDING', ?, ?)";
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return false;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rec.getItemId());
            if (rec.getPersonnelId() > 0) {
                ps.setInt(2, rec.getPersonnelId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, rec.getMaintenanceType());
            ps.setDate(4, rec.getScheduledDate());
            ps.setString(5, rec.getRemarks());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ── Update status of a maintenance record ──────────────────
    public boolean updateStatus(int maintenanceId, String newStatus) {
        String sql;
        if ("COMPLETED".equals(newStatus)) {
            sql = "UPDATE maintenance SET status = ?, completed_date = CURDATE() "
                + "WHERE maintenance_id = ?";
        } else {
            sql = "UPDATE maintenance SET status = ? WHERE maintenance_id = ?";
        }
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return false;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, maintenanceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ── Get maintenance records for a specific item ────────────
    public List<MaintenanceRecord> getMaintenanceByItem(int itemId) {
        List<MaintenanceRecord> list = new ArrayList<>();
        String sql = "SELECT m.*, e.item_name FROM maintenance m "
                   + "JOIN equipment e ON m.item_id = e.item_id "
                   + "WHERE m.item_id = ? ORDER BY m.scheduled_date DESC";
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return list;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── Get overdue maintenance (scheduled before today, not completed) ─
    public List<MaintenanceRecord> getOverdueMaintenance() {
        List<MaintenanceRecord> list = new ArrayList<>();
        String sql = "SELECT m.*, e.item_name FROM maintenance m "
                   + "JOIN equipment e ON m.item_id = e.item_id "
                   + "WHERE m.scheduled_date < CURDATE() "
                   + "AND m.status != 'COMPLETED' "
                   + "ORDER BY m.scheduled_date ASC";
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

    // ── Get all maintenance records ────────────────────────────
    public List<MaintenanceRecord> getAllMaintenance() {
        List<MaintenanceRecord> list = new ArrayList<>();
        String sql = "SELECT m.*, e.item_name FROM maintenance m "
                   + "JOIN equipment e ON m.item_id = e.item_id "
                   + "ORDER BY m.scheduled_date DESC";
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

    // ── Map ResultSet row to MaintenanceRecord ─────────────────
    private MaintenanceRecord mapRow(ResultSet rs) throws SQLException {
        MaintenanceRecord rec = new MaintenanceRecord();
        rec.setMaintenanceId(rs.getInt("maintenance_id"));
        rec.setItemId(rs.getInt("item_id"));
        rec.setPersonnelId(rs.getInt("personnel_id"));
        rec.setMaintenanceType(rs.getString("maintenance_type"));
        rec.setStatus(rs.getString("status"));
        rec.setScheduledDate(rs.getDate("scheduled_date"));
        rec.setCompletedDate(rs.getDate("completed_date"));
        rec.setRemarks(rs.getString("remarks"));
        rec.setItemName(rs.getString("item_name"));
        return rec;
    }
}
