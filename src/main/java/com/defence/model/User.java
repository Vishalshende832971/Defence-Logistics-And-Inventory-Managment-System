package com.defence.model;

import java.sql.Timestamp;

/**
 * User — POJO mapping to the 'users' table.
 */
public class User {

    private int       userId;
    private String    username;
    private String    passwordHash;
    private String    role;         // ADMIN / OFFICER / VIEWER
    private Timestamp createdAt;

    // ── Constructors ───────────────────────────────────────────
    public User() {}

    public User(int userId, String username, String role) {
        this.userId   = userId;
        this.username = username;
        this.role     = role;
    }

    // ── Getters & Setters ──────────────────────────────────────
    public int       getUserId()                    { return userId; }
    public void      setUserId(int id)              { this.userId = id; }

    public String    getUsername()                   { return username; }
    public void      setUsername(String name)        { this.username = name; }

    public String    getPasswordHash()              { return passwordHash; }
    public void      setPasswordHash(String hash)   { this.passwordHash = hash; }

    public String    getRole()                      { return role; }
    public void      setRole(String role)           { this.role = role; }

    public Timestamp getCreatedAt()                 { return createdAt; }
    public void      setCreatedAt(Timestamp ts)     { this.createdAt = ts; }

    @Override
    public String toString() {
        return username + " [" + role + "]";
    }
}
