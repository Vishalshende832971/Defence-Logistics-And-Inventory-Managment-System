package com.defence.ui;

import com.defence.manager.SessionManager;

import javax.swing.*;
import java.awt.*;

/**
 * DashboardFrame — Main navigation hub with role-based button access,
 * now including Reports and Data Explorer.
 */
public class DashboardFrame extends JFrame {

    private static final Color BG_DARK    = new Color(0x1B, 0x2A, 0x1B);
    private static final Color BTN_BG     = new Color(0x2E, 0x5E, 0x2E);
    private static final Color BTN_HOVER  = new Color(0x4A, 0x8C, 0x4A);
    private static final Color TEXT_COLOR  = new Color(0xE8, 0xF5, 0xE8);
    private static final Color ACCENT     = new Color(0x8F, 0xA8, 0x32);
    private static final Color ALERT_RED  = new Color(0x8B, 0x00, 0x00);

    private JButton addItemButton, viewItemsButton, issueItemButton,
                    returnItemButton, transactionButton, alertsButton,
                    maintenanceButton, reportsButton, explorerButton,
                    logoutButton;

    public DashboardFrame() {
        setTitle("Defence Logistics — Dashboard");
        setSize(660, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(BG_DARK);
        initializeUI();
        applyRoleAccess();
        setVisible(true);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // ════════════════════════════════════════════════════════
        //  HEADER
        // ════════════════════════════════════════════════════════
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0x14, 0x20, 0x14));
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel shield = new JLabel("\uD83D\uDEE1");
        shield.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        header.add(shield, BorderLayout.WEST);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("  DEFENCE LOGISTICS SYSTEM");
        title.setFont(new Font("Courier New", Font.BOLD, 20));
        title.setForeground(TEXT_COLOR);
        JLabel subtitle = new JLabel("  Inventory Management Dashboard");
        subtitle.setFont(new Font("Tahoma", Font.ITALIC, 11));
        subtitle.setForeground(new Color(0xA0, 0xC0, 0xA0));
        titlePanel.add(title);
        titlePanel.add(subtitle);
        header.add(titlePanel, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // ════════════════════════════════════════════════════════
        //  BUTTON GRID (5 rows × 2 cols)
        // ════════════════════════════════════════════════════════
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(BG_DARK);
        center.setBorder(BorderFactory.createEmptyBorder(16, 40, 10, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(7, 10, 7, 10);
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.weightx = 1;

        addItemButton      = makeButton("➕  Add Item");
        viewItemsButton    = makeButton("📋  View Items");
        issueItemButton    = makeButton("📤  Issue Item");
        returnItemButton   = makeButton("📥  Return Item");
        transactionButton  = makeButton("📊  Transactions");
        alertsButton       = makeButton("⚠  Alerts");
        maintenanceButton  = makeButton("🔧  Maintenance");
        reportsButton      = makeButton("📈  Reports");
        explorerButton     = makeButton("🗄  Data Explorer");
        logoutButton       = makeButton("🚪  Logout");
        logoutButton.setBackground(ALERT_RED);

        gbc.gridx = 0; gbc.gridy = 0; center.add(addItemButton,      gbc);
        gbc.gridx = 1;                center.add(viewItemsButton,    gbc);
        gbc.gridx = 0; gbc.gridy = 1; center.add(issueItemButton,    gbc);
        gbc.gridx = 1;                center.add(returnItemButton,   gbc);
        gbc.gridx = 0; gbc.gridy = 2; center.add(transactionButton,  gbc);
        gbc.gridx = 1;                center.add(alertsButton,       gbc);
        gbc.gridx = 0; gbc.gridy = 3; center.add(maintenanceButton,  gbc);
        gbc.gridx = 1;                center.add(reportsButton,      gbc);
        gbc.gridx = 0; gbc.gridy = 4; center.add(explorerButton,     gbc);
        gbc.gridx = 1;                center.add(logoutButton,       gbc);

        add(center, BorderLayout.CENTER);

        // ════════════════════════════════════════════════════════
        //  STATUS BAR
        // ════════════════════════════════════════════════════════
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(0x14, 0x20, 0x14));
        statusBar.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));

        String user = SessionManager.getInstance().getUsername();
        String role = SessionManager.getInstance().getRole();

        JLabel userLabel = new JLabel("👤 " + (user != null ? user : "—"));
        userLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
        userLabel.setForeground(TEXT_COLOR);

        JLabel roleLabel = new JLabel("Role: " + (role != null ? role : "—"));
        roleLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
        roleLabel.setForeground(ACCENT);

        statusBar.add(userLabel, BorderLayout.WEST);
        statusBar.add(roleLabel, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);

        // ════════════════════════════════════════════════════════
        //  ACTIONS
        // ════════════════════════════════════════════════════════
        addItemButton.addActionListener(e ->     { new AddItemFrame();      dispose(); });
        viewItemsButton.addActionListener(e ->   { new ViewItemsFrame();    dispose(); });
        issueItemButton.addActionListener(e ->   { new IssueItemFrame();    dispose(); });
        returnItemButton.addActionListener(e ->  { new ReturnItemFrame();   dispose(); });
        transactionButton.addActionListener(e -> { new TransactionFrame();  dispose(); });
        alertsButton.addActionListener(e ->      { new AlertsFrame();       dispose(); });
        maintenanceButton.addActionListener(e -> { new MaintenanceFrame();  dispose(); });
        reportsButton.addActionListener(e ->     { new ReportsFrame();      dispose(); });
        explorerButton.addActionListener(e ->    { new DataExplorerFrame(); dispose(); });

        logoutButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                SessionManager.getInstance().logout();
                new LoginFrame();
                dispose();
            }
        });
    }

    private void applyRoleAccess() {
        String role = SessionManager.getInstance().getRole();
        if (role == null) role = "VIEWER";

        switch (role) {
            case "ADMIN":
                // All buttons enabled
                break;
            case "OFFICER":
                addItemButton.setEnabled(false);
                alertsButton.setEnabled(false);
                break;
            case "VIEWER":
                addItemButton.setEnabled(false);
                issueItemButton.setEnabled(false);
                returnItemButton.setEnabled(false);
                maintenanceButton.setEnabled(false);
                alertsButton.setEnabled(false);
                break;
        }
    }

    private JButton makeButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Tahoma", Font.BOLD, 14));
        btn.setBackground(BTN_BG);
        btn.setForeground(TEXT_COLOR);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(220, 48));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(BTN_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) {
                    if (btn.getText().contains("Logout")) btn.setBackground(ALERT_RED);
                    else btn.setBackground(BTN_BG);
                }
            }
        });
        return btn;
    }
}
