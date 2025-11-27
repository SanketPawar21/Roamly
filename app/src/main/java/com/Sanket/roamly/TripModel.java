package com.Sanket.roamly;

import java.util.HashMap;
import java.util.Map;

public class TripModel {
    public String tripId, userId, location, date, period, preference, status, thingsToCarry, hostName;

    // peopleNeeded is stored as String in Firebase, keep it String here
    public String peopleNeeded;
    // timestamp is stored as long (System.currentTimeMillis()), so keep it long
    public long timestamp;

    public Map<String, Object> participants = new HashMap<>();

    public TripModel() {}

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getPreference() {
        return preference;
    }

    public void setPreference(String preference) {
        this.preference = preference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThingsToCarry() {
        return thingsToCarry;
    }

    public void setThingsToCarry(String thingsToCarry) {
        this.thingsToCarry = thingsToCarry;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getPeopleNeeded() {
        return peopleNeeded;
    }

    public void setPeopleNeeded(String peopleNeeded) {
        this.peopleNeeded = peopleNeeded;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getParticipants() {
        return participants;
    }

    public void setParticipants(Map<String, Object> participants) {
        this.participants = participants;
    }
}
