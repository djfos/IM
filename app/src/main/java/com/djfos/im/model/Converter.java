package com.djfos.im.model;

import android.net.Uri;

import androidx.room.TypeConverter;

public class Converter {
    @TypeConverter
    public static final String uriToString(Uri uri) {
        String result = uri.getPath();
        if (result == null)
            throw new NullPointerException();

        return uri.getPath();
    }

    @TypeConverter
    public static final Uri stringToUri(String uri) {
        return Uri.parse(uri);
    }


}
