package com.app.checkinmap.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * This class handle all the information
 * about the user action.
 */

public class UserLog extends RealmObject {
    @PrimaryKey
    private long   id;
    private String userAction;
    private String date;
    private String userId;
    private long   routeId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserAction() {
        return userAction;
    }

    public void setUserAction(String userAction) {
        this.userAction = userAction;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getRouteId() {
        return routeId;
    }

    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }
}
