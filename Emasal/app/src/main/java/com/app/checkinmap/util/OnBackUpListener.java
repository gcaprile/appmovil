package com.app.checkinmap.util;

import java.io.File;
import java.util.List;

/**
 * This interface help us to notify
 * the network state.
 */
public interface OnBackUpListener {
    void onBackUp(boolean success, String message,List<File> backupFiles);
}
