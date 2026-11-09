package com.example

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp

/**
 * Custom Application class providing startup crash immunity and global crash loop protection.
 */
class YemeBingoApplication : Application() {

    companion object {
        private const val TAG = "YemeBingoApp"
        private const val PREF_CRASH_GUARD = "yeme_crash_guard"
        private const val KEY_LAST_CRASH_TIME = "last_crash_timestamp"
        private const val KEY_CRASH_COUNT = "consecutive_crash_count"
        private const val CRASH_LOOP_THRESHOLD_MS = 5000L
    }

    override fun onCreate() {
        super.onCreate()
        setupGlobalCrashHandler()
        checkAndRecoverFromCrashLoop()
        initializeFirebaseSafely()
    }

    /**
     * Intercepts uncaught fatal exceptions before the OS displays "App has a bug".
     */
    private fun setupGlobalCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e(TAG, "FATAL CRASH intercepted on thread [${thread.name}]", throwable)
                recordCrashTimestamp()
            } catch (e: Exception) {
                Log.e(TAG, "Error inside crash handler", e)
            } finally {
                // Pass to system default to allow graceful process termination
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    /**
     * Detects repeated startup crashes within 5 seconds and resets corrupted cache/state.
     */
    private fun checkAndRecoverFromCrashLoop() {
        try {
            val prefs = getSharedPreferences(PREF_CRASH_GUARD, Context.MODE_PRIVATE)
            val lastCrash = prefs.getLong(KEY_LAST_CRASH_TIME, 0L)
            val now = System.currentTimeMillis()
            val crashCount = prefs.getInt(KEY_CRASH_COUNT, 0)

            if (now - lastCrash < CRASH_LOOP_THRESHOLD_MS) {
                val newCount = crashCount + 1
                prefs.edit().putInt(KEY_CRASH_COUNT, newCount).apply()
                if (newCount >= 2) {
                    Log.w(TAG, "Crash loop detected ($newCount startup crashes). Executing emergency recovery...")
                    clearApplicationCache()
                    prefs.edit().putInt(KEY_CRASH_COUNT, 0).apply()
                }
            } else {
                // Reset counter if app launched without an immediate crash
                prefs.edit().putInt(KEY_CRASH_COUNT, 0).apply()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed crash loop check", e)
        }
    }

    private fun recordCrashTimestamp() {
        try {
            val prefs = getSharedPreferences(PREF_CRASH_GUARD, Context.MODE_PRIVATE)
            prefs.edit()
                .putLong(KEY_LAST_CRASH_TIME, System.currentTimeMillis())
                .apply()
        } catch (_: Exception) {}
    }

    /**
     * Clears local cache directory and reset corrupted temporary files.
     */
    private fun clearApplicationCache() {
        try {
            cacheDir.deleteRecursively()
            Log.i(TAG, "Application cache successfully cleared to break crash loop.")
        } catch (e: Exception) {
            Log.e(TAG, "Could not clear application cache", e)
        }
    }

    /**
     * Ensures Firebase is initialized safely without throwing unhandled exceptions.
     */
    private fun initializeFirebaseSafely() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val app = FirebaseApp.initializeApp(this)
                Log.i(TAG, "Firebase initialized safely on startup: ${app?.name}")
            } else {
                Log.i(TAG, "Firebase was already initialized via ContentProvider.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Non-fatal: Firebase initialization failed during startup", e)
        }
    }
}
