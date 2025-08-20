package com.chatchat.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages")
public class Message {
    @PrimaryKey
    private String messageId;
    private String senderId;
    private String receiverId; // null for group messages
    private String groupId; // null for direct messages
    private String content;
    private MessageType type;
    private long timestamp;
    private boolean isRead;
    private boolean isRecalled;
    private String mediaUrl;
    private boolean isSentToCloud;
    private boolean isAiMessage;

    public enum MessageType {
        TEXT, IMAGE, VOICE, EMOJI, MARKDOWN, CHART, CONTACT_CARD
    }

    // Constructors
    public Message() {}

    public Message(String messageId, String senderId, String content, MessageType type) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.content = content;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
        this.isRecalled = false;
        this.isSentToCloud = false;
        this.isAiMessage = false;
    }

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public boolean isRecalled() { return isRecalled; }
    public void setRecalled(boolean recalled) { this.isRecalled = recalled; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public boolean isSentToCloud() { return isSentToCloud; }
    public void setSentToCloud(boolean sentToCloud) { isSentToCloud = sentToCloud; }

    public boolean isAiMessage() { return isAiMessage; }
    public void setAiMessage(boolean aiMessage) { isAiMessage = aiMessage; }
}