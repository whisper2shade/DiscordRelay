package org.nyxcode.wurm.discordrelay.utils;

public class SteamIdConvertor {

    private SteamIdConvertor() {
        // utility class
    }

    public static long to32bit(long id64) {
        return id64 << 32;
    }
}
