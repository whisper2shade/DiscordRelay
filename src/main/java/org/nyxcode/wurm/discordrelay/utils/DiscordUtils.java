package org.nyxcode.wurm.discordrelay.utils;

public class DiscordUtils {
    private DiscordUtils() {
        // utility class
    }

    public static String discordifyName(String name, boolean useUnderscore) {
        name = name.toLowerCase();
        if (useUnderscore) {
            return name.replace(" ", "_");
        } else {
            return name.replace(" ", "");
        }
    }
}
