
<div align="center">

<img src="DefenseLogo.png" alt="Defence Logistics Logo" width="120"/>

# 🛡️ Defence Logistics & Inventory Management System

### *Precision. Security. Control.*

**A professional-grade, full-featured desktop application for military logistics operations — built in Java.**

<br/>

[![Java](https://img.shields.io/badge/Java-11-007396?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com)
[![Maven](https://img.shields.io/badge/Maven-3.x-C71A22?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com)
[![Swing](https://img.shields.io/badge/GUI-Java%20Swing-6DB33F?style=for-the-badge)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)
[![Status](https://img.shields.io/badge/Status-Active-brightgreen?style=for-the-badge)]()

<br/>

> Developed as an academic project at **MIT Academy of Engineering**  
> A centralized system for managing military assets, personnel, logistics, and maintenance — all from a single secure desktop interface.

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [System Architecture](#-system-architecture)
- [Database Schema](#-database-schema)
- [UI Modules](#-ui-modules)
- [Tech Stack](#-tech-stack)
- [Getting Started](#-getting-started)
- [Default Credentials](#-default-credentials)
- [Project Structure](#-project-structure)
- [Contributors](#-contributors)

---

## 🎯 Overview

The **Defence Logistics & Inventory Management System** is designed to replace manual, paper-based military logistics processes with a **secure, centralized, and auditable** digital system.

It manages the complete lifecycle of defence assets — from procurement and depot storage, to field issuance, shipment tracking, and maintenance scheduling — while enforcing strict **role-based access control** (Admin / Officer / Viewer) across all operations.

The application features a custom **Dark Military Theme** — a bespoke Java Swing UI with a deep green–black colour palette, designed for low-ambient-light operational environments.

---

## ✨ Key Features

### 🔐 Authentication & Role-Based Access
- Secure **SHA-256 hashed passwords** stored in the database.
- Three role levels: **ADMIN**, **OFFICER**, **VIEWER** with different access permissions.
- Singleton `SessionManager` persists the active user session across all UI frames.
- Automatic session expiry on logout.

### 📦 Inventory Management
- Full **CRUD operations** on the `equipment` table via `InventoryManager`.
- Categories include: `WEAPONS`, `PROTECTIVE GEAR`, `ELECTRONICS`, `COMMUNICATION`, `MEDICAL`, `AMMUNITION`, `LOGISTICS`, `VEHICLES`.
- **Auto-incremented item IDs** — no manual ID entry needed.
- Live **low-stock alerting** — triggers warning when quantity falls below `reorder_level`.
- Smart combo-box dropdowns showing `ID — Item Name [Category]` for user-friendly selection.
- Full-text **search & filter** by keyword or category.
- **Category-wise summary** report (item count + total quantity per category).

### 🏭 Depot Management
- Three depot types: `FORWARD`, `TERMINAL`, `BASE`.
- Per-depot inventory tracking with `max_stock_level` and `reorder_level`.
- Pre-seeded depots: *Alpha Forward Base* (Rajasthan), *Bravo Terminal Depot* (Pune), *Charlie Base Depot* (Delhi).

### 🚢 Shipment Tracking
- Track inter-depot shipments with `from_depot` → `to_depot` routing.
- Transport modes: `ROAD`, `AIR`, `RAIL`, `SEA`.
- Status lifecycle: `PENDING` → `IN_TRANSIT` → `DELIVERED`.
- Dispatch and arrival date recording.

### 🛒 Order & Procurement Management
- Place supply orders linked to verified **Government or Private suppliers**.
- Full order lifecycle: `PENDING` → `APPROVED` → `DELIVERED` / `CANCELLED`.
- Orders linked to approving officers via foreign key to the users table.
- Granular `order_details` table for per-item quantity tracking within a single order.

### 🔧 Maintenance Scheduling
- Schedule maintenance tasks of type: `ROUTINE`, `REPAIR`, `INSPECTION`.
- Status tracking: `PENDING` → `IN_PROGRESS` → `COMPLETED`.
- Automatic `completed_date` stamped on completion via `CURDATE()`.
- **Overdue detection** — surfaces all maintenance records past scheduled date and not yet completed.
- Assign specific personnel to each maintenance task.

### 🧑‍✈️ Personnel Management
- Store personnel with `rank_title`, `unit`, `clearance_level`, and `contact`.
- Link personnel directly to maintenance tasks and transactions.
- Pre-seeded sample records: Colonel, Major, Captain, Sergeant.

### 📊 Reporting & Analytics (`ReportsFrame`)
- Category-wise inventory summary reports.
- Transaction history and audit logs.
- Maintenance backlog and overdue task lists.
- Low stock alerts dashboard.

### 🔭 Interactive Database Explorer (`DataExplorerFrame`)
- Browse every database table directly from the UI — no external tool needed.
- Sortable, scrollable table view of all records.

### 🔔 Alerts Module (`AlertsFrame`)
- Consolidated alert dashboard for low-stock items and overdue maintenance.
- Color-coded severity levels — orange for warnings, red for critical.

---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     UI Layer (Swing)                    │
│  LoginFrame · DashboardFrame · AddItemFrame             │
│  ViewItemsFrame · IssueItemFrame · ReturnItemFrame      │
│  MaintenanceFrame · TransactionFrame · ReportsFrame     │
│  AlertsFrame · DataExplorerFrame                        │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│                  Manager Layer (Business Logic)          │
│  InventoryManager · MaintenanceManager · SessionManager │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│                    DB Layer (JDBC)                       │
│          DatabaseManager (Singleton)                    │
│          MySQL Connector/J 8.0.33                       │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│                  MySQL 8.0 Database                     │
│         defence_logistics_db · 12 Tables                │
└─────────────────────────────────────────────────────────┘
```

---

## 🗄️ Database Schema

The system uses **12 fully normalized, InnoDB tables** with cascading foreign key constraints:

| # | Table | Description |
|---|-------|-------------|
| 1 | `users` | Authenticated users with hashed passwords and roles |
| 2 | `equipment` | Master catalogue of all defence items |
| 3 | `personnel` | Military personnel assigned to units |
| 4 | `depot` | Storage depots (Forward / Terminal / Base) |
| 5 | `supplier` | Government and private suppliers |
| 6 | `transactions` | Audit log of all ADD / ISSUE / RETURN events |
| 7 | `inventory` | Per-depot stock levels with reorder and max limits |
| 8 | `orders` | Procurement orders from suppliers |
| 9 | `order_details` | Line items for each order |
| 10 | `shipment` | Inter-depot shipment records |
| 11 | `shipment_details` | Line items per shipment |
| 12 | `maintenance` | Maintenance schedule and completion log |

### Entity Relationship Highlights
- `equipment` ← `transactions`, `inventory`, `order_details`, `shipment_details`, `maintenance`
- `depot` ← `inventory`, `shipment` (from + to)
- `supplier` ← `orders` ← `order_details`
- `personnel` ← `maintenance`
- `users` ← `orders` (approver)

---

## 🖥️ UI Modules

| Frame | Description |
|-------|-------------|
| `LoginFrame` | Secure login with SHA-256 password verification |
| `DashboardFrame` | Central hub — quick stats, navigation menu |
| `AddItemFrame` | Add new equipment with category dropdowns |
| `ViewItemsFrame` | Browse, search, and filter all inventory |
| `IssueItemFrame` | Issue items with smart stock validation & alerts |
| `ReturnItemFrame` | Accept returned items back into stock |
| `MaintenanceFrame` | Schedule, view, and update maintenance tasks |
| `TransactionFrame` | Full transaction history log |
| `ReportsFrame` | Analytics, category summaries, low-stock reports |
| `AlertsFrame` | Consolidated alert panel for critical events |
| `DataExplorerFrame` | Browse any database table directly from the UI |

---

## 🛠️ Tech Stack

| Component | Technology |
|-----------|-----------|
| **Language** | Java 11 |
| **GUI** | Java Swing + AWT (Custom Dark Military Theme) |
| **Build** | Apache Maven 3.x |
| **Database** | MySQL 8.0+ |
| **JDBC Driver** | `mysql-connector-java 8.0.33` |
| **Packaging** | Maven Shade Plugin (fat JAR with all dependencies) |
| **Security** | SHA-256 password hashing via MySQL `SHA2()` |

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Version |
|-------------|---------|
| JDK | 11 or higher |
| Apache Maven | 3.6+ |
| MySQL Server | 8.0+ |

---

### Step 1 — Clone the Repository

```bash
git clone https://github.com/your-username/defence-logistics.git
cd defence-logistics
```

---

### Step 2 — Configure the Database

**2a. Create the database in MySQL:**
```sql
CREATE DATABASE defence_logistics_db;
```

**2b. Import the schema:**
```bash
mysql -u root -p defence_logistics_db < sql/schema.sql
```

**2c. Update database credentials:**

Edit `src/main/resources/db.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/defence_logistics_db?useSSL=false&serverTimezone=UTC
db.username=root
db.password=your_password_here
```

---

### Step 3 — Build the Project

```bash
mvn clean install
```

This generates a **fat/shaded JAR** in the `target/` directory containing all dependencies.

---

### Step 4 — Run the Application

```bash
java -jar target/defence-logistics-1.0-SNAPSHOT.jar
```

> ⚠️ **Note:** The application tests the database connection on startup. If it fails, a dialog will guide you on the exact fix required before exiting gracefully.

---

### Step 5 — First-Time Setup

On the very first run, the `DatabaseManager` **auto-creates all 12 tables** and seeds them with:
- ✅ **3 default users** (admin, officer, viewer)
- ✅ **10 defence equipment items** across 8 categories
- ✅ **3 depots** (Rajasthan, Pune, Delhi)
- ✅ **4 personnel records** (Colonel → Sergeant)
- ✅ **3 suppliers** (Bharat Dynamics, HAL, Tata Advanced Systems)

No manual SQL inserts needed.

---

## 🔑 Default Credentials

| Role | Username | Password |
|------|----------|----------|
| 🔴 Admin | `admin` | `admin123` |
| 🟡 Officer | `officer` | `officer123` |
| 🟢 Viewer | `viewer` | `viewer123` |

> ⚠️ **Important:** Change these credentials immediately after first login in a production environment.

---

## 📁 Project Structure

```
DefenceProject/
│
├── 📄 pom.xml                          # Maven build config & dependencies
├── 🖼️  DefenseLogo.png                  # Application logo
│
├── 📂 sql/
│   └── schema.sql                      # Full database schema (12 tables)
│
└── 📂 src/main/java/com/defence/
    │
    ├── 🚀 Main.java                     # Entry point — theme bootstrap + DB init
    │
    ├── 📂 db/
    │   └── DatabaseManager.java        # Singleton JDBC connection manager
    │
    ├── 📂 manager/
    │   ├── InventoryManager.java       # CRUD + search + stock logic
    │   ├── MaintenanceManager.java     # Scheduling, overdue detection
    │   └── SessionManager.java        # Active user session state
    │
    ├── 📂 model/
    │   ├── DefenceItem.java            # Equipment POJO
    │   ├── MaintenanceRecord.java      # Maintenance POJO
    │   ├── Personnel.java              # Personnel POJO
    │   ├── Transaction.java            # Transaction POJO
    │   └── User.java                   # User POJO (with role)
    │
    └── 📂 ui/
        ├── LoginFrame.java             # Secure login screen
        ├── DashboardFrame.java         # Main dashboard & navigation
        ├── AddItemFrame.java           # Add new equipment
        ├── ViewItemsFrame.java         # Browse & search inventory
        ├── IssueItemFrame.java         # Issue items to personnel
        ├── ReturnItemFrame.java        # Accept item returns
        ├── MaintenanceFrame.java       # Maintenance scheduling
        ├── TransactionFrame.java       # Full transaction history
        ├── ReportsFrame.java           # Analytics & reports
        ├── AlertsFrame.java            # Critical alerts panel
        ├── DataExplorerFrame.java      # Live DB table explorer
        └── RoundedBorder.java          # Custom UI border utility
```

---

## 🎨 UI Theme

The application applies a global **Dark Military Theme** using Java Swing's `UIManager`:

| Element | Color |
|---------|-------|
| Background | `#1B2A1B` (Deep Forest Green) |
| Panel | `#243324` |
| Button | `#2E5E2E` |
| Button Hover | `#4A8C4A` |
| Text | `#E8F5E8` (Soft White-Green) |
| Accent | `#8FA832` (Military Olive) |
| Alert Red | `#8B0000` (Dark Red) |
| Alert Orange | `#CC6600` |
| Table Header | `#1A3A1A` |

---

## 🔮 Future Enhancements

- [ ] PDF/Excel report export from `ReportsFrame`
- [ ] Barcode scanning for item identification
- [ ] Email/SMS alerts for critical low-stock events
- [ ] Multi-base server deployment with central DB
- [ ] Audit trail logging per user action
- [ ] Enhanced role permissions (granular per-module)

---

## 🤝 Contributors

| Name | Role |
|------|------|
| Your Name | Developer / Designer |

---

## 📜 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Built with** ☕ **Java and** 🛡️ **pride at MIT Academy of Engineering**

*"Equipped for precision. Engineered for defence."*

</div>
=======
# Defence-Logistics-And-Inventory-Managment-System
DBMS project 
>>>>>>> 2b19d29a1e512615e429fe50289f53173ea2de99
