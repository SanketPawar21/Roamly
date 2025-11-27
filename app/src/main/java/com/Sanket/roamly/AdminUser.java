package com.Sanket.roamly;

public class AdminUser {
    public String uid;
    public String name;
    public String email;
    public double rating;
    public long ratingCount;
    public long reportsCount;
    public boolean blocked;

    public AdminUser() {}

    public AdminUser(String uid, String name, String email,
                     double rating, long ratingCount,
                     long reportsCount, boolean blocked) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.rating = rating;
        this.ratingCount = ratingCount;
        this.reportsCount = reportsCount;
        this.blocked = blocked;
    }
}
