package com.chatchat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AvatarManager {
    
    private static final String PREFS_NAME = "avatar_prefs";
    private static final String KEY_AVATAR_PATH = "avatar_path";
    private static final String KEY_AVATAR_ACCESSORY = "avatar_accessory";
    
    private Context context;
    private SharedPreferences prefs;

    public AvatarManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean saveAvatar(Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            
            // Resize to standard size
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
            
            // Save to internal storage
            File avatarDir = new File(context.getFilesDir(), "avatars");
            if (!avatarDir.exists()) {
                avatarDir.mkdirs();
            }
            
            File avatarFile = new File(avatarDir, "user_avatar.png");
            FileOutputStream fos = new FileOutputStream(avatarFile);
            resizedBitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
            
            // Save path to preferences
            prefs.edit().putString(KEY_AVATAR_PATH, avatarFile.getAbsolutePath()).apply();
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getAvatarPath() {
        return prefs.getString(KEY_AVATAR_PATH, null);
    }

    public void setAvatarAccessory(String accessory) {
        prefs.edit().putString(KEY_AVATAR_ACCESSORY, accessory).apply();
    }

    public String getAvatarAccessory() {
        return prefs.getString(KEY_AVATAR_ACCESSORY, null);
    }

    public String[] getAvailableAccessories() {
        return new String[]{
            "🎓", "👑", "🎩", "🧢", "⭐", "💎", "🌟", "🎭", "🎪", "🎨"
        };
    }

    public void clearAvatar() {
        String avatarPath = getAvatarPath();
        if (avatarPath != null) {
            File avatarFile = new File(avatarPath);
            if (avatarFile.exists()) {
                avatarFile.delete();
            }
        }
        prefs.edit().remove(KEY_AVATAR_PATH).apply();
    }

    public void clearAccessory() {
        prefs.edit().remove(KEY_AVATAR_ACCESSORY).apply();
    }
}