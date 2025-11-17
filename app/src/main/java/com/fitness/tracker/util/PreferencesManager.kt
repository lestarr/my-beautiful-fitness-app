package com.fitness.tracker.util

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("fitness_tracker_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val KEY_USE_KG = "use_kg"
        private const val KEY_APP_PIN = "app_pin"
        private const val KEY_AUTH_ENABLED = "auth_enabled"
        private const val KEY_TABLE_LOG_COUNT = "table_log_count"
    }

    var currentUserId: Long
        get() = prefs.getLong(KEY_CURRENT_USER_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_CURRENT_USER_ID, value).apply()

    var useKg: Boolean
        get() = prefs.getBoolean(KEY_USE_KG, true)
        set(value) = prefs.edit().putBoolean(KEY_USE_KG, value).apply()

    var appPin: String?
        get() = prefs.getString(KEY_APP_PIN, null)
        set(value) = prefs.edit().putString(KEY_APP_PIN, value).apply()

    var authEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTH_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTH_ENABLED, value).apply()

    var tableLogCount: Int
        get() = prefs.getInt(KEY_TABLE_LOG_COUNT, 3)
        set(value) = prefs.edit().putInt(KEY_TABLE_LOG_COUNT, value).apply()
}
