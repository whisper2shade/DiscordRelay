package org.nyxcode.wurm.discordrelay;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.Village;
import org.nyxcode.wurm.discordrelay.utils.SteamIdConvertor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class UserManager {

    private final UserDb db;

    public UserManager() throws SQLException {
        this.db = new UserDb();
        db.init();
    }

    public void addUser(long discordId, long steamId) {
        try {
            db.updateDiscordIdForPlayer(discordId, SteamIdConvertor.to32bit(steamId));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Long> getWurmIds(long discordId) {
        try {
            Optional<Long> steamId = db.getSteamIdForDiscordId(discordId);
            if (!steamId.isPresent()) {
                return Collections.emptyList();
            }
            return db.getPlayersForSteamId32(steamId.get());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public long getWurmId(long discordId, Village village) {
        List<Long> playerIds = getWurmIds(discordId);
        for (Citizen citizen : village.getCitizens()) {
            long citizenId = citizen.getId();
            if (playerIds.contains(citizenId)) {
                return citizenId;
            }
        }
        return -1;
    }

    public List<Long> getWurmId(long discordId, byte kingdomId) {
        List<Long> playerIds = getWurmIds(discordId);
        List<Long> kingdomPlayerIds = new ArrayList<>();
        for (Long playerId : playerIds) {
            try {
                Player player = Players.getInstance().getPlayer(playerId);
                if (player.getKingdomId() == kingdomId) {
                    kingdomPlayerIds.add(playerId);
                }
            } catch (NoSuchPlayerException e) {
                continue;
            }
        }
        return kingdomPlayerIds;
    }

    public Optional<Long> getDiscordIdForPlayer(long wurmId) {
        try {
            return db.getDiscordIdForPlayer(wurmId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public int getUniqueKingdomPlayers(byte kingdomId) {
        try {
            return db.getKingdomPlayerCount(kingdomId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
