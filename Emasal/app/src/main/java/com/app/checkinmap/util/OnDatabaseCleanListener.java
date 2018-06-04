package com.app.checkinmap.util;

/**
 * This class help us to notify when database has been
 * cleaned
 */

public interface OnDatabaseCleanListener {
    void onDatabaseClean(int messageResource, int totalItems);
}
