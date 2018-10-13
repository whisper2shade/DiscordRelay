package org.nyxcode.wurm.discordrelay.oauth.entity;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;

@Document(collection = "wurmusers", schemaVersion = "1.0")
public class WurmUser {
    @Id
    private long discordId;
    private long steamId;

    public WurmUser(long discordId, long steamId) {

        this.discordId = discordId;
        this.steamId = steamId;
    }

    public long getDiscordId() {
        return discordId;
    }

    public void setDiscordId(long discordId) {
        this.discordId = discordId;
    }

    public long getSteamId() {
        return steamId;
    }

    public void setSteamId(long steamId) {
        this.steamId = steamId;
    }
}
