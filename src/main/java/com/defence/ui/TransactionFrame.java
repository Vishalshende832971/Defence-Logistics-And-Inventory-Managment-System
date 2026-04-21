package com.defence.ui;

import com.defence.manager.SessionManager;
import com.defence.manager.TransactionManager;
import com.defence.model.Transaction;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * TransactionFrame — View all transactions with color-coded rows,
 * CSV export via JFileChooser, and clear (ADMIN only).
 */
public class TransactionFrame extends JFrame {

    private static final Color BG_DARK     = new Color(0x1B, 0x2A, 0x1B);
    private static final Color TEXT_CLR    = new Color(0xE8, 0xF5, 0xE8);
    private static final Color BTN_BG      = new Color(0x2E, 0x5E, 0x2E);
    private static final Color BTN_HOVER   = new Color(0x4A, 0x8C, 0x4A);
    private static final Color TABLE_HDR   = new Color(0x1a, 0x3a, 0x1a);
    private static final Color TABLE_ROW1  = new Color(0x1e, 0x2e, 0x1e);
    private static final Color TABLE_ROW2  = new Color(0x22, 0x33, 0x22);
    private static final Color ACCENT      = new Color(0x8F, 0xA8, 0x32);
    private static final Color ISSUE_CLR   = new Color(0x5B, 0x20, 0x20);
    private static final Color RETURN_CLR  = new Color(0x20, 0x4B, 0x20);
    private static final Color ADD_CLR     = new Color(0x20, 0x30, 0x50);

    private JTable table;
    private DefaultTableModel tableModel;
    private final TransactionManager txManager = new TransactionManager();

    public TransactionFrame() {
        setTitle("Defence Logistics — Transactions");
        setSize(1000, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(BG_DARK);

        initializeUI();
        loadTransactions();
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

        JLabel title = new JLabel("📊 TRANSACTION HISTORY", SwingConstants.CENTER);
        title.setFont(new Font("Courier New", Font.BOLD, 18));
        title.setForeground(TEXT_CLR);
        header.add(title, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);

        // ── Table ──────────────────────────────────────────────
        tableModel = new DefaultTableModel(
            new String[]{"TX ID", "Item ID", "Item Name", "Action",
                         "Quantity", "Performed By", "Timestamp"}, 0) {
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

        // Color-coded rows by action type
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(
                        tbl, val, sel, foc, row, col);
                if (!sel) {
                    int modelRow = tbl.convertRowIndexToModel(row);
                    String action = (String) tableModel.getValueAt(modelRow, 3);
                    if ("ISSUE".equals(action))      c.setBackground(ISSUE_CLR);
                    else if ("RETURN".equals(action)) c.setBackground(RETURN_CLR);
                    else                              c.setBackground(ADD_CLR);
                    c.setForeground(TEXT_CLR);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(BG_DARK);
        add(scrollPane, BorderLayout.CENTER);

        // ── Bottom buttons ─────────────────────────────────────
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        bottom.setBackground(new Color(0x14, 0x20, 0x14));

        JButton refreshBtn = makeBtn("↻ Refresh");
        refreshBtn.addActionListener(e -> loadTransactions());
        bottom.add(refreshBtn);

        JButton exportBtn = makeBtn("📁 Export to CSV");
        exportBtn.addActionListener(e -> exportCSV());
        bottom.add(exportBtn);

        // Clear button — ADMIN only
        String role = SessionManager.getInstance().getRole();
        if ("ADMIN".equals(role)) {
            JButton clearBtn = makeBtn("🗑 Clear All");
            clearBtn.setBackground(new Color(0x8B, 0x00, 0x00));
            clearBtn.addActionListener(e -> {
                int choice = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to clear ALL transactions?\n"
                    + "This action cannot be undone!",
                    "Confirm Clear", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    txManager.clearTransactions();
                    loadTransactions();
                    JOptionPane.showMessageDialog(this,
                        "All transactions cleared.",
                        "Cleared", JOptionPane.INFORMATION_MESSAGE);
                }
            });
            bottom.add(clearBtn);
        }

        add(bottom, BorderLayout.SOUTH);
    }

    private void loadTransactions() {
        List<Transaction> txns = txManager.getAllTransactions();
        tableModel.setRowCount(0);
        for (Transaction tx : txns) {
            tableModel.addRow(new Object[]{
                tx.getTransactionId(),
                tx.getItemId(),
                tx.getItemName(),
                tx.getAction(),
                tx.getQuantity(),
                tx.getPerformedBy(),
                tx.getTimestamp()
            });
        }
    }

    private void exportCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Transactions as CSV");
        fc.setSelectedFile(new java.io.File("transactions_report.csv"));

        int result = fc.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String path = fc.getSelectedFile().getAbsolutePath();
            if (!path.endsWith(".csv")) path += ".csv";

            boolean ok = txManager.exportToCSV(path);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                    "✅ Transactions exported to:\n" + path,
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "❌ Export failed! Check console for details.",
                    "Export Error", JOptionPane.ERROR_MESSAGE);
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
