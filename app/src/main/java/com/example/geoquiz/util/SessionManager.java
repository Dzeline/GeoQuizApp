package com.example.geoquiz.util;

public class SessionManager {
    private static SessionManager instance;
    private String selectedRiderPhone;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setSelectedRiderPhone(String phone) {
        this.selectedRiderPhone = phone;
    }

    public String getSelectedRiderPhone() {
        return selectedRiderPhone;
    }

    public void clear() {
        selectedRiderPhone = null;
    }
}
