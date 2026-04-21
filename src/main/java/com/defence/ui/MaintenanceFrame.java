package com.defence.ui;

import com.defence.manager.InventoryManager;
import com.defence.manager.MaintenanceManager;
import com.defence.model.MaintenanceRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * MaintenanceFrame — Now uses item picker dropdown instead of manual ID entry.
 */
public class MaintenanceFrame extends JFrame {

    private static final Color BG_DARK     = new Color(0x1B, 0x2A, 0x1B);
    private static final Color TEXT_CLR    = new Color(0xE8, 0xF5, 0xE8);
    private static final Color BTN_BG      = new Color(0x2E, 0x5E, 0x2E);
    private static final Color BTN_HOVER   = new Color(0x4A, 0x8C, 0x4A);
    private static final Color TABLE_HDR   = new Color(0x1a, 0x3a, 0x1a);
    private static final Color TABLE_ROW1  = new Color(0x1e, 0x2e, 0x1e);
    private static final Color TABLE_ROW2  = new Color(0x22, 0x33, 0x22);
    private static final Color ACCENT      = new Color(0x8F, 0xA8, 0x32);
    private static final Color ALERT_RED   = new Color(0x8B, 0x00, 0x00);

    private JTable table;
    private DefaultTableModel tableModel;
    private final MaintenanceManager mxManager  = new MaintenanceManager();
    private final InventoryManager   invManager = new InventoryManager();

    public MaintenanceFrame() {
        setTitle("Defence Logistics — Maintenance");
        setSize(1050, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(BG_DARK);
        initializeUI();
        loadMaintenance();
        setVisible(true);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0x14, 0x20, 0x14));
        header.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JButton back = makeBtn("◄ Back");
        back.setPreferredSize(new Dimension(100, 32));
        back.addActionListener(e -> { new DashboardFrame(); dispose(); });
        header.add(back, BorderLayout.WEST);

