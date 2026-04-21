package com.defence.ui;

import com.defence.manager.InventoryManager;
import com.defence.model.DefenceItem;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * AlertsFrame — Shows equipment below reorder level.
 * RED rows = quantity 0, ORANGE rows = below reorder.
 */
public class AlertsFrame extends JFrame {

    private static final Color BG_DARK     = new Color(0x1B, 0x2A, 0x1B);
    private static final Color TEXT_CLR    = new Color(0xE8, 0xF5, 0xE8);
    private static final Color BTN_BG      = new Color(0x2E, 0x5E, 0x2E);
    private static final Color BTN_HOVER   = new Color(0x4A, 0x8C, 0x4A);
    private static final Color TABLE_HDR   = new Color(0x1a, 0x3a, 0x1a);
    private static final Color ACCENT      = new Color(0x8F, 0xA8, 0x32);
    private static final Color ALERT_RED   = new Color(0x8B, 0x00, 0x00);
    private static final Color ALERT_ORANGE = new Color(0xCC, 0x66, 0x00);

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel alertCountLabel;
    private final InventoryManager invManager = new InventoryManager();

    public AlertsFrame() {
        setTitle("Defence Logistics — Low Stock Alerts");
        setSize(850, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(BG_DARK);

        initializeUI();
        loadAlerts();
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

        JLabel title = new JLabel("⚠ LOW STOCK ALERTS", SwingConstants.CENTER);
        title.setFont(new Font("Courier New", Font.BOLD, 18));
        title.setForeground(new Color(0xFF, 0xAA, 0x00));
        header.add(title, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);

        // ── Table ──────────────────────────────────────────────
        tableModel = new DefaultTableModel(
            new String[]{"Item ID", "Item Name", "Category",
                         "Current Qty", "Reorder Level", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Tahoma", Font.BOLD, 13));
        table.setForeground(TEXT_CLR);
        table.setBackground(new Color(0x1e, 0x2e, 0x1e));
        table.setSelectionBackground(ACCENT);
        table.setGridColor(new Color(0x30, 0x40, 0x30));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Courier New", Font.BOLD, 12));
        table.getTableHeader().setBackground(TABLE_HDR);
        table.getTableHeader().setForeground(TEXT_CLR);

        // Color-coded row renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(
                        tbl, val, sel, foc, row, col);
                if (!sel) {
                    int modelRow = tbl.convertRowIndexToModel(row);
                    int quantity = (int) tableModel.getValueAt(modelRow, 3);
                    if (quantity == 0) {
                        c.setBackground(ALERT_RED);
                        c.setForeground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(0x4A, 0x2E, 0x00));
                        c.setForeground(new Color(0xFF, 0xCC, 0x66));
                    }
                    setFont(new Font("Tahoma", Font.BOLD, 13));
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(BG_DARK);
        add(scrollPane, BorderLayout.CENTER);

        // ── Bottom panel ───────────────────────────────────────
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(new Color(0x14, 0x20, 0x14));
        bottom.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));

        alertCountLabel = new JLabel("0 alert(s)");
        alertCountLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
        alertCountLabel.setForeground(ALERT_ORANGE);
        bottom.add(alertCountLabel, BorderLayout.WEST);

        JButton refreshBtn = makeBtn("↻ Refresh Alerts");
        refreshBtn.addActionListener(e -> loadAlerts());
        bottom.add(refreshBtn, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);
    }

    private void loadAlerts() {
        List<DefenceItem> lowStock = invManager.getLowStockItems();
        tableModel.setRowCount(0);

        for (DefenceItem item : lowStock) {
            String status;
            if (item.getQuantity() == 0) {
                status = "OUT OF STOCK";
            } else {
                status = "LOW STOCK";
            }

            tableModel.addRow(new Object[]{
                item.getItemId(),
                item.getItemName(),
                String.valueOf(item.getCategory()),
                item.getQuantity(),
                item.getReorderLevel(),
                status
            });
        }

        alertCountLabel.setText(lowStock.size() + " alert(s)");

        if (lowStock.isEmpty()) {
            alertCountLabel.setText("✔ All items are above reorder level.");
            alertCountLabel.setForeground(ACCENT);
        } else {
            alertCountLabel.setForeground(ALERT_ORANGE);
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
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(BTN_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(BTN_BG);
            }
        });
        return btn;
    }
}
