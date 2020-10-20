package com.vitmas.womansafetyfinal.LocationsActivities;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.firestore.auth.User;

public class UserLocation {
    private GeoPoint geoPoint;
    private @ServerTimestamp String timestamp;

    public UserLocation(GeoPoint geoPoint, String timestamp, User user) {
        this.geoPoint = geoPoint;
        this.timestamp = timestamp;
    }

    public UserLocation() {
        this.geoPoint = geoPoint;
        this.timestamp = timestamp;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "UserLocation{" +
                "geoPoint=" + geoPoint +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
