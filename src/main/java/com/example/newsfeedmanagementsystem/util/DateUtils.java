package com.example.newsfeedmanagementsystem.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd MMM yyyy, hh:mm a");

    public static String format(Date date) {
        return date != null ? FORMAT.format(date) : "";
    }
}