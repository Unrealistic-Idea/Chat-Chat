package com.chatchat.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey
    @NonNull
    private String travelerId;
    private String username;
    private String avatarUrl;
    private String avatarAccessory;
    private String encryptedPassword;
    private String token;
    private boolean isOnline;
    private long lastSeen;
    private String publicKey;

    // Constructors
    public User() {}

    @Ignore
    public User(String travelerId, String username) {
        this.travelerId = travelerId;
        this.username = username;
        this.isOnline = false;
        this.lastSeen = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getTravelerId() { return travelerId; }
    public void setTravelerId(String travelerId) { this.travelerId = travelerId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getAvatarAccessory() { return avatarAccessory; }
    public void setAvatarAccessory(String avatarAccessory) { this.avatarAccessory = avatarAccessory; }

    public String getEncryptedPassword() { return encryptedPassword; }
    public void setEncryptedPassword(String encryptedPassword) { this.encryptedPassword = encryptedPassword; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

    public long getLastSeen() { return lastSeen; }
    public void setLastSeen(long lastSeen) { this.lastSeen = lastSeen; }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
}