package com.roimaa.reminderer.DB;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {

    @Insert
    void insert(User user);

    @Query("SELECT * FROM User WHERE user_name = :userName")
    User findByName(String userName);

    @Query("SELECT * FROM User WHERE uid = :Id")
    User findById(int Id);
}
