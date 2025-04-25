package com.example.geoquiz.presentation.feature_role;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;



/**
 * RoleManager manages persistent user role state (USER or RIDER)
 */
public class RoleManager {
    public enum Role {
        USER,
        RIDER
    }

    private static final String PREF_NAME = "AppPrefs";
    private static final String KEY_ROLE = "current_role";

    /**
     * Saves the selected role into SharedPreferences.
     *
     * @param context The application context
     * @param role    The role to save
     */
    public static void setRole( @NonNull Context context,@NonNull Role role) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_ROLE, role.name()).apply();
    }

    /**
     * Retrieves the current role from SharedPreferences.
     *
     * @param context The application context
     * @return The retrieved Role, defaults to USER if invalid or missing
     */
    public static Role getRole(@NonNull Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String roleString = prefs.getString(KEY_ROLE, Role.USER.name());
        try {
            return Role.valueOf(roleString);
        } catch (IllegalArgumentException e) {
            return Role.USER;
        }
    }
}
