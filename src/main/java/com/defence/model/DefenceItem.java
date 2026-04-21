package com.defence.model;

import java.sql.Timestamp;

/**
 * DefenceItem — POJO mapping to the 'equipment' table.
 */
public class DefenceItem {

    // ── Defence Category Constants ─────────────────────────────
    public static final String[] CATEGORIES = {
        "WEAPONS",
        "VEHICLES",
        "AMMUNITION",
        "COMMUNICATION",
        "PROTECTIVE GEAR",
        "MEDICAL",
        "ELECTRONICS",
        "LOGISTICS"
    };

    private int       itemId;
    private String    itemName;
    private String    category;       // Defence category name
    private int       quantity;
    private int       reorderLevel;
    private String    description;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // ── Constructors ───────────────────────────────────────────
    public DefenceItem() {}

    public DefenceItem(String itemName, String category, int quantity,
                       int reorderLevel, String description) {
        this.itemName     = itemName;
        this.category     = category;
        this.quantity     = quantity;
        this.reorderLevel = reorderLevel;
        this.description  = description;
    }

    public DefenceItem(int itemId, String itemName, String category,
                       int quantity, int reorderLevel, String description) {
        this(itemName, category, quantity, reorderLevel, description);
        this.itemId = itemId;
    }

    // ── Getters & Setters ──────────────────────────────────────
    public int       getItemId()       { return itemId; }
    public void      setItemId(int id) { this.itemId = id; }

    public String    getItemName()              { return itemName; }
    public void      setItemName(String name)   { this.itemName = name; }

    public String    getCategory()                  { return category; }
    public void      setCategory(String category)   { this.category = category; }

    public int       getQuantity()              { return quantity; }
    public void      setQuantity(int qty)       { this.quantity = qty; }

    public int       getReorderLevel()              { return reorderLevel; }
    public void      setReorderLevel(int level)     { this.reorderLevel = level; }

    public String    getDescription()               { return description; }
    public void      setDescription(String desc)    { this.description = desc; }

    public Timestamp getCreatedAt()                 { return createdAt; }
    public void      setCreatedAt(Timestamp t)      { this.createdAt = t; }

    public Timestamp getUpdatedAt()                 { return updatedAt; }
    public void      setUpdatedAt(Timestamp t)      { this.updatedAt = t; }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) — Qty: %d | Reorder: %d",
                itemId, itemName, category, quantity, reorderLevel);
    }
}
