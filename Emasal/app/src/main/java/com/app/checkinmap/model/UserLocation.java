package com.app.checkinmap.model;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class UserLocation extends RealmObject{

    @PrimaryKey
    private long   id;
    private double latitude;
    private double longitude;
    private String date;
    private long   routeId;
    private double distance;
    private double distanceB;
    private double distanceC;
    private double accuracy;
    private double accuracyError;

    public double getAccuracyError() {
        return accuracyError;
    }

    public void setAccuracyError(double accuracyError) {
        this.accuracyError = accuracyError;
    }

    public double getDistanceB() {
        return distanceB;
    }

    public void setDistanceB(double distanceB) {
        this.distanceB = distanceB;
    }

    public long getRouteId() {
        return routeId;
    }

    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public double getDistanceC() {
        return distanceC;
    }

    public void setDistanceC(double distanceC) {
        this.distanceC = distanceC;
    }
}
