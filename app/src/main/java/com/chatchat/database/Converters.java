package com.chatchat.database;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.chatchat.model.Message;
import java.lang.reflect.Type;
import java.util.List;

public class Converters {
    private static Gson gson = new Gson();

    @TypeConverter
    public static String fromStringList(List<String> value) {
        return gson.toJson(value);
    }

    @TypeConverter
    public static List<String> toStringList(String value) {
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String fromMessageType(Message.MessageType type) {
        return type.name();
    }

    @TypeConverter
    public static Message.MessageType toMessageType(String type) {
        return Message.MessageType.valueOf(type);
    }
}