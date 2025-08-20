package com.chatchat.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import com.chatchat.model.User;
import java.util.List;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    @Update
    void updateUser(User user);

    @Delete
    void deleteUser(User user);

    @Query("SELECT * FROM users WHERE travelerId = :travelerId")
    User getUserById(String travelerId);

    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    @Query("SELECT * FROM users WHERE isOnline = 1")
    List<User> getOnlineUsers();

    @Query("UPDATE users SET isOnline = :isOnline, lastSeen = :lastSeen WHERE travelerId = :travelerId")
    void updateUserStatus(String travelerId, boolean isOnline, long lastSeen);

    @Query("UPDATE users SET token = :token WHERE travelerId = :travelerId")
    void updateUserToken(String travelerId, String token);

    @Query("DELETE FROM users WHERE travelerId = :travelerId")
    void deleteUserById(String travelerId);
}