package com.defence.ui;

import com.defence.manager.InventoryManager;
import com.defence.manager.SessionManager;
import com.defence.manager.TransactionManager;
import com.defence.model.DefenceItem;
import com.defence.model.Transaction;

import javax.swing.*;
import java.awt.*;

/**
 * AddItemFrame — Add new equipment with defence categories.
 * Item ID is auto-incremented (user does not enter it).
 */
public class AddItemFrame extends JFrame {

    private static final Color BG_DARK   = new Color(0x1B, 0x2A, 0x1B);
    private static final Color BTN_BG    = new Color(0x2E, 0x5E, 0x2E);
    private static final Color BTN_HOVER = new Color(0x4A, 0x8C, 0x4A);
    private static final Color TEXT_CLR  = new Color(0xE8, 0xF5, 0xE8);

    private JTextField  itemNameField, quantityField, reorderField, descriptionField;
    private JComboBox<String> categoryCombo;
    private JButton     saveButton, backButton;

    private final InventoryManager   invManager  = new InventoryManager();
    private final TransactionManager txManager   = new TransactionManager();

    public AddItemFrame() {
        setTitle("Defence Logistics — Add Item");
        setSize(520, 500);
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

        JLabel title = new JLabel("     ➕ ADD NEW EQUIPMENT");
        title.setFont(new Font("Courier New", Font.BOLD, 18));
        title.setForeground(TEXT_CLR);
        header.add(title);

        add(header, BorderLayout.NORTH);

        // ── Form panel ─────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG_DARK);
        form.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Note: ID is auto-generated
        JLabel idNote = new JLabel("ℹ  Item ID will be auto-generated");
        idNote.setFont(new Font("Tahoma", Font.ITALIC, 11));
        idNote.setForeground(new Color(0x8F, 0xA8, 0x32));
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        form.add(idNote, gbc);

        // Item Name
        row++;
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(makeLabel("Item Name:"), gbc);
        itemNameField = makeTextField();
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(itemNameField, gbc);

        // Category — Defence categories
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(makeLabel("Category:"), gbc);
        categoryCombo = new JComboBox<>(DefenceItem.CATEGORIES);
        categoryCombo.setFont(new Font("Tahoma", Font.BOLD, 13));
        categoryCombo.setBackground(new Color(0x1e, 0x2e, 0x1e));
        categoryCombo.setForeground(TEXT_CLR);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(categoryCombo, gbc);

        // Quantity
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(makeLabel("Quantity:"), gbc);
        quantityField = makeTextField();
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(quantityField, gbc);

        // Reorder Level
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(makeLabel("Reorder Level:"), gbc);
        reorderField = makeTextField();
        reorderField.setText("10");
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(reorderField, gbc);

        // Description
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(makeLabel("Description:"), gbc);
        descriptionField = makeTextField();
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(descriptionField, gbc);

        // Save button
        row++;
        saveButton = makeBtn("💾 SAVE ITEM");
        saveButton.setPreferredSize(new Dimension(200, 40));
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 60, 8, 60);
        form.add(saveButton, gbc);

        add(form, BorderLayout.CENTER);

        saveButton.addActionListener(e -> saveItem());
    }

    private void saveItem() {
        String name = itemNameField.getText().trim();
        String qtyStr = quantityField.getText().trim();
        String reorderStr = reorderField.getText().trim();
        String desc = descriptionField.getText().trim();

        if (name.isEmpty() || qtyStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Item Name and Quantity are required!",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int qty, reorder;
        try {
            qty = Integer.parseInt(qtyStr);
            reorder = reorderStr.isEmpty() ? 10 : Integer.parseInt(reorderStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Quantity and Reorder Level must be numbers!",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String category = (String) categoryCombo.getSelectedItem();

        DefenceItem item = new DefenceItem(name, category, qty, reorder, desc);
        boolean saved = invManager.addItem(item);

        if (saved) {
            Transaction tx = new Transaction(item.getItemId(), "ADD", qty,
                    SessionManager.getInstance().getUsername());
            txManager.recordTransaction(tx);

            JOptionPane.showMessageDialog(this,
                "✅ Item added successfully!\nAuto-generated ID: " + item.getItemId(),
                "Success", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
        } else {
            JOptionPane.showMessageDialog(this,
                "❌ Failed to add item.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        itemNameField.setText("");
        quantityField.setText("");
        reorderField.setText("10");
        descriptionField.setText("");
        categoryCombo.setSelectedIndex(0);
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Tahoma", Font.BOLD, 13));
        l.setForeground(TEXT_CLR);
        return l;
    }

    private JTextField makeTextField() {
        JTextField tf = new JTextField(18);
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
