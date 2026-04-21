package com.defence.manager;

import com.defence.model.User;

/**
 * SessionManager — Singleton that stores the currently logged-in user.
 * Persists across frame transitions.
 */
public class SessionManager {

    private static SessionManager instance;
    private User   currentUser;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // ── User session ───────────────────────────────────────────
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    public String getUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        this.currentUser = null;
    }
}
