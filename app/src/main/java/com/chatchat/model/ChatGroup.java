package com.chatchat.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.util.List;

@Entity(tableName = "chat_groups")
public class ChatGroup {
    @PrimaryKey
    @NonNull
    private String groupId;
    private String groupName;
    private String groupAvatarUrl;
    private String createdBy;
    private long createdAt;
    private String description;
    private List<String> memberIds;
    private List<String> adminIds;
    private String lastMessageId;
    private long lastMessageTime;
    private int unreadCount;

    // Constructors
    public ChatGroup() {}

    @Ignore
    public ChatGroup(String groupId, String groupName, String createdBy) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.createdBy = createdBy;
        this.createdAt = System.currentTimeMillis();
        this.lastMessageTime = System.currentTimeMillis();
        this.unreadCount = 0;
    }

    // Getters and Setters
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getGroupAvatarUrl() { return groupAvatarUrl; }
    public void setGroupAvatarUrl(String groupAvatarUrl) { this.groupAvatarUrl = groupAvatarUrl; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getMemberIds() { return memberIds; }
    public void setMemberIds(List<String> memberIds) { this.memberIds = memberIds; }

    public List<String> getAdminIds() { return adminIds; }
    public void setAdminIds(List<String> adminIds) { this.adminIds = adminIds; }

    public String getLastMessageId() { return lastMessageId; }
    public void setLastMessageId(String lastMessageId) { this.lastMessageId = lastMessageId; }

    public long getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(long lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}