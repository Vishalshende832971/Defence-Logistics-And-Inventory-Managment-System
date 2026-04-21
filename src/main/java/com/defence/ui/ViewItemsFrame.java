package com.defence.ui;

import com.defence.manager.InventoryManager;
import com.defence.model.DefenceItem;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

/**
 * ViewItemsFrame — JTable with live search, defence category filter, sorting.
 */
public class ViewItemsFrame extends JFrame {

    private static final Color BG_DARK     = new Color(0x1B, 0x2A, 0x1B);
    private static final Color TEXT_CLR    = new Color(0xE8, 0xF5, 0xE8);
    private static final Color BTN_BG      = new Color(0x2E, 0x5E, 0x2E);
    private static final Color BTN_HOVER   = new Color(0x4A, 0x8C, 0x4A);
    private static final Color TABLE_HDR   = new Color(0x1a, 0x3a, 0x1a);
    private static final Color TABLE_ROW1  = new Color(0x1e, 0x2e, 0x1e);
    private static final Color TABLE_ROW2  = new Color(0x22, 0x33, 0x22);
    private static final Color ACCENT      = new Color(0x8F, 0xA8, 0x32);

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchBar;
    private JComboBox<String> categoryFilter;
    private JLabel countLabel;
    private final InventoryManager invManager = new InventoryManager();

    public ViewItemsFrame() {
        setTitle("Defence Logistics — View Items");
        setSize(1000, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(BG_DARK);
        initializeUI();
        loadItems();
        setVisible(true);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // ── Top ────────────────────────────────────────────────
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(0x14, 0x20, 0x14));
        topPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JButton back = makeBtn("◄ Back");
        back.setPreferredSize(new Dimension(100, 32));
        back.addActionListener(e -> { new DashboardFrame(); dispose(); });
        topPanel.add(back, BorderLayout.WEST);

        JLabel title = new JLabel("📋 EQUIPMENT INVENTORY", SwingConstants.CENTER);
        title.setFont(new Font("Courier New", Font.BOLD, 18));
        title.setForeground(TEXT_CLR);
        topPanel.add(title, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // ── Filter bar ─────────────────────────────────────────
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filterPanel.setBackground(new Color(0x1a, 0x2a, 0x1a));

        filterPanel.add(makeSmallLabel("🔍 Search:"));
        searchBar = new JTextField(18);
        searchBar.setFont(new Font("Tahoma", Font.PLAIN, 13));
        searchBar.setBackground(new Color(0x1e, 0x2e, 0x1e));
        searchBar.setForeground(TEXT_CLR);
        searchBar.setCaretColor(TEXT_CLR);
        searchBar.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8), BorderFactory.createEmptyBorder(3,6,3,6)));
        filterPanel.add(searchBar);

        filterPanel.add(makeSmallLabel("  Category:"));
        // Build defence category filter list
        String[] filterOptions = new String[DefenceItem.CATEGORIES.length + 1];
        filterOptions[0] = "ALL";
        System.arraycopy(DefenceItem.CATEGORIES, 0, filterOptions, 1,
                         DefenceItem.CATEGORIES.length);
        categoryFilter = new JComboBox<>(filterOptions);
        categoryFilter.setFont(new Font("Tahoma", Font.PLAIN, 12));
        categoryFilter.setBackground(new Color(0x1e, 0x2e, 0x1e));
        categoryFilter.setForeground(TEXT_CLR);
        filterPanel.add(categoryFilter);

        JPanel midPanel = new JPanel(new BorderLayout());
        midPanel.setBackground(BG_DARK);
        midPanel.add(filterPanel, BorderLayout.NORTH);

        // ── Table ──────────────────────────────────────────────
        tableModel = new DefaultTableModel(
            new String[]{"ID", "Item Name", "Category", "Quantity",
                         "Reorder Lvl", "Description"}, 0) {
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
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                if (!sel) {
                    c.setBackground(row % 2 == 0 ? TABLE_ROW1 : TABLE_ROW2);
                    c.setForeground(TEXT_CLR);
                }
                return c;
            }
        });

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(BG_DARK);
        midPanel.add(scrollPane, BorderLayout.CENTER);
        add(midPanel, BorderLayout.CENTER);

        // ── Bottom ─────────────────────────────────────────────
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(0x14, 0x20, 0x14));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));

        countLabel = new JLabel("Showing 0 of 0 items");
        countLabel.setFont(new Font("Tahoma", Font.ITALIC, 12));
        countLabel.setForeground(ACCENT);
        bottomPanel.add(countLabel, BorderLayout.WEST);

        JButton refreshBtn = makeBtn("↻ Refresh");
        refreshBtn.setPreferredSize(new Dimension(100, 28));
        refreshBtn.addActionListener(e -> loadItems());
        bottomPanel.add(refreshBtn, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // ── Live search ────────────────────────────────────────
        searchBar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { loadItems(); }
            public void removeUpdate(DocumentEvent e)  { loadItems(); }
            public void changedUpdate(DocumentEvent e) { loadItems(); }
        });
        categoryFilter.addActionListener(e -> loadItems());
    }

    private void loadItems() {
        String keyword  = searchBar.getText().trim();
        String category = (String) categoryFilter.getSelectedItem();

        List<DefenceItem> items = invManager.searchItems(keyword, category);
        int totalCount = invManager.getTotalItemCount();

        tableModel.setRowCount(0);
        for (DefenceItem item : items) {
            tableModel.addRow(new Object[]{
                item.getItemId(),
                item.getItemName(),
                item.getCategory(),
                item.getQuantity(),
                item.getReorderLevel(),
                item.getDescription()
            });
        }
        countLabel.setText("Showing " + items.size() + " of " + totalCount + " items");
    }

    private JLabel makeSmallLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_CLR);
        l.setFont(new Font("Tahoma", Font.BOLD, 12));
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
