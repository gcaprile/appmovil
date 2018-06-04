package com.app.checkinmap.util;

/**
 * This interface help us to notify
 * the network state.
 */
public interface OnNetworkListener {
    void onNetwork(boolean success, String message);
}
