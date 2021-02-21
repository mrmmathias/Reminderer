package com.roimaa.reminderer.DB;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {User.class, Reminder.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class RemindererDataBase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract ReminderDao reminderDao();
}
