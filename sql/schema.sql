-- ============================================================
-- Defence Logistics and Inventory Management System
-- Complete MySQL Schema — MySQL 8.0
-- MIT Academy of Engineering
-- ============================================================

CREATE DATABASE IF NOT EXISTS defence_logistics_db;
USE defence_logistics_db;

-- ============================================================
-- 1. USERS — Authentication & Role-Based Access
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          ENUM('ADMIN','OFFICER','VIEWER') NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================
-- 2. EQUIPMENT — Maps to DefenceItem model
-- ============================================================
CREATE TABLE IF NOT EXISTS equipment (
    item_id       INT AUTO_INCREMENT PRIMARY KEY,
    item_name     VARCHAR(100) NOT NULL,
    category      CHAR(1)      NOT NULL COMMENT 'A/B/C/D',
    quantity      INT          NOT NULL DEFAULT 0,
    reorder_level INT          NOT NULL DEFAULT 10,
    description   TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================
-- 3. PERSONNEL
-- ============================================================
CREATE TABLE IF NOT EXISTS personnel (
    personnel_id    INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    rank            VARCHAR(50),
    unit            VARCHAR(100),
    clearance_level INT DEFAULT 1,
    contact         VARCHAR(100)
) ENGINE=InnoDB;

-- ============================================================
-- 4. DEPOT
-- ============================================================
CREATE TABLE IF NOT EXISTS depot (
    depot_id       INT AUTO_INCREMENT PRIMARY KEY,
    depot_name     VARCHAR(100) NOT NULL,
    location       VARCHAR(150),
    depot_type     ENUM('FORWARD','TERMINAL','BASE') NOT NULL,
    security_level INT DEFAULT 1
) ENGINE=InnoDB;

-- ============================================================
-- 5. SUPPLIER
-- ============================================================
CREATE TABLE IF NOT EXISTS supplier (
    supplier_id    INT AUTO_INCREMENT PRIMARY KEY,
    supplier_name  VARCHAR(100) NOT NULL,
    company_type   ENUM('GOVT','PRIVATE') NOT NULL,
    license_number VARCHAR(50),
    contact_details VARCHAR(200),
    address        TEXT
) ENGINE=InnoDB;

-- ============================================================
-- 6. TRANSACTIONS — Inventory transaction log
-- ============================================================
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    item_id        INT NOT NULL,
    action         ENUM('ADD','ISSUE','RETURN') NOT NULL,
    quantity       INT NOT NULL,
    performed_by   VARCHAR(50),
    timestamp      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES equipment(item_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- 7. INVENTORY — Per-depot stock tracking
-- ============================================================
CREATE TABLE IF NOT EXISTS inventory (
    inventory_id       INT AUTO_INCREMENT PRIMARY KEY,
    item_id            INT NOT NULL,
    depot_id           INT NOT NULL,
    quantity_available INT NOT NULL DEFAULT 0,
    reorder_level      INT DEFAULT 10,
    max_stock_level    INT DEFAULT 500,
    last_updated       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                       ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES equipment(item_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (depot_id) REFERENCES depot(depot_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- 8. ORDERS — Procurement orders to suppliers
-- ============================================================
CREATE TABLE IF NOT EXISTS orders (
    order_id      INT AUTO_INCREMENT PRIMARY KEY,
    supplier_id   INT NOT NULL,
    order_date    DATE NOT NULL,
    delivery_date DATE,
    status        ENUM('PENDING','APPROVED','DELIVERED','CANCELLED')
                  DEFAULT 'PENDING',
    approved_by   INT,
    FOREIGN KEY (supplier_id) REFERENCES supplier(supplier_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(user_id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- 9. ORDER_DETAILS — Line items within an order
-- ============================================================
CREATE TABLE IF NOT EXISTS order_details (
    order_detail_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id        INT NOT NULL,
    item_id         INT NOT NULL,
    quantity        INT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (item_id) REFERENCES equipment(item_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- 10. SHIPMENT — Inter-depot transfers
-- ============================================================
CREATE TABLE IF NOT EXISTS shipment (
    shipment_id    INT AUTO_INCREMENT PRIMARY KEY,
    from_depot     INT NOT NULL,
    to_depot       INT NOT NULL,
    dispatch_date  DATE,
    arrival_date   DATE,
    transport_mode ENUM('ROAD','AIR','RAIL','SEA') NOT NULL,
    status         ENUM('PENDING','IN_TRANSIT','DELIVERED')
                   DEFAULT 'PENDING',
    FOREIGN KEY (from_depot) REFERENCES depot(depot_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (to_depot) REFERENCES depot(depot_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- 11. SHIPMENT_DETAILS — Items within a shipment
-- ============================================================
CREATE TABLE IF NOT EXISTS shipment_details (
    shipment_detail_id INT AUTO_INCREMENT PRIMARY KEY,
    shipment_id        INT NOT NULL,
    item_id            INT NOT NULL,
    quantity           INT NOT NULL,
    FOREIGN KEY (shipment_id) REFERENCES shipment(shipment_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (item_id) REFERENCES equipment(item_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- 12. MAINTENANCE — Equipment maintenance tracking
-- ============================================================
CREATE TABLE IF NOT EXISTS maintenance (
    maintenance_id   INT AUTO_INCREMENT PRIMARY KEY,
    item_id          INT NOT NULL,
    personnel_id     INT,
    maintenance_type ENUM('ROUTINE','REPAIR','INSPECTION') NOT NULL,
    status           ENUM('PENDING','IN_PROGRESS','COMPLETED')
                     DEFAULT 'PENDING',
    scheduled_date   DATE,
    completed_date   DATE,
    remarks          TEXT,
    FOREIGN KEY (item_id) REFERENCES equipment(item_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- SEED DATA — Default users  (passwords are SHA-256 hashed)
-- ============================================================
-- Plaintext passwords for testing:
--   admin   / admin123
--   officer / officer123
--   viewer  / viewer123
-- ============================================================
INSERT IGNORE INTO users (username, password_hash, role) VALUES
    ('admin',   SHA2('admin123',   256), 'ADMIN'),
    ('officer', SHA2('officer123', 256), 'OFFICER'),
    ('viewer',  SHA2('viewer123',  256), 'VIEWER');

-- ============================================================
-- SEED DATA — Sample equipment
-- ============================================================
INSERT IGNORE INTO equipment (item_name, category, quantity, reorder_level, description) VALUES
    ('AK-47 Assault Rifle',         'A', 500, 50,  'Standard infantry assault rifle'),
    ('9mm Pistol',                   'A', 300, 30,  'Sidearm for officers'),
    ('Body Armor Vest',              'B', 200, 20,  'Kevlar body armor — Level IIIA'),
    ('Night Vision Goggles',         'B', 100, 15,  'Gen-3 NVG for night operations'),
    ('Combat Helmet',                'B', 400, 40,  'Ballistic helmet MICH-2000'),
    ('Field Radio Set',              'C', 80,  10,  'Tactical radio — 30 km range'),
    ('Medical First Aid Kit',        'D', 600, 60,  'Standard military first aid kit'),
    ('Ammunition 7.62mm (box)',      'A', 1000,100, '7.62×39mm — box of 500 rounds'),
    ('Portable Generator',           'C', 25,  5,   '5 kW diesel portable generator'),
    ('Camouflage Netting (roll)',    'D', 150, 20,  '10×10 m woodland pattern net');

-- ============================================================
-- SEED DATA — Sample depot
-- ============================================================
INSERT IGNORE INTO depot (depot_name, location, depot_type, security_level) VALUES
    ('Alpha Forward Base',  'Rajasthan Sector', 'FORWARD',  3),
    ('Bravo Terminal Depot', 'Pune Cantonment',  'TERMINAL', 2),
    ('Charlie Base Depot',   'Delhi Cantt',      'BASE',     5);

-- ============================================================
-- SEED DATA — Sample personnel
-- ============================================================
INSERT IGNORE INTO personnel (name, rank, unit, clearance_level, contact) VALUES
    ('Col. Rajesh Sharma',  'Colonel',   '4th Infantry Div', 5, 'rajesh.s@army.mil'),
    ('Maj. Priya Singh',    'Major',     '7th Armoured Bde', 4, 'priya.s@army.mil'),
    ('Capt. Arjun Patel',   'Captain',   '12th Engineers',   3, 'arjun.p@army.mil'),
    ('Sgt. Vikram Rao',     'Sergeant',  '4th Infantry Div', 2, 'vikram.r@army.mil');
