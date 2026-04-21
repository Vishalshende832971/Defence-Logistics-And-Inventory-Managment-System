package com.defence.model;

import java.sql.Timestamp;

/**
 * Transaction — POJO mapping to the 'transactions' table.
 */
public class Transaction {

    private int       transactionId;
    private int       itemId;
    private String    action;       // ADD / ISSUE / RETURN
    private int       quantity;
    private String    performedBy;  // username
    private Timestamp timestamp;

    // Joined field (not in table, used for display)
    private String    itemName;

    // ── Constructors ───────────────────────────────────────────
    public Transaction() {}

    public Transaction(int itemId, String action, int quantity,
                       String performedBy) {
        this.itemId      = itemId;
        this.action      = action;
        this.quantity    = quantity;
        this.performedBy = performedBy;
    }

    // ── Getters & Setters ──────────────────────────────────────
    public int       getTransactionId()            { return transactionId; }
    public void      setTransactionId(int id)      { this.transactionId = id; }

    public int       getItemId()                   { return itemId; }
    public void      setItemId(int id)             { this.itemId = id; }

    public String    getAction()                   { return action; }
    public void      setAction(String action)      { this.action = action; }

    public int       getQuantity()                 { return quantity; }
    public void      setQuantity(int qty)          { this.quantity = qty; }

    public String    getPerformedBy()              { return performedBy; }
    public void      setPerformedBy(String user)   { this.performedBy = user; }

    public Timestamp getTimestamp()                { return timestamp; }
    public void      setTimestamp(Timestamp ts)    { this.timestamp = ts; }

    public String    getItemName()                 { return itemName; }
    public void      setItemName(String name)      { this.itemName = name; }

    @Override
    public String toString() {
        return String.format("TX#%d | Item %d | %s | Qty: %d | By: %s | %s",
                transactionId, itemId, action, quantity, performedBy, timestamp);
    }
}
