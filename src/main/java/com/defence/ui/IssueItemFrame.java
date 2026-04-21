package com.defence.ui;

import com.defence.manager.InventoryManager;
import com.defence.manager.SessionManager;
import com.defence.manager.TransactionManager;
import com.defence.model.DefenceItem;
import com.defence.model.Transaction;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * IssueItemFrame — Issue equipment using an item picker dropdown.
 */
public class IssueItemFrame extends JFrame {

    private static final Color BG_DARK   = new Color(0x1B, 0x2A, 0x1B);
    private static final Color TEXT_CLR  = new Color(0xE8, 0xF5, 0xE8);
    private static final Color BTN_BG    = new Color(0x2E, 0x5E, 0x2E);
    private static final Color BTN_HOVER = new Color(0x4A, 0x8C, 0x4A);
    private static final Color ACCENT    = new Color(0x8F, 0xA8, 0x32);

    private JComboBox<String> itemCombo;
    private JTextField quantityField;
    private JLabel     itemInfoLabel;
    private JButton    issueButton, backButton;

    private final InventoryManager   invManager = new InventoryManager();
    private final TransactionManager txManager  = new TransactionManager();
    private Map<Integer, String> itemMap;

    public IssueItemFrame() {
        setTitle("Defence Logistics — Issue Item");
        setSize(540, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(BG_DARK);
        initializeUI();
        setVisible(true);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // ── Header ─────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setBackground(new Color(0x14, 0x20, 0x14));
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        backButton = makeBtn("◄ Back");
        backButton.setPreferredSize(new Dimension(100, 32));
        backButton.addActionListener(e -> { new DashboardFrame(); dispose(); });
        header.add(backButton);

        JLabel title = new JLabel("     📤 ISSUE EQUIPMENT");
        title.setFont(new Font("Courier New", Font.BOLD, 18));
        title.setForeground(TEXT_CLR);
        header.add(title);
        add(header, BorderLayout.NORTH);

        // ── Form ───────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG_DARK);
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 8, 10, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Select Equipment (dropdown)
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(makeLabel("Select Equipment:"), gbc);

        itemMap = invManager.getItemComboMap();
        itemCombo = new JComboBox<>(itemMap.values().toArray(new String[0]));
        itemCombo.setFont(new Font("Tahoma", Font.PLAIN, 12));
        itemCombo.setBackground(new Color(0x1e, 0x2e, 0x1e));
        itemCombo.setForeground(TEXT_CLR);
        itemCombo.setMaximumRowCount(15);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(itemCombo, gbc);

        // Info label
        itemInfoLabel = new JLabel(" ");
        itemInfoLabel.setFont(new Font("Tahoma", Font.ITALIC, 11));
        itemInfoLabel.setForeground(ACCENT);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weightx = 0;
        form.add(itemInfoLabel, gbc);

        // Quantity
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        form.add(makeLabel("Quantity to Issue:"), gbc);
        quantityField = makeTextField();
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(quantityField, gbc);

        // Issue button
        issueButton = makeBtn("📤 ISSUE ITEM");
        issueButton.setPreferredSize(new Dimension(200, 40));
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 60, 8, 60);
        form.add(issueButton, gbc);

        add(form, BorderLayout.CENTER);

        // ── Show details on combo selection ────────────────────
        itemCombo.addActionListener(e -> showItemDetails());
        if (itemCombo.getItemCount() > 0) showItemDetails();

        issueButton.addActionListener(e -> issueItem());
    }

    private void showItemDetails() {
        int itemId = getSelectedItemId();
        if (itemId <= 0) { itemInfoLabel.setText(" "); return; }
        DefenceItem item = invManager.getItemById(itemId);
        if (item != null) {
            itemInfoLabel.setText("📦 Stock: " + item.getQuantity()
                + "  |  Reorder Lvl: " + item.getReorderLevel()
                + "  |  " + item.getCategory());
        }
    }

    private int getSelectedItemId() {
        String selected = (String) itemCombo.getSelectedItem();
        if (selected == null) return 0;
        try {
            return Integer.parseInt(selected.split(" — ")[0].trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private void issueItem() {
        int itemId = getSelectedItemId();
        String qtyStr = quantityField.getText().trim();

        if (itemId <= 0 || qtyStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Select an item and enter quantity!",
                "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Quantity must be a number!",
                "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (qty <= 0) {
            JOptionPane.showMessageDialog(this,
                "Quantity must be positive!",
                "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int result = invManager.issueItem(itemId, qty);

        switch (result) {
            case 0:
                JOptionPane.showMessageDialog(this,
                    "❌ Item not found!", "Error", JOptionPane.ERROR_MESSAGE);
                break;
            case -1:
                JOptionPane.showMessageDialog(this,
                    "❌ Insufficient stock!", "Error", JOptionPane.ERROR_MESSAGE);
                break;
            case 1:
                txManager.recordTransaction(new Transaction(itemId, "ISSUE", qty,
                        SessionManager.getInstance().getUsername()));
                JOptionPane.showMessageDialog(this,
                    "✅ Item issued successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshCombo();
                break;
            case 2:
                txManager.recordTransaction(new Transaction(itemId, "ISSUE", qty,
                        SessionManager.getInstance().getUsername()));
                DefenceItem item = invManager.getItemById(itemId);
                JOptionPane.showMessageDialog(this,
                    "✅ Issued!\n\n⚠ LOW STOCK ALERT!\n"
                    + "Item: " + (item != null ? item.getItemName() : itemId) + "\n"
                    + "Remaining: " + (item != null ? item.getQuantity() : "?") + "\n"
                    + "Reorder Level: " + (item != null ? item.getReorderLevel() : "?"),
                    "Low Stock Warning", JOptionPane.WARNING_MESSAGE);
                refreshCombo();
                break;
        }
    }

    private void refreshCombo() {
        itemMap = invManager.getItemComboMap();
        itemCombo.removeAllItems();
        for (String s : itemMap.values()) itemCombo.addItem(s);
        quantityField.setText("");
        showItemDetails();
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Tahoma", Font.BOLD, 13));
        l.setForeground(TEXT_CLR);
        return l;
    }

    private JTextField makeTextField() {
        JTextField tf = new JTextField(12);
        tf.setFont(new Font("Tahoma", Font.PLAIN, 13));
        tf.setBackground(new Color(0x1e, 0x2e, 0x1e));
        tf.setForeground(TEXT_CLR);
        tf.setCaretColor(TEXT_CLR);
        tf.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8), BorderFactory.createEmptyBorder(4,6,4,6)));
        return tf;
    }

    private JButton makeBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Tahoma", Font.BOLD, 13));
        btn.setBackground(BTN_BG);
        btn.setForeground(TEXT_CLR);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(BTN_HOVER); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(BTN_BG); }
        });
        return btn;
    }
}