        JLabel title = new JLabel("🔧 MAINTENANCE SCHEDULE", SwingConstants.CENTER);
        title.setFont(new Font("Courier New", Font.BOLD, 18));
        title.setForeground(TEXT_CLR);
        header.add(title, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(
            new String[]{"MX ID", "Item ID", "Item Name", "Type",
                         "Status", "Scheduled", "Completed", "Remarks"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Tahoma", Font.PLAIN, 12));
        table.setForeground(TEXT_CLR);
        table.setBackground(TABLE_ROW1);
        table.setSelectionBackground(ACCENT);
        table.setGridColor(new Color(0x30, 0x40, 0x30));
        table.setRowHeight(26);
        table.getTableHeader().setFont(new Font("Courier New", Font.BOLD, 12));
        table.getTableHeader().setBackground(TABLE_HDR);
        table.getTableHeader().setForeground(TEXT_CLR);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                if (!sel) {
                    int modelRow = tbl.convertRowIndexToModel(row);
                    String status = (String) tableModel.getValueAt(modelRow, 4);
                    Object schedObj = tableModel.getValueAt(modelRow, 5);
                    boolean overdue = false;
                    if (schedObj != null && !"COMPLETED".equals(status)) {
                        try {
                            Date schedDate = Date.valueOf(schedObj.toString());
                            if (schedDate.toLocalDate().isBefore(LocalDate.now())) overdue = true;
                        } catch (Exception ignored) {}
                    }
                    if (overdue) {
                        c.setBackground(ALERT_RED); c.setForeground(Color.WHITE);
                    } else if ("COMPLETED".equals(status)) {
                        c.setBackground(new Color(0x1e, 0x3e, 0x1e)); c.setForeground(ACCENT);
                    } else {
                        c.setBackground(row % 2 == 0 ? TABLE_ROW1 : TABLE_ROW2); c.setForeground(TEXT_CLR);
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(BG_DARK);
        add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        bottom.setBackground(new Color(0x14, 0x20, 0x14));

        JButton addBtn = makeBtn("➕ Schedule Maintenance");
        addBtn.addActionListener(e -> showAddDialog());
        bottom.add(addBtn);

        JButton updateBtn = makeBtn("✏ Update Status");
        updateBtn.addActionListener(e -> showUpdateDialog());
        bottom.add(updateBtn);

        JButton refreshBtn = makeBtn("↻ Refresh");
        refreshBtn.addActionListener(e -> loadMaintenance());
        bottom.add(refreshBtn);

        add(bottom, BorderLayout.SOUTH);
    }

    private void loadMaintenance() {
        List<MaintenanceRecord> records = mxManager.getAllMaintenance();
        tableModel.setRowCount(0);
        for (MaintenanceRecord rec : records) {
            tableModel.addRow(new Object[]{
                rec.getMaintenanceId(), rec.getItemId(), rec.getItemName(),
                rec.getMaintenanceType(), rec.getStatus(),
                rec.getScheduledDate(), rec.getCompletedDate(), rec.getRemarks()
            });
        }
    }

    private void showAddDialog() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));
        panel.setBackground(new Color(0x24, 0x33, 0x24));

        // Item picker dropdown
        Map<Integer, String> itemMap = invManager.getItemComboMap();
        JComboBox<String> itemCombo = new JComboBox<>(itemMap.values().toArray(new String[0]));
        itemCombo.setFont(new Font("Tahoma", Font.PLAIN, 12));

        JComboBox<String> typeBox = new JComboBox<>(new String[]{"ROUTINE", "REPAIR", "INSPECTION"});
        JTextField dateField = new JTextField("YYYY-MM-DD");
        JTextField personnelField = new JTextField("0");
        JTextField remarksField = new JTextField();

        panel.add(makeDialogLabel("Equipment:"));      panel.add(itemCombo);
        panel.add(makeDialogLabel("Type:"));            panel.add(typeBox);
        panel.add(makeDialogLabel("Scheduled Date:"));  panel.add(dateField);
        panel.add(makeDialogLabel("Personnel ID:"));    panel.add(personnelField);
        panel.add(makeDialogLabel("Remarks:"));         panel.add(remarksField);

        int result = JOptionPane.showConfirmDialog(this, panel,
            "Schedule Maintenance", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String selected = (String) itemCombo.getSelectedItem();
                int itemId = Integer.parseInt(selected.split(" — ")[0].trim());
                String type = (String) typeBox.getSelectedItem();
                Date schedDate = Date.valueOf(dateField.getText().trim());
                int personnelId = 0;
                try { personnelId = Integer.parseInt(personnelField.getText().trim()); }
                catch (NumberFormatException ignored) {}

                MaintenanceRecord rec = new MaintenanceRecord(itemId, type, schedDate,
                    remarksField.getText().trim());
                rec.setPersonnelId(personnelId);

                if (mxManager.scheduleMaintenance(rec)) {
                    JOptionPane.showMessageDialog(this, "✅ Maintenance scheduled!");
                    loadMaintenance();
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Failed.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showUpdateDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a record first.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        int mxId = (int) tableModel.getValueAt(modelRow, 0);
        String current = (String) tableModel.getValueAt(modelRow, 4);

        String[] options = {"PENDING", "IN_PROGRESS", "COMPLETED"};
        String newStatus = (String) JOptionPane.showInputDialog(this,
            "Update MX#" + mxId + " (currently: " + current + ")",
            "Update Status", JOptionPane.QUESTION_MESSAGE, null, options, current);

        if (newStatus != null && !newStatus.equals(current)) {
            if (mxManager.updateStatus(mxId, newStatus)) {
                JOptionPane.showMessageDialog(this, "✅ Status updated to " + newStatus);
                loadMaintenance();
            }
        }
    }

    private JLabel makeDialogLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Tahoma", Font.BOLD, 12));
        l.setForeground(TEXT_CLR);
        return l;
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
