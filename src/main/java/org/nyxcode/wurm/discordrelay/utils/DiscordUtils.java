package org.nyxcode.wurm.discordrelay.utils;

public class DiscordUtils {
    private DiscordUtils() {
        // utility class
    }

    public static String discordifyName(String name, boolean useUnderscore) {
        name = name.toLowerCase();
        if (useUnderscore) {
            name = name.replace(" ", "_");
        } else {
            name = name.replace(" ", "");
        }
        return name.replaceAll("[^0-9a-zA-Z-_]", "");
    }
}
