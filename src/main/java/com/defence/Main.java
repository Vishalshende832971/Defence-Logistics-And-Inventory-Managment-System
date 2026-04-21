package com.defence;

import com.defence.db.DatabaseManager;
import com.defence.ui.LoginFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Main — Application entry point.
 * 1. Applies the dark military UI theme globally via UIManager.
 * 2. Tests MySQL connection (exits gracefully on failure).
 * 3. Initializes all database tables.
 * 4. Launches LoginFrame.
 */
public class Main {

    // ── Theme colour palette ───────────────────────────────────
    private static final Color BG_DARK       = new Color(0x1B, 0x2A, 0x1B);
    private static final Color PANEL_BG      = new Color(0x24, 0x33, 0x24);
    private static final Color BTN_BG        = new Color(0x2E, 0x5E, 0x2E);
    private static final Color BTN_HOVER     = new Color(0x4A, 0x8C, 0x4A);
    private static final Color TEXT_COLOR    = new Color(0xE8, 0xF5, 0xE8);
    private static final Color ACCENT        = new Color(0x8F, 0xA8, 0x32);
    private static final Color TABLE_HDR     = new Color(0x1a, 0x3a, 0x1a);
    private static final Color TABLE_ROW1    = new Color(0x1e, 0x2e, 0x1e);
    private static final Color TABLE_ROW2    = new Color(0x22, 0x33, 0x22);
    private static final Color FIELD_BG      = new Color(0x1e, 0x2e, 0x1e);
    private static final Color ALERT_RED     = new Color(0x8B, 0x00, 0x00);
    private static final Color ALERT_ORANGE  = new Color(0xCC, 0x66, 0x00);

    public static void main(String[] args) {
        // ── 1. Apply dark military theme ───────────────────────
        applyDarkMilitaryTheme();

        // ── 2. Run on EDT ──────────────────────────────────────
        SwingUtilities.invokeLater(() -> {
            // ── 3. Test MySQL connection ───────────────────────
            DatabaseManager db = DatabaseManager.getInstance();
            if (!db.testConnection()) {
                JOptionPane.showMessageDialog(null,
                    "⛔ Cannot connect to MySQL!\n\n"
                  + "Please ensure:\n"
                  + "  1. MySQL Server is running on localhost:3306\n"
                  + "  2. Database 'defence_logistics_db' exists\n"
                  + "     (run: CREATE DATABASE defence_logistics_db;)\n"
                  + "  3. db.properties has correct credentials\n\n"
                  + "Application will now exit.",
                    "Database Connection Failed",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            // ── 4. Initialize tables ───────────────────────────
            db.initializeDatabase();

            // ── 5. Launch LoginFrame ───────────────────────────
            new LoginFrame();
        });
    }

    /**
     * Apply the dark military UI theme globally via UIManager
     * BEFORE any Swing component is created.
     */
    private static void applyDarkMilitaryTheme() {
        try {
            // Use system look-and-feel as base, then override
            UIManager.setLookAndFeel(
                UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        Font headerFont = new Font("Courier New", Font.BOLD, 14);
        Font bodyFont   = new Font("Tahoma", Font.PLAIN, 13);

        // ── Panel / OptionPane ─────────────────────────────────
        UIManager.put("Panel.background",        PANEL_BG);
        UIManager.put("OptionPane.background",    PANEL_BG);
        UIManager.put("OptionPane.messageForeground", TEXT_COLOR);
        UIManager.put("OptionPane.messageFont",   bodyFont);

        // ── Button ─────────────────────────────────────────────
        UIManager.put("Button.background",        BTN_BG);
        UIManager.put("Button.foreground",        TEXT_COLOR);
        UIManager.put("Button.font",              bodyFont);
        UIManager.put("Button.focus",             ACCENT);

        // ── Label ──────────────────────────────────────────────
        UIManager.put("Label.foreground",         TEXT_COLOR);
        UIManager.put("Label.font",               bodyFont);

        // ── TextField / PasswordField ──────────────────────────
        UIManager.put("TextField.background",     FIELD_BG);
        UIManager.put("TextField.foreground",     TEXT_COLOR);
        UIManager.put("TextField.caretForeground",TEXT_COLOR);
        UIManager.put("TextField.font",           bodyFont);
        UIManager.put("PasswordField.background", FIELD_BG);
        UIManager.put("PasswordField.foreground", TEXT_COLOR);
        UIManager.put("PasswordField.caretForeground", TEXT_COLOR);
        UIManager.put("PasswordField.font",       bodyFont);
        UIManager.put("TextArea.background",      FIELD_BG);
        UIManager.put("TextArea.foreground",      TEXT_COLOR);

        // ── ComboBox ───────────────────────────────────────────
        UIManager.put("ComboBox.background",      FIELD_BG);
        UIManager.put("ComboBox.foreground",      TEXT_COLOR);
        UIManager.put("ComboBox.selectionBackground", ACCENT);
        UIManager.put("ComboBox.selectionForeground", Color.BLACK);
        UIManager.put("ComboBox.font",            bodyFont);

        // ── Table ──────────────────────────────────────────────
        UIManager.put("Table.background",         TABLE_ROW1);
        UIManager.put("Table.foreground",         TEXT_COLOR);
        UIManager.put("Table.gridColor",          new Color(0x30, 0x40, 0x30));
        UIManager.put("Table.selectionBackground",ACCENT);
        UIManager.put("Table.selectionForeground",Color.BLACK);
        UIManager.put("Table.font",               bodyFont);
        UIManager.put("TableHeader.background",   TABLE_HDR);
        UIManager.put("TableHeader.foreground",   TEXT_COLOR);
        UIManager.put("TableHeader.font",         headerFont);

        // ── ScrollPane / Viewport ──────────────────────────────
        UIManager.put("ScrollPane.background",    BG_DARK);
        UIManager.put("Viewport.background",      BG_DARK);
        UIManager.put("ScrollBar.background",     PANEL_BG);
        UIManager.put("ScrollBar.thumb",          BTN_BG);

        // ── CheckBox ───────────────────────────────────────────
        UIManager.put("CheckBox.background",      PANEL_BG);
        UIManager.put("CheckBox.foreground",      TEXT_COLOR);
        UIManager.put("CheckBox.font",            bodyFont);

        // ── ProgressBar ────────────────────────────────────────
        UIManager.put("ProgressBar.background",   PANEL_BG);
        UIManager.put("ProgressBar.foreground",   ACCENT);

        // ── Separator ──────────────────────────────────────────
        UIManager.put("Separator.foreground",     ACCENT);

        // ── ToolTip ────────────────────────────────────────────
        UIManager.put("ToolTip.background",       PANEL_BG);
        UIManager.put("ToolTip.foreground",       TEXT_COLOR);

        // ── List ───────────────────────────────────────────────
        UIManager.put("List.background",          FIELD_BG);
        UIManager.put("List.foreground",          TEXT_COLOR);
        UIManager.put("List.selectionBackground", ACCENT);
        UIManager.put("List.selectionForeground", Color.BLACK);

        // ── FileChooser ────────────────────────────────────────
        UIManager.put("FileChooser.background",   PANEL_BG);

        // ── TabbedPane ─────────────────────────────────────────
        UIManager.put("TabbedPane.background",    PANEL_BG);
        UIManager.put("TabbedPane.foreground",    TEXT_COLOR);
        UIManager.put("TabbedPane.selected",      BTN_BG);
    }
}
