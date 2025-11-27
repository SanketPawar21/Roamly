package com.Sanket.roamly;

public class HomeTripModel {
    private String tripId;
    private String userId;
    private String location;
    private String date;
    private String period;
    private String preference;
    private String peopleNeeded;
    private String status;
    private String thingsToCarry;
    private long timestamp;
    private String hostName;

    public HomeTripModel() {}

    public HomeTripModel(String tripId, String userId, String location, String date, String period,
                         String preference, String peopleNeeded, String status,
                         String thingsToCarry, long timestamp, String hostName) {
        this.tripId = tripId;
        this.userId = userId;
        this.location = location;
        this.date = date;
        this.period = period;
        this.preference = preference;
        this.peopleNeeded = peopleNeeded;
        this.status = status;
        this.thingsToCarry = thingsToCarry;
        this.timestamp = timestamp;
        this.hostName = hostName;
    }

    // Getters and Setters
    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public String getPreference() { return preference; }
    public void setPreference(String preference) { this.preference = preference; }

    public String getPeopleNeeded() { return peopleNeeded; }
    public void setPeopleNeeded(String peopleNeeded) { this.peopleNeeded = peopleNeeded; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getThingsToCarry() { return thingsToCarry; }
    public void setThingsToCarry(String thingsToCarry) { this.thingsToCarry = thingsToCarry; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }
}
