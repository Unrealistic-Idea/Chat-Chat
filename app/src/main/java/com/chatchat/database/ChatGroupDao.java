package com.chatchat.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import com.chatchat.model.ChatGroup;
import java.util.List;

@Dao
public interface ChatGroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChatGroup(ChatGroup chatGroup);

    @Update
    void updateChatGroup(ChatGroup chatGroup);

    @Delete
    void deleteChatGroup(ChatGroup chatGroup);

    @Query("SELECT * FROM chat_groups WHERE groupId = :groupId")
    ChatGroup getChatGroupById(String groupId);

    @Query("SELECT * FROM chat_groups ORDER BY lastMessageTime DESC")
    List<ChatGroup> getAllChatGroups();

    @Query("UPDATE chat_groups SET unreadCount = :count WHERE groupId = :groupId")
    void updateUnreadCount(String groupId, int count);

    @Query("UPDATE chat_groups SET lastMessageId = :messageId, lastMessageTime = :time WHERE groupId = :groupId")
    void updateLastMessage(String groupId, String messageId, long time);

    @Query("DELETE FROM chat_groups WHERE groupId = :groupId")
    void deleteChatGroupById(String groupId);
}