package com.roimaa.reminderer.DB;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class Reminder {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "message")
    private String message;

    @ColumnInfo(name = "location_x")
    private double lon;

    @ColumnInfo(name = "location_y")
    private double lat;

    @ColumnInfo(name = "reminder_time")
    private Date reminderTime;

    @ColumnInfo(name = "creation_time")
    private Date creationTime;

    @ColumnInfo(name = "reminder_seen")
    private boolean reminderSeen;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public Date getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(Date reminderTime) {
        this.reminderTime = reminderTime;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public boolean isReminderSeen() {
        return reminderSeen;
    }

    public void setReminderSeen(boolean reminderSeen) {
        this.reminderSeen = reminderSeen;
    }
}
