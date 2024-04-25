package com.everepl.evereplspringboot.utils;

public class StringUtils{

    public static String truncateText(String text, int maxLength) {
        return text.substring(0, Math.min(text.length(), maxLength));
    }

}
