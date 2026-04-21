package com.defence.db;

import javax.swing.JOptionPane;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * DatabaseManager — Singleton class for MySQL database operations.
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection connection;
    private String url;
    private String username;
    private String password;

    private DatabaseManager() {
        loadProperties();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new RuntimeException(
                    "db.properties not found in classpath!");
            }
            Properties props = new Properties();
            props.load(input);
            this.url      = props.getProperty("db.url");
            this.username = props.getProperty("db.username");
            this.password = props.getProperty("db.password");
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to load db.properties: " + e.getMessage(), e);
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(url, username, password);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            e.printStackTrace();
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✔ MySQL connection successful!");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void initializeDatabase() {
        Connection conn = getConnection();
        if (conn == null) {
            System.err.println("Cannot initialize DB — no connection.");
            return;
        }

        try (Statement stmt = conn.createStatement()) {

            // 1. USERS
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users ("
              + "  user_id       INT AUTO_INCREMENT PRIMARY KEY,"
              + "  username      VARCHAR(50)  UNIQUE NOT NULL,"
              + "  password_hash VARCHAR(255) NOT NULL,"
              + "  role          ENUM('ADMIN','OFFICER','VIEWER') NOT NULL,"
              + "  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
              + ") ENGINE=InnoDB"
            );

            // 2. EQUIPMENT — category is VARCHAR(30) for defence names
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS equipment ("
              + "  item_id       INT AUTO_INCREMENT PRIMARY KEY,"
              + "  item_name     VARCHAR(100) NOT NULL,"
              + "  category      VARCHAR(30)  NOT NULL,"
              + "  quantity      INT          NOT NULL DEFAULT 0,"
              + "  reorder_level INT          NOT NULL DEFAULT 10,"
              + "  description   TEXT,"
              + "  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
              + "  updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP "
              + "                ON UPDATE CURRENT_TIMESTAMP"
              + ") ENGINE=InnoDB"
            );

            // Migrate category column if it's still CHAR(1) from old schema
            migrateCategories(conn);

            // 3. PERSONNEL
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS personnel ("
              + "  personnel_id    INT AUTO_INCREMENT PRIMARY KEY,"
              + "  name            VARCHAR(100) NOT NULL,"
              + "  rank_title      VARCHAR(50),"
              + "  unit            VARCHAR(100),"
              + "  clearance_level INT DEFAULT 1,"
              + "  contact         VARCHAR(100)"
              + ") ENGINE=InnoDB"
            );

            // 4. DEPOT
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS depot ("
              + "  depot_id       INT AUTO_INCREMENT PRIMARY KEY,"
              + "  depot_name     VARCHAR(100) NOT NULL,"
              + "  location       VARCHAR(150),"
              + "  depot_type     ENUM('FORWARD','TERMINAL','BASE') NOT NULL,"
              + "  security_level INT DEFAULT 1"
              + ") ENGINE=InnoDB"
            );

            // 5. SUPPLIER
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS supplier ("
              + "  supplier_id     INT AUTO_INCREMENT PRIMARY KEY,"
              + "  supplier_name   VARCHAR(100) NOT NULL,"
              + "  company_type    ENUM('GOVT','PRIVATE') NOT NULL,"
              + "  license_number  VARCHAR(50),"
              + "  contact_details VARCHAR(200),"
              + "  address         TEXT"
              + ") ENGINE=InnoDB"
            );

            // 6. TRANSACTIONS
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS transactions ("
              + "  transaction_id INT AUTO_INCREMENT PRIMARY KEY,"
              + "  item_id        INT NOT NULL,"
              + "  action         ENUM('ADD','ISSUE','RETURN') NOT NULL,"
              + "  quantity       INT NOT NULL,"
              + "  performed_by   VARCHAR(50),"
              + "  timestamp      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
              + "  FOREIGN KEY (item_id) REFERENCES equipment(item_id) "
              + "    ON DELETE CASCADE ON UPDATE CASCADE"
              + ") ENGINE=InnoDB"
            );

            // 7. INVENTORY (per-depot)
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS inventory ("
              + "  inventory_id       INT AUTO_INCREMENT PRIMARY KEY,"
              + "  item_id            INT NOT NULL,"
              + "  depot_id           INT NOT NULL,"
              + "  quantity_available INT NOT NULL DEFAULT 0,"
              + "  reorder_level      INT DEFAULT 10,"
              + "  max_stock_level    INT DEFAULT 500,"
              + "  last_updated       TIMESTAMP DEFAULT CURRENT_TIMESTAMP "
              + "                     ON UPDATE CURRENT_TIMESTAMP,"
              + "  FOREIGN KEY (item_id) REFERENCES equipment(item_id) "
              + "    ON DELETE CASCADE ON UPDATE CASCADE,"
              + "  FOREIGN KEY (depot_id) REFERENCES depot(depot_id) "
              + "    ON DELETE CASCADE ON UPDATE CASCADE"
              + ") ENGINE=InnoDB"
            );

            // 8. ORDERS
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS orders ("
              + "  order_id      INT AUTO_INCREMENT PRIMARY KEY,"
              + "  supplier_id   INT NOT NULL,"
              + "  order_date    DATE NOT NULL,"
              + "  delivery_date DATE,"
              + "  status        ENUM('PENDING','APPROVED','DELIVERED','CANCELLED') "
              + "                DEFAULT 'PENDING',"
              + "  approved_by   INT,"
              + "  FOREIGN KEY (supplier_id) REFERENCES supplier(supplier_id) "
              + "    ON DELETE CASCADE ON UPDATE CASCADE,"
              + "  FOREIGN KEY (approved_by) REFERENCES users(user_id) "
              + "    ON DELETE SET NULL ON UPDATE CASCADE"
              + ") ENGINE=InnoDB"
            );

            // 9. ORDER_DETAILS
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS order_details ("
              + "  order_detail_id INT AUTO_INCREMENT PRIMARY KEY,"
              + "  order_id        INT NOT NULL,"
              + "  item_id         INT NOT NULL,"
              + "  quantity        INT NOT NULL,"
              + "  FOREIGN KEY (order_id) REFERENCES orders(order_id) "
              + "    ON DELETE CASCADE ON UPDATE CASCADE,"
              + "  FOREIGN KEY (item_id) REFERENCES equipment(item_id) "
              + "    ON DELETE CASCADE ON UPDATE CASCADE"
              + ") ENGINE=InnoDB"
            );

            // 10. SHIPMENT
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS shipment ("
              + "  shipment_id    INT AUTO_INCREMENT PRIMARY KEY,"
              + "  from_depot     INT NOT NULL,"
              + "  to_depot       INT NOT NULL,"
              + "  dispatch_date  DATE,"
              + "  arrival_date   DATE,"
              + "  transport_mode ENUM('ROAD','AIR','RAIL','SEA') NOT NULL,"
              + "  status         ENUM('PENDING','IN_TRANSIT','DELIVERED') "
              + "                 DEFAULT 'PENDING',"
              + "  FOREIGN KEY (from_depot) REFERENCES depot(depot_id) "
              + "    ON DELETE CASCADE ON UPDATE CASCADE,"
              + "  FOREIGN KEY (to_depot) REFERENCES depot(depot_id) "
              + "    ON DELETE CASCADE ON UPDATE CASCADE"
              + ") ENGINE=InnoDB"
            );

            // 11. SHIPMENT_DETAILS
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS shipment_details ("
              + "  shipment_detail_id INT AUTO_INCREMENT PRIMARY KEY,"
              + "  shipment_id        INT NOT NULL,"
              + "  item_id            INT NOT NULL,"
              + "  quantity           INT NOT NULL,"
              + "  FOREIGN KEY (shipment_id) REFERENCES shipment(shipment_id) "
              + "    ON DELETE CASCADE ON UPDATE CASCADE,"
              + "  FOREIGN KEY (item_id) REFERENCES equipment(item_id) "
              + "    ON DELETE CASCADE ON UPDATE CASCADE"
              + ") ENGINE=InnoDB"
            );

            // 12. MAINTENANCE
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS maintenance ("
              + "  maintenance_id   INT AUTO_INCREMENT PRIMARY KEY,"
              + "  item_id          INT NOT NULL,"
              + "  personnel_id     INT,"
              + "  maintenance_type ENUM('ROUTINE','REPAIR','INSPECTION') NOT NULL,"
              + "  status           ENUM('PENDING','IN_PROGRESS','COMPLETED') "
              + "                   DEFAULT 'PENDING',"
              + "  scheduled_date   DATE,"
              + "  completed_date   DATE,"
              + "  remarks          TEXT,"
              + "  FOREIGN KEY (item_id) REFERENCES equipment(item_id) "
              + "    ON DELETE CASCADE ON UPDATE CASCADE"
              + ") ENGINE=InnoDB"
            );

            // ── Seed default users ─────────────────────────────
            String insertUser =
                "INSERT IGNORE INTO users (username, password_hash, role) VALUES "
              + "(?, SHA2(?, 256), ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertUser)) {
                ps.setString(1, "admin");   ps.setString(2, "admin123");   ps.setString(3, "ADMIN");   ps.executeUpdate();
                ps.setString(1, "officer"); ps.setString(2, "officer123"); ps.setString(3, "OFFICER"); ps.executeUpdate();
                ps.setString(1, "viewer");  ps.setString(2, "viewer123");  ps.setString(3, "VIEWER");  ps.executeUpdate();
            }

            // ── Seed equipment with defence categories ─────────
            String insertEquip =
                "INSERT IGNORE INTO equipment (item_id, item_name, category, quantity, reorder_level, description) VALUES "
              + "(?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertEquip)) {
                seedItem(ps, 1, "AK-47 Assault Rifle",      "WEAPONS",        500, 50,  "Standard infantry assault rifle");
                seedItem(ps, 2, "9mm Beretta Pistol",        "WEAPONS",        300, 30,  "Sidearm for officers");
                seedItem(ps, 3, "Kevlar Body Armor Vest",    "PROTECTIVE GEAR",200, 20,  "Kevlar body armor — Level IIIA");
                seedItem(ps, 4, "Night Vision Goggles NVG",  "ELECTRONICS",    100, 15,  "Gen-3 NVG for night operations");
                seedItem(ps, 5, "MICH-2000 Combat Helmet",   "PROTECTIVE GEAR",400, 40,  "Ballistic helmet MICH-2000");
                seedItem(ps, 6, "AN/PRC-152 Field Radio",    "COMMUNICATION",   80, 10,  "Tactical radio — 30 km range");
                seedItem(ps, 7, "Military First Aid Kit",    "MEDICAL",        600, 60,  "Standard military first aid kit");
                seedItem(ps, 8, "7.62mm Ammunition Box",     "AMMUNITION",    1000,100,  "7.62×39mm — box of 500 rounds");
                seedItem(ps, 9, "5kW Diesel Generator",      "LOGISTICS",       25,  5,  "5 kW diesel portable generator");
                seedItem(ps,10, "BMP-2 Infantry Vehicle",    "VEHICLES",        15,  3,  "Amphibious infantry fighting vehicle");
            }

            // ── Seed depot ─────────────────────────────────────
            stmt.executeUpdate(
                "INSERT IGNORE INTO depot (depot_id, depot_name, location, depot_type, security_level) VALUES "
              + "(1, 'Alpha Forward Base',   'Rajasthan Sector', 'FORWARD',  3),"
              + "(2, 'Bravo Terminal Depot',  'Pune Cantonment',  'TERMINAL', 2),"
              + "(3, 'Charlie Base Depot',    'Delhi Cantt',      'BASE',     5)"
            );

            // ── Seed personnel ─────────────────────────────────
            stmt.executeUpdate(
                "INSERT IGNORE INTO personnel (personnel_id, name, rank_title, unit, clearance_level, contact) VALUES "
              + "(1, 'Col. Rajesh Sharma', 'Colonel',   '4th Infantry Div', 5, 'rajesh.s@army.mil'),"
              + "(2, 'Maj. Priya Singh',   'Major',     '7th Armoured Bde', 4, 'priya.s@army.mil'),"
              + "(3, 'Capt. Arjun Patel',  'Captain',   '12th Engineers',   3, 'arjun.p@army.mil'),"
              + "(4, 'Sgt. Vikram Rao',    'Sergeant',  '4th Infantry Div', 2, 'vikram.r@army.mil')"
            );

            // ── Seed supplier ──────────────────────────────────
            stmt.executeUpdate(
                "INSERT IGNORE INTO supplier (supplier_id, supplier_name, company_type, license_number, contact_details, address) VALUES "
              + "(1, 'Bharat Dynamics Ltd',     'GOVT',    'GOV-BDL-001', '+91-40-24340010', 'Hyderabad, Telangana'),"
              + "(2, 'Hindustan Aeronautics',    'GOVT',    'GOV-HAL-002', '+91-80-22320231', 'Bangalore, Karnataka'),"
              + "(3, 'Tata Advanced Systems',    'PRIVATE', 'PVT-TAS-003', '+91-22-66657700', 'Mumbai, Maharashtra')"
            );

            System.out.println("✔ All 12 tables initialized successfully!");

        } catch (SQLException e) {
            System.err.println("Error initializing database tables:");
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to initialize database tables!\n" + e.getMessage(),
                "Database Init Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Migrate old CHAR(1) categories (A/B/C/D) to defence names.
     */
    private void migrateCategories(Connection conn) {
        try {
            // Check current column type
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet cols = meta.getColumns(null, null, "equipment", "category");
            if (cols.next()) {
                int colSize = cols.getInt("COLUMN_SIZE");
                if (colSize <= 1) {
                    // Old CHAR(1) — need to migrate
                    System.out.println("Migrating category column from CHAR(1) to VARCHAR(30)...");
                    Statement stmt = conn.createStatement();
                    stmt.executeUpdate("ALTER TABLE equipment MODIFY COLUMN category VARCHAR(30) NOT NULL");
                    stmt.executeUpdate("UPDATE equipment SET category = CASE category "
                        + "WHEN 'A' THEN 'WEAPONS' "
                        + "WHEN 'B' THEN 'PROTECTIVE GEAR' "
                        + "WHEN 'C' THEN 'COMMUNICATION' "
                        + "WHEN 'D' THEN 'LOGISTICS' "
                        + "ELSE category END "
                        + "WHERE LENGTH(category) = 1");
                    System.out.println("✔ Category migration complete!");
                }
            }
        } catch (SQLException e) {
            // Table might not exist yet, that's OK
            System.out.println("Category migration skipped (table may be new).");
        }
    }

    private void seedItem(PreparedStatement ps, int id, String name,
                          String category, int qty, int reorder, String desc)
                          throws SQLException {
        ps.setInt(1, id);
        ps.setString(2, name);
        ps.setString(3, category);
        ps.setInt(4, qty);
        ps.setInt(5, reorder);
        ps.setString(6, desc);
        ps.executeUpdate();
    }
}
