package com.jennifertestu.coeffapp.converter;

import androidx.room.TypeConverter;

import com.jennifertestu.coeffapp.model.TypeDeNote;

public class TypeDeNoteConverter {

    @TypeConverter
    public static TypeDeNote fromString(String value) {
        return value == null ? null : TypeDeNote.valueOf(value);
    }
    @TypeConverter
    public static String typeToString(TypeDeNote type) {
        return type == null ? null : type.toString();
    }
}
