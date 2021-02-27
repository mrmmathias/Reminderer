package com.roimaa.reminderer.DB;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ReminderDao {

    @Insert
    long insert(Reminder reminder);

    @Delete
    void delete(Reminder reminder);

    @Update
    void update(Reminder reminder);

    @Query("SELECT * FROM Reminder WHERE user_id = :userId")
    List<Reminder> getUserReminders(int userId);

    @Query("SELECT * FROM Reminder WHERE id = :id")
    Reminder getById(int id);
}
