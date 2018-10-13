package org.nyxcode.wurm.discordrelay;

import java.sql.SQLException;
import java.util.Optional;

public class UserManager {

    private UserDb db;

    public UserManager() throws SQLException {
        this.db = new UserDb();
        db.init();
    }

    public void addUser(long discordId, long steamId) {
        Optional<Long> player = null;
        try {
            player = UserDb.getPlayerForSteamId32(steamId);
            if (player.isPresent()) {
                UserDb.updateDiscordIdForPlayer(discordId, player.get());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
