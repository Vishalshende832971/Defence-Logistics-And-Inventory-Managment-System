package com.defence.model;

/**
 * Personnel — POJO mapping to the 'personnel' table.
 */
public class Personnel {

    private int    personnelId;
    private String name;
    private String rank;
    private String unit;
    private int    clearanceLevel;
    private String contact;

    // ── Constructors ───────────────────────────────────────────
    public Personnel() {}

    public Personnel(String name, String rank, String unit,
                     int clearanceLevel, String contact) {
        this.name           = name;
        this.rank           = rank;
        this.unit           = unit;
        this.clearanceLevel = clearanceLevel;
        this.contact        = contact;
    }

    // ── Getters & Setters ──────────────────────────────────────
    public int    getPersonnelId()                   { return personnelId; }
    public void   setPersonnelId(int id)             { this.personnelId = id; }

    public String getName()                          { return name; }
    public void   setName(String n)                  { this.name = n; }

    public String getRank()                          { return rank; }
    public void   setRank(String r)                  { this.rank = r; }

    public String getUnit()                          { return unit; }
    public void   setUnit(String u)                  { this.unit = u; }

    public int    getClearanceLevel()                { return clearanceLevel; }
    public void   setClearanceLevel(int lv)          { this.clearanceLevel = lv; }

    public String getContact()                       { return contact; }
    public void   setContact(String c)               { this.contact = c; }

    @Override
    public String toString() {
        return name + " (" + rank + ") — " + unit;
    }
}
