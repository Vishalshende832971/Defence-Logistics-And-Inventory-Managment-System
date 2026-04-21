package com.defence.manager;

import com.defence.db.DatabaseManager;
import com.defence.model.DefenceItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * InventoryManager — MySQL-backed CRUD operations on the equipment table.
 */
public class InventoryManager {

    // ── Add a new item (ID is auto-incremented) ────────────────
    public boolean addItem(DefenceItem item) {
        String sql = "INSERT INTO equipment (item_name, category, quantity, "
                   + "reorder_level, description) VALUES (?, ?, ?, ?, ?)";
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return false;

        try (PreparedStatement ps = conn.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getItemName());
            ps.setString(2, item.getCategory());
            ps.setInt(3, item.getQuantity());
            ps.setInt(4, item.getReorderLevel());
            ps.setString(5, item.getDescription());
            int rows = ps.executeUpdate();

            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    item.setItemId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ── Get all items ──────────────────────────────────────────
    public List<DefenceItem> getAllItems() {
        List<DefenceItem> items = new ArrayList<>();
        String sql = "SELECT * FROM equipment ORDER BY item_id";
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return items;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // ── Get item by ID ─────────────────────────────────────────
    public DefenceItem getItemById(int itemId) {
        String sql = "SELECT * FROM equipment WHERE item_id = ?";
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return null;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get a map of item ID → display string "ID - ItemName (Category)"
     * for use in combo boxes.
     */
    public Map<Integer, String> getItemComboMap() {
        Map<Integer, String> map = new LinkedHashMap<>();
        String sql = "SELECT item_id, item_name, category FROM equipment ORDER BY item_id";
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return map;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("item_id");
                String name = rs.getString("item_name");
                String cat  = rs.getString("category");
                map.put(id, id + " — " + name + " [" + cat + "]");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * Issue an item — reduce quantity.
     * @return  1 = success, 2 = success + low stock, -1 = no stock, 0 = not found
     */
    public int issueItem(int itemId, int quantity) {
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return 0;

        String updateSql = "UPDATE equipment SET quantity = quantity - ? "
                         + "WHERE item_id = ? AND quantity >= ?";
        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, itemId);
            ps.setInt(3, quantity);
            int rows = ps.executeUpdate();

            if (rows == 0) {
                DefenceItem item = getItemById(itemId);
                if (item == null) return 0;
                return -1;
            }

            DefenceItem updated = getItemById(itemId);
            if (updated != null && updated.getQuantity() < updated.getReorderLevel()) {
                return 2;
            }
            return 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ── Return an item ─────────────────────────────────────────
    public boolean returnItem(int itemId, int quantity) {
        String sql = "UPDATE equipment SET quantity = quantity + ? WHERE item_id = ?";
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return false;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ── Remove an item ─────────────────────────────────────────
    public boolean removeItem(int itemId) {
        String sql = "DELETE FROM equipment WHERE item_id = ?";
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return false;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ── Search / filter items ──────────────────────────────────
    public List<DefenceItem> searchItems(String keyword, String category) {
        List<DefenceItem> items = new ArrayList<>();
        String sql = "SELECT * FROM equipment "
                   + "WHERE (item_name LIKE ? OR CAST(item_id AS CHAR) LIKE ?) "
                   + "AND (category = ? OR ? = 'ALL') "
                   + "ORDER BY item_id";
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return items;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, category);
            ps.setString(4, category);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                items.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // ── Get items below reorder level ──────────────────────────
    public List<DefenceItem> getLowStockItems() {
        List<DefenceItem> items = new ArrayList<>();
        String sql = "SELECT * FROM equipment WHERE quantity < reorder_level "
                   + "ORDER BY quantity ASC";
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return items;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    // ── Get total item count ───────────────────────────────────
    public int getTotalItemCount() {
        String sql = "SELECT COUNT(*) FROM equipment";
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return 0;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ── Category-wise summary for reports ──────────────────────
    public Map<String, int[]> getCategorySummary() {
        Map<String, int[]> summary = new LinkedHashMap<>();
        String sql = "SELECT category, COUNT(*) AS cnt, SUM(quantity) AS total_qty "
                   + "FROM equipment GROUP BY category ORDER BY category";
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return summary;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                summary.put(rs.getString("category"),
                    new int[]{rs.getInt("cnt"), rs.getInt("total_qty")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return summary;
    }

    // ── Map ResultSet row to DefenceItem ───────────────────────
    private DefenceItem mapRow(ResultSet rs) throws SQLException {
        DefenceItem item = new DefenceItem();
        item.setItemId(rs.getInt("item_id"));
        item.setItemName(rs.getString("item_name"));
        item.setCategory(rs.getString("category"));
        item.setQuantity(rs.getInt("quantity"));
        item.setReorderLevel(rs.getInt("reorder_level"));
        item.setDescription(rs.getString("description"));
        item.setCreatedAt(rs.getTimestamp("created_at"));
        item.setUpdatedAt(rs.getTimestamp("updated_at"));
        return item;
    }
}
