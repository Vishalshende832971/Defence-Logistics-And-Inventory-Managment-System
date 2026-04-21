package com.defence.ui;

import com.defence.db.DatabaseManager;
import com.defence.manager.InventoryManager;
import com.defence.manager.TransactionManager;
import com.defence.model.DefenceItem;
import com.defence.model.Transaction;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;

/**
 * ReportsFrame — Generate and export various defence logistics reports.
 */
public class ReportsFrame extends JFrame {

    private static final Color BG_DARK     = new Color(0x1B, 0x2A, 0x1B);
    private static final Color TEXT_CLR    = new Color(0xE8, 0xF5, 0xE8);
    private static final Color BTN_BG      = new Color(0x2E, 0x5E, 0x2E);
    private static final Color BTN_HOVER   = new Color(0x4A, 0x8C, 0x4A);
    private static final Color TABLE_HDR   = new Color(0x1a, 0x3a, 0x1a);
    private static final Color TABLE_ROW1  = new Color(0x1e, 0x2e, 0x1e);
    private static final Color TABLE_ROW2  = new Color(0x22, 0x33, 0x22);
    private static final Color ACCENT      = new Color(0x8F, 0xA8, 0x32);
    private static final Color ALERT_RED   = new Color(0x8B, 0x00, 0x00);

    private JComboBox<String> reportTypeCombo;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel reportInfoLabel;

    private final InventoryManager   invManager = new InventoryManager();
    private final TransactionManager txManager  = new TransactionManager();

    private static final String[] REPORT_TYPES = {
        "📦 Full Inventory Summary",
        "📊 Category-wise Breakdown",
        "📉 Low Stock Alert Report",
        "📜 Transaction History Report",
        "🔧 Maintenance Status Report",
        "🏭 Depot Overview",
        "🚚 Supplier Directory",
        "👤 Personnel Registry"
    };

