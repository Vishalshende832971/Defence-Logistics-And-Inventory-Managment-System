package com.defence.ui;

import com.defence.db.DatabaseManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * DataExplorerFrame — Browse data from all database tables (ER diagram entities).
 * Auto-detects columns using ResultSetMetaData.
 */
public class DataExplorerFrame extends JFrame {

    private static final Color BG_DARK     = new Color(0x1B, 0x2A, 0x1B);
    private static final Color TEXT_CLR    = new Color(0xE8, 0xF5, 0xE8);
    private static final Color BTN_BG      = new Color(0x2E, 0x5E, 0x2E);
    private static final Color BTN_HOVER   = new Color(0x4A, 0x8C, 0x4A);
    private static final Color TABLE_HDR   = new Color(0x1a, 0x3a, 0x1a);
    private static final Color TABLE_ROW1  = new Color(0x1e, 0x2e, 0x1e);
    private static final Color TABLE_ROW2  = new Color(0x22, 0x33, 0x22);
    private static final Color ACCENT      = new Color(0x8F, 0xA8, 0x32);

    private JComboBox<String> tableCombo;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel infoLabel;

    // All tables from the ER diagram
    private static final String[][] TABLE_OPTIONS = {
        {"equipment",        "🛡 Equipment (Defence Items)"},
        {"users",            "👤 Users (Login Accounts)"},
        {"transactions",     "📜 Transactions (Audit Log)"},
        {"depot",            "🏭 Depot (Storage Bases)"},
        {"inventory",        "📦 Inventory (Depot Stock)"},
        {"supplier",         "🚚 Supplier (Vendors)"},
        {"orders",           "📋 Orders (Procurement)"},
        {"order_details",    "📄 Order Details (Line Items)"},
        {"shipment",         "✈ Shipment (Transfers)"},
        {"shipment_details", "📄 Shipment Details"},
        {"maintenance",      "🔧 Maintenance (Service Log)"},
        {"personnel",        "🎖 Personnel (Military Staff)"}
    };

    public DataExplorerFrame() {
        setTitle("Defence Logistics — Database Explorer");
        setSize(1050, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(BG_DARK);
        initializeUI();
        setVisible(true);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // ── Header ─────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0x14, 0x20, 0x14));
        header.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JButton back = makeBtn("◄ Back");
        back.setPreferredSize(new Dimension(100, 32));
        back.addActionListener(e -> { new DashboardFrame(); dispose(); });
        header.add(back, BorderLayout.WEST);

        JLabel title = new JLabel("🗄 DATABASE EXPLORER", SwingConstants.CENTER);
        title.setFont(new Font("Courier New", Font.BOLD, 18));
        title.setForeground(TEXT_CLR);
        header.add(title, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // ── Control bar ────────────────────────────────────────
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        controls.setBackground(new Color(0x1a, 0x2a, 0x1a));

        JLabel selectLabel = new JLabel("Select Table:");
        selectLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
        selectLabel.setForeground(TEXT_CLR);
        controls.add(selectLabel);

        String[] displayNames = new String[TABLE_OPTIONS.length];
        for (int i = 0; i < TABLE_OPTIONS.length; i++) {
            displayNames[i] = TABLE_OPTIONS[i][1];
        }
        tableCombo = new JComboBox<>(displayNames);
        tableCombo.setFont(new Font("Tahoma", Font.BOLD, 13));
        tableCombo.setBackground(new Color(0x1e, 0x2e, 0x1e));
        tableCombo.setForeground(TEXT_CLR);
        tableCombo.setPreferredSize(new Dimension(320, 30));
        controls.add(tableCombo);

        JButton loadBtn = makeBtn("🔍 Load Data");
        loadBtn.addActionListener(e -> loadTableData());
        controls.add(loadBtn);

        JButton refreshBtn = makeBtn("↻ Refresh");
        refreshBtn.addActionListener(e -> loadTableData());
        controls.add(refreshBtn);

        JPanel midPanel = new JPanel(new BorderLayout());
        midPanel.setBackground(BG_DARK);
        midPanel.add(controls, BorderLayout.NORTH);

        // ── Table ──────────────────────────────────────────────
        tableModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Tahoma", Font.PLAIN, 12));
        table.setForeground(TEXT_CLR);
        table.setBackground(TABLE_ROW1);
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(0x30, 0x40, 0x30));
        table.setRowHeight(26);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getTableHeader().setFont(new Font("Courier New", Font.BOLD, 12));
        table.getTableHeader().setBackground(TABLE_HDR);
        table.getTableHeader().setForeground(TEXT_CLR);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (!s) {
                    comp.setBackground(r % 2 == 0 ? TABLE_ROW1 : TABLE_ROW2);
                    comp.setForeground(TEXT_CLR);
                }
                return comp;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(BG_DARK);
        midPanel.add(scroll, BorderLayout.CENTER);
        add(midPanel, BorderLayout.CENTER);

        // ── Bottom info ────────────────────────────────────────
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(new Color(0x14, 0x20, 0x14));
        bottom.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));

