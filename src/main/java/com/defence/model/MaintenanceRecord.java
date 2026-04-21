package com.defence.model;

import java.sql.Date;

/**
 * MaintenanceRecord — POJO mapping to the 'maintenance' table.
 */
public class MaintenanceRecord {

    private int    maintenanceId;
    private int    itemId;
    private int    personnelId;
    private String maintenanceType; // ROUTINE / REPAIR / INSPECTION
    private String status;          // PENDING / IN_PROGRESS / COMPLETED
    private Date   scheduledDate;
    private Date   completedDate;
    private String remarks;

    // Joined fields for display
    private String itemName;
    private String personnelName;

    // ── Constructors ───────────────────────────────────────────
    public MaintenanceRecord() {}

    public MaintenanceRecord(int itemId, String maintenanceType,
                             Date scheduledDate, String remarks) {
        this.itemId          = itemId;
        this.maintenanceType = maintenanceType;
        this.scheduledDate   = scheduledDate;
        this.remarks         = remarks;
        this.status          = "PENDING";
    }

    // ── Getters & Setters ──────────────────────────────────────
    public int    getMaintenanceId()                    { return maintenanceId; }
    public void   setMaintenanceId(int id)              { this.maintenanceId = id; }

    public int    getItemId()                           { return itemId; }
    public void   setItemId(int id)                     { this.itemId = id; }

    public int    getPersonnelId()                      { return personnelId; }
    public void   setPersonnelId(int id)                { this.personnelId = id; }

    public String getMaintenanceType()                  { return maintenanceType; }
    public void   setMaintenanceType(String type)       { this.maintenanceType = type; }

    public String getStatus()                           { return status; }
    public void   setStatus(String status)              { this.status = status; }

    public Date   getScheduledDate()                    { return scheduledDate; }
    public void   setScheduledDate(Date d)              { this.scheduledDate = d; }

    public Date   getCompletedDate()                    { return completedDate; }
    public void   setCompletedDate(Date d)              { this.completedDate = d; }

    public String getRemarks()                          { return remarks; }
    public void   setRemarks(String r)                  { this.remarks = r; }

    public String getItemName()                         { return itemName; }
    public void   setItemName(String name)              { this.itemName = name; }

    public String getPersonnelName()                    { return personnelName; }
    public void   setPersonnelName(String name)         { this.personnelName = name; }

    @Override
    public String toString() {
        return String.format("MX#%d | Item %d | %s | %s | Sched: %s",
                maintenanceId, itemId, maintenanceType, status, scheduledDate);
    }
}
