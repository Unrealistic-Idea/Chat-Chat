package com.chatchat.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import com.chatchat.model.Message;
import java.util.List;

@Dao
public interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessage(Message message);

    @Update
    void updateMessage(Message message);

    @Delete
    void deleteMessage(Message message);

    @Query("SELECT * FROM messages WHERE messageId = :messageId")
    Message getMessageById(String messageId);

    @Query("SELECT * FROM messages WHERE (senderId = :userId1 AND receiverId = :userId2) OR (senderId = :userId2 AND receiverId = :userId1) ORDER BY timestamp ASC")
    List<Message> getDirectMessages(String userId1, String userId2);

    @Query("SELECT * FROM messages WHERE groupId = :groupId ORDER BY timestamp ASC")
    List<Message> getGroupMessages(String groupId);

    @Query("SELECT * FROM messages WHERE receiverId = :userId AND isRead = 0")
    List<Message> getUnreadMessages(String userId);

    @Query("UPDATE messages SET isRead = 1 WHERE receiverId = :userId AND senderId = :senderId")
    void markMessagesAsRead(String userId, String senderId);

    @Query("UPDATE messages SET isRecalled = 1 WHERE messageId = :messageId")
    void recallMessage(String messageId);

    @Query("SELECT * FROM messages WHERE isSentToCloud = 0")
    List<Message> getUnsyncedMessages();

    @Query("UPDATE messages SET isSentToCloud = 1 WHERE messageId = :messageId")
    void markMessageAsSynced(String messageId);

    @Query("DELETE FROM messages WHERE messageId = :messageId")
    void deleteMessageById(String messageId);

    @Query("SELECT * FROM messages WHERE isAiMessage = 1 AND (senderId = :userId OR receiverId = :userId) ORDER BY timestamp ASC")
    List<Message> getAiMessages(String userId);
}