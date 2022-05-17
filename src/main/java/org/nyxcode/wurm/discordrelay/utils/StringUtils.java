package org.nyxcode.wurm.discordrelay.utils;

public class StringUtils {
    private StringUtils() {
        // utility class
    }

    public static boolean isEmptyTrimmed(String string) {
        return string == null || string.trim().length() == 0;
    }

}