        infoLabel = new JLabel("Select a table and click 'Load Data'");
        infoLabel.setFont(new Font("Tahoma", Font.ITALIC, 12));
        infoLabel.setForeground(ACCENT);
        bottom.add(infoLabel, BorderLayout.WEST);

        // Schema info button
        JButton schemaBtn = makeBtn("📐 Show Schema");
        schemaBtn.addActionListener(e -> showTableSchema());
        bottom.add(schemaBtn, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);

        // Auto-load first table
        tableCombo.addActionListener(e -> loadTableData());
    }

    private void loadTableData() {
        int idx = tableCombo.getSelectedIndex();
        if (idx < 0) return;

        String tableName = TABLE_OPTIONS[idx][0];
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) {
            infoLabel.setText("❌ Database connection failed!");
            return;
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            // Add columns with proper labels
            for (int i = 1; i <= colCount; i++) {
                String colName = meta.getColumnLabel(i).toUpperCase()
                    .replace("_", " ");
                tableModel.addColumn(colName);
            }

            int rowCount = 0;
            while (rs.next()) {
                Object[] row = new Object[colCount];
                for (int i = 1; i <= colCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                tableModel.addRow(row);
                rowCount++;
            }

            infoLabel.setText("Table: " + tableName + "  |  "
                + rowCount + " rows  |  " + colCount + " columns");

        } catch (SQLException e) {
            e.printStackTrace();
            infoLabel.setText("❌ Error loading table: " + e.getMessage());
        }
    }

    private void showTableSchema() {
        int idx = tableCombo.getSelectedIndex();
        if (idx < 0) return;

        String tableName = TABLE_OPTIONS[idx][0];
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("TABLE: ").append(tableName.toUpperCase()).append("\n");
        sb.append("─".repeat(45)).append("\n");

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("DESCRIBE " + tableName)) {
            sb.append(String.format("%-20s %-20s %-6s %-6s\n",
                "COLUMN", "TYPE", "NULL", "KEY"));
            sb.append("─".repeat(45)).append("\n");
            while (rs.next()) {
                sb.append(String.format("%-20s %-20s %-6s %-6s\n",
                    rs.getString("Field"),
                    rs.getString("Type"),
                    rs.getString("Null"),
                    rs.getString("Key")));
            }
        } catch (SQLException e) {
            sb.append("Error: ").append(e.getMessage());
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        textArea.setBackground(new Color(0x1e, 0x2e, 0x1e));
        textArea.setForeground(TEXT_CLR);
        textArea.setEditable(false);

        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(500, 300));

        JOptionPane.showMessageDialog(this, scroll,
            "Schema — " + tableName.toUpperCase(),
            JOptionPane.PLAIN_MESSAGE);
    }

    private JButton makeBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Tahoma", Font.BOLD, 12));
        btn.setBackground(BTN_BG);
        btn.setForeground(TEXT_CLR);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(8));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(BTN_HOVER); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(BTN_BG); }
        });
        return btn;
    }
}