    public ReportsFrame() {
        setTitle("Defence Logistics — Reports");
        setSize(1000, 560);
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

        JLabel title = new JLabel("📊 REPORT GENERATION", SwingConstants.CENTER);
        title.setFont(new Font("Courier New", Font.BOLD, 18));
        title.setForeground(TEXT_CLR);
        header.add(title, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // ── Control Panel ──────────────────────────────────────
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        controls.setBackground(new Color(0x1a, 0x2a, 0x1a));

        JLabel selectLabel = new JLabel("Report Type:");
        selectLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
        selectLabel.setForeground(TEXT_CLR);
        controls.add(selectLabel);

        reportTypeCombo = new JComboBox<>(REPORT_TYPES);
        reportTypeCombo.setFont(new Font("Tahoma", Font.BOLD, 13));
        reportTypeCombo.setBackground(new Color(0x1e, 0x2e, 0x1e));
        reportTypeCombo.setForeground(TEXT_CLR);
        reportTypeCombo.setPreferredSize(new Dimension(320, 30));
        controls.add(reportTypeCombo);

        JButton generateBtn = makeBtn("⚡ Generate");
        generateBtn.addActionListener(e -> generateReport());
        controls.add(generateBtn);

        JButton exportBtn = makeBtn("📁 Export CSV");
        exportBtn.addActionListener(e -> exportCurrentReport());
        controls.add(exportBtn);

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

        reportInfoLabel = new JLabel("Select a report type and click Generate");
        reportInfoLabel.setFont(new Font("Tahoma", Font.ITALIC, 12));
        reportInfoLabel.setForeground(ACCENT);
        bottom.add(reportInfoLabel, BorderLayout.WEST);
        add(bottom, BorderLayout.SOUTH);
    }

    private void generateReport() {
        int idx = reportTypeCombo.getSelectedIndex();
        tableModel.setRowCount(0);
        tableModel.setColumnCount(0);

        switch (idx) {
            case 0: generateInventorySummary(); break;
            case 1: generateCategoryBreakdown(); break;
            case 2: generateLowStockReport(); break;
            case 3: generateTransactionReport(); break;
            case 4: generateMaintenanceReport(); break;
            case 5: generateTableReport("depot", "Depot Overview"); break;
            case 6: generateTableReport("supplier", "Supplier Directory"); break;
            case 7: generateTableReport("personnel", "Personnel Registry"); break;
        }
    }

    private void generateInventorySummary() {
        String[] cols = {"ID", "Item Name", "Category", "Quantity", "Reorder Lvl", "Status", "Description"};
        for (String c : cols) tableModel.addColumn(c);

        List<DefenceItem> items = invManager.getAllItems();
        for (DefenceItem item : items) {
            String status;
            if (item.getQuantity() == 0) status = "⛔ OUT OF STOCK";
            else if (item.getQuantity() < item.getReorderLevel()) status = "⚠ LOW STOCK";
            else status = "✅ OK";

            tableModel.addRow(new Object[]{
                item.getItemId(), item.getItemName(), item.getCategory(),
                item.getQuantity(), item.getReorderLevel(), status,
                item.getDescription()
            });
        }
        reportInfoLabel.setText("Full Inventory — " + items.size() + " items | Generated");
    }

    private void generateCategoryBreakdown() {
        String[] cols = {"Category", "Total Items", "Total Quantity", "Avg Qty/Item"};
        for (String c : cols) tableModel.addColumn(c);

        Map<String, int[]> summary = invManager.getCategorySummary();
        int totalItems = 0, totalQty = 0;
        for (Map.Entry<String, int[]> entry : summary.entrySet()) {
            int count = entry.getValue()[0];
            int qty   = entry.getValue()[1];
            double avg = count > 0 ? (double) qty / count : 0;
            tableModel.addRow(new Object[]{
                entry.getKey(), count, qty, String.format("%.1f", avg)
            });
            totalItems += count;
            totalQty   += qty;
        }
        tableModel.addRow(new Object[]{"── TOTAL ──", totalItems, totalQty, ""});
        reportInfoLabel.setText("Category Breakdown — " + summary.size() + " categories");
    }

    private void generateLowStockReport() {
        String[] cols = {"ID", "Item Name", "Category", "Current Qty", "Reorder Level", "Deficit"};
        for (String c : cols) tableModel.addColumn(c);

        List<DefenceItem> items = invManager.getLowStockItems();
        for (DefenceItem item : items) {
            int deficit = item.getReorderLevel() - item.getQuantity();
            tableModel.addRow(new Object[]{
                item.getItemId(), item.getItemName(), item.getCategory(),
                item.getQuantity(), item.getReorderLevel(), deficit
            });
        }
        reportInfoLabel.setText("Low Stock Alert — " + items.size() + " items below reorder level");
    }

    private void generateTransactionReport() {
        String[] cols = {"TX ID", "Item ID", "Item Name", "Action", "Quantity", "By", "Timestamp"};
        for (String c : cols) tableModel.addColumn(c);

        List<Transaction> txns = txManager.getAllTransactions();
        int addCount = 0, issueCount = 0, returnCount = 0;
        for (Transaction tx : txns) {
            tableModel.addRow(new Object[]{
                tx.getTransactionId(), tx.getItemId(), tx.getItemName(),
                tx.getAction(), tx.getQuantity(), tx.getPerformedBy(),
                tx.getTimestamp()
            });
            switch (tx.getAction()) {
                case "ADD":    addCount++;    break;
                case "ISSUE":  issueCount++;  break;
                case "RETURN": returnCount++; break;
            }
        }
        reportInfoLabel.setText("Transactions — Total: " + txns.size()
            + " | ADD: " + addCount + " | ISSUE: " + issueCount
            + " | RETURN: " + returnCount);
    }

    private void generateMaintenanceReport() {
        String[] cols = {"MX ID", "Item", "Type", "Status", "Scheduled", "Completed", "Remarks"};
        for (String c : cols) tableModel.addColumn(c);

        String sql = "SELECT m.*, e.item_name FROM maintenance m "
                   + "JOIN equipment e ON m.item_id = e.item_id "
                   + "ORDER BY m.scheduled_date DESC";
        try (java.sql.PreparedStatement ps =
                DatabaseManager.getInstance().getConnection().prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("maintenance_id"),
                    rs.getString("item_name"),
                    rs.getString("maintenance_type"),
                    rs.getString("status"),
                    rs.getDate("scheduled_date"),
                    rs.getDate("completed_date"),
                    rs.getString("remarks")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        reportInfoLabel.setText("Maintenance Report — " + tableModel.getRowCount() + " records");
    }

    private void generateTableReport(String tableName, String label) {
        try (java.sql.Statement stmt = DatabaseManager.getInstance().getConnection().createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {
            java.sql.ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            for (int i = 1; i <= colCount; i++) {
                tableModel.addColumn(meta.getColumnLabel(i).toUpperCase());
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
            reportInfoLabel.setText(label + " — " + rowCount + " records");
        } catch (Exception e) {
            e.printStackTrace();
            reportInfoLabel.setText("Error loading " + tableName);
        }
    }

    private void exportCurrentReport() {
        if (tableModel.getColumnCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "Generate a report first!", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export Report as CSV");
        String reportName = ((String) reportTypeCombo.getSelectedItem())
            .replaceAll("[^a-zA-Z ]", "").trim().replace(" ", "_").toLowerCase();
        fc.setSelectedFile(new java.io.File(reportName + "_report.csv"));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fc.getSelectedFile().getAbsolutePath();
            if (!path.endsWith(".csv")) path += ".csv";
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
                // Header
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    if (i > 0) bw.write(",");
                    bw.write("\"" + tableModel.getColumnName(i) + "\"");
                }
                bw.newLine();
                // Rows
                for (int r = 0; r < tableModel.getRowCount(); r++) {
                    for (int c = 0; c < tableModel.getColumnCount(); c++) {
                        if (c > 0) bw.write(",");
                        Object val = tableModel.getValueAt(r, c);
                        bw.write("\"" + (val != null ? val.toString().replace("\"", "\"\"") : "") + "\"");
                    }
                    bw.newLine();
                }
                JOptionPane.showMessageDialog(this,
                    "✅ Report exported to:\n" + path,
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "❌ Export failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
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
