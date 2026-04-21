package com.defence.ui;

import com.defence.db.DatabaseManager;
import com.defence.manager.SessionManager;
import com.defence.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;

/**
 * LoginFrame — SHA-256 authenticated login with 3-attempt lockout.
 * Launches DashboardFrame on success.
 */
public class LoginFrame extends JFrame {

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JButton        loginButton;
    private JLabel         statusLabel;
    private JLabel         lockLabel;

    private int failedAttempts = 0;
    private Timer lockTimer;

    // ── Theme colours (re-used from Main) ──────────────────────
    private static final Color BG_DARK    = new Color(0x1B, 0x2A, 0x1B);
    private static final Color PANEL_BG   = new Color(0x24, 0x33, 0x24);
    private static final Color BTN_BG     = new Color(0x2E, 0x5E, 0x2E);
    private static final Color BTN_HOVER  = new Color(0x4A, 0x8C, 0x4A);
    private static final Color TEXT_COLOR  = new Color(0xE8, 0xF5, 0xE8);
    private static final Color ACCENT     = new Color(0x8F, 0xA8, 0x32);
    private static final Color ALERT_RED  = new Color(0x8B, 0x00, 0x00);

    public LoginFrame() {
        setTitle("Defence Logistics — Login");
        setSize(460, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(BG_DARK);

        initializeUI();
        setVisible(true);
    }

    private void initializeUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        // ── Shield icon / title ────────────────────────────────
        JLabel shield = new JLabel("\uD83D\uDEE1", SwingConstants.CENTER);
        shield.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        shield.setForeground(ACCENT);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(shield, gbc);

        JLabel title = new JLabel("DEFENCE LOGISTICS", SwingConstants.CENTER);
        title.setFont(new Font("Courier New", Font.BOLD, 22));
        title.setForeground(TEXT_COLOR);
        gbc.gridy = 1;
        add(title, gbc);

        JLabel subtitle = new JLabel("Inventory Management System",
                                      SwingConstants.CENTER);
        subtitle.setFont(new Font("Tahoma", Font.ITALIC, 12));
        subtitle.setForeground(new Color(0xA0, 0xC0, 0xA0));
        gbc.gridy = 2;
        add(subtitle, gbc);

        // ── Separator ──────────────────────────────────────────
        JSeparator sep = new JSeparator();
        sep.setForeground(ACCENT);
        gbc.gridy = 3; gbc.insets = new Insets(12, 10, 12, 10);
        add(sep, gbc);

        gbc.insets    = new Insets(6, 10, 6, 10);
        gbc.gridwidth = 1;

        // ── Username ───────────────────────────────────────────
        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(TEXT_COLOR);
        userLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = 4;
        add(userLabel, gbc);

        usernameField = new JTextField(18);
        usernameField.setFont(new Font("Tahoma", Font.PLAIN, 13));
        usernameField.setBackground(new Color(0x1e, 0x2e, 0x1e));
        usernameField.setForeground(TEXT_COLOR);
        usernameField.setCaretColor(TEXT_COLOR);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8), BorderFactory.createEmptyBorder(4,6,4,6)));
        gbc.gridx = 1;
        add(usernameField, gbc);

        // ── Password ───────────────────────────────────────────
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(TEXT_COLOR);
        passLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = 5;
        add(passLabel, gbc);

        passwordField = new JPasswordField(18);
        passwordField.setFont(new Font("Tahoma", Font.PLAIN, 13));
        passwordField.setBackground(new Color(0x1e, 0x2e, 0x1e));
        passwordField.setForeground(TEXT_COLOR);
        passwordField.setCaretColor(TEXT_COLOR);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8), BorderFactory.createEmptyBorder(4,6,4,6)));
        gbc.gridx = 1;
        add(passwordField, gbc);

        // ── Login button ───────────────────────────────────────
        loginButton = new JButton("LOGIN");
        loginButton.setFont(new Font("Courier New", Font.BOLD, 15));
        loginButton.setBackground(BTN_BG);
        loginButton.setForeground(TEXT_COLOR);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(new RoundedBorder(10));
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                loginButton.setBackground(BTN_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                loginButton.setBackground(BTN_BG);
            }
        });
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 40, 6, 40);
        add(loginButton, gbc);

        // ── Status label ───────────────────────────────────────
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
        statusLabel.setForeground(ALERT_RED);
        gbc.gridy = 7; gbc.insets = new Insets(4, 10, 2, 10);
        add(statusLabel, gbc);

        // ── Lock countdown label ───────────────────────────────
        lockLabel = new JLabel(" ", SwingConstants.CENTER);
        lockLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
        lockLabel.setForeground(new Color(0xCC, 0x66, 0x00));
        gbc.gridy = 8;
        add(lockLabel, gbc);

        // ── Footer ─────────────────────────────────────────────
        JLabel footer = new JLabel("MIT Academy of Engineering",
                                    SwingConstants.CENTER);
        footer.setFont(new Font("Tahoma", Font.ITALIC, 10));
        footer.setForeground(new Color(0x60, 0x80, 0x60));
        gbc.gridy = 9; gbc.insets = new Insets(12, 10, 6, 10);
        add(footer, gbc);

        // ── Actions ────────────────────────────────────────────
        loginButton.addActionListener(this::performLogin);

        // Enter key triggers login
        passwordField.addActionListener(this::performLogin);
        usernameField.addActionListener(e -> passwordField.requestFocus());
    }

    // ── Login logic ────────────────────────────────────────────
    private void performLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("⚠ Please enter both username and password.");
            return;
        }

        String hashedPassword = hashSHA256(password);

        // Query DB
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) {
            statusLabel.setText("✖ Database connection failed!");
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // ── SUCCESS ────────────────────────────────────
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("role")
                );
                SessionManager.getInstance().setCurrentUser(user);
                failedAttempts = 0;

                statusLabel.setForeground(ACCENT);
                statusLabel.setText("✔ Login successful! Welcome, " + username);

                // Brief delay then open dashboard
                Timer t = new Timer(800, ev -> {
                    new DashboardFrame();
                    dispose();
                });
                t.setRepeats(false);
                t.start();

            } else {
                // ── FAILED ─────────────────────────────────────
                failedAttempts++;
                int remaining = 3 - failedAttempts;

                if (failedAttempts >= 3) {
                    lockApplication();
                } else {
                    statusLabel.setForeground(ALERT_RED);
                    statusLabel.setText("✖ Invalid credentials! "
                                      + remaining + " attempt(s) remaining.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            statusLabel.setText("✖ Database error: " + ex.getMessage());
        }
    }

    // ── Lock app for 30 seconds after 3 failures ───────────────
    private void lockApplication() {
        loginButton.setEnabled(false);
        usernameField.setEnabled(false);
        passwordField.setEnabled(false);
        statusLabel.setForeground(ALERT_RED);
        statusLabel.setText("✖ Too many failed attempts!");

        final int[] countdown = {30};
        lockLabel.setText("Locked for " + countdown[0] + " seconds...");

        lockTimer = new Timer(1000, e -> {
            countdown[0]--;
            if (countdown[0] <= 0) {
                lockTimer.stop();
                loginButton.setEnabled(true);
                usernameField.setEnabled(true);
                passwordField.setEnabled(true);
                failedAttempts = 0;
                lockLabel.setText(" ");
                statusLabel.setText("Unlocked — try again.");
                statusLabel.setForeground(ACCENT);
            } else {
                lockLabel.setText("Locked for " + countdown[0] + " seconds...");
            }
        });
        lockTimer.start();
    }

    // ── SHA-256 password hashing ───────────────────────────────
    public static String hashSHA256(String input) {
        try {
            MessageDigest md   = MessageDigest.getInstance("SHA-256");
            byte[]        hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex  = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 hashing failed", e);
        }
    }
}
