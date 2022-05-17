package org.nyxcode.wurm.discordrelay;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.LoginHandler;
import org.nyxcode.wurm.common.ModDb;
import org.nyxcode.wurm.discordrelay.oauth.entity.WurmUser;
import org.nyxcode.wurm.discordrelay.utils.SteamIdConvertor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDb {
    private static Connection connection;

    public void init() throws SQLException {
        ModDb.init();
        connection = ModDb.getConnection();
        int version = ModDb.getSchemaVer("DISCORDRELAY");
        if (version < 1) {
            DiscordRelay.logger.info("Creating database table");

            ModDb.execSQL("CREATE TABLE DISCORDRELAY_PLAYERS (" +
//                    "WurmID LONG NOT NULL," +
                    "SteamID LONG NOT NULL," +
                    "DiscordID LONG NOT NULL," +
                    "PRIMARY KEY(SteamID)" +
                    ")");

            ModDb.setSchemaVer("DISCORDRELAY", 1);
        }
    }

    public Optional<Long> getDiscordIdForPlayer(long steamId) throws SQLException {
        try (PreparedStatement st = connection.prepareStatement("SELECT DiscordID from DISCORDRELAY_PLAYERS WHERE SteamID = ?")) {
            st.setLong(1, steamId);
            try (ResultSet res = st.executeQuery()) {
                if (res.next())
                    return Optional.of(res.getLong(1));
                else
                    return Optional.empty();
            }
        }
    }

    public Optional<Long> getSteamIdForDiscordId(long discordId) throws SQLException {
        try (PreparedStatement st = connection.prepareStatement("SELECT SteamID FROM DISCORDRELAY_PLAYERS WHERE DiscordID = ?")) {
            st.setLong(1, discordId);
            try (ResultSet res = st.executeQuery()) {
                if (res.next())
                    return Optional.of(res.getLong(1));
                else
                    return Optional.empty();
            }
        }
    }

    public void updateDiscordIdForPlayer(long discordId, long steamId) throws SQLException {
        Optional<Long> current = getDiscordIdForPlayer(steamId);
        if (current.isPresent() && current.get() != discordId) {
            DiscordRelay.logger.warning(String.format("Player %d changed discord ID from %d to %d", discordId, current.get(), steamId));
        }
        try (PreparedStatement st = connection.prepareStatement("INSERT OR REPLACE INTO DISCORDRELAY_PLAYERS (SteamId, DiscordId) VALUES (?,?)")) {
            st.setLong(1, steamId);
            st.setLong(2, discordId);
            st.execute();
        }
    }

    public Optional<Long> getPlayersForSteamId(long steamId) throws SQLException {
        Connection dbcon = DbConnector.getPlayerDbCon();
        try {
            try (PreparedStatement ps = dbcon.prepareStatement("SELECT PLAYER_ID FROM STEAM_IDS WHERE STEAM_ID=?")) {
                ps.setLong(1, steamId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(rs.getLong(1));
                    } else {
                        return Optional.empty();
                    }
                }
            }
        } finally {
            DbConnector.returnConnection(dbcon);
        }
    }

    public List<WurmUser> getAllUsers() throws SQLException {
        Connection dbcon = DbConnector.getLoginDbCon();
        List<WurmUser> users = new ArrayList<>();
        try {
            try (PreparedStatement ps = dbcon.prepareStatement("SELECT * FROM DISCORDRELAY_PLAYERS")) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        long discordId = rs.getLong(1);
                        long wurmId = rs.getLong(2);
                        users.add(new WurmUser(discordId, wurmId));
                    }

                }
            }
        } finally {
            DbConnector.returnConnection(dbcon);
        }
        return users;
    }

    public List<Long> getPlayersForSteamId32(long steamId) throws SQLException {
        Connection dbcon = DbConnector.getPlayerDbCon();
        try {
            try (PreparedStatement ps = dbcon.prepareStatement("SELECT PLAYER_ID FROM STEAM_IDS WHERE STEAM_ID << 32 = ?")) {
                ps.setLong(1, SteamIdConvertor.to32bit(steamId));
                try (ResultSet rs = ps.executeQuery()) {
                    List<Long> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(rs.getLong(1));
                    }
                    return result;
                }
            }
        } finally {
            DbConnector.returnConnection(dbcon);
        }
    }

    public Optional<Long> getPlayerId(String name) throws SQLException {
        Connection dbcon = DbConnector.getPlayerDbCon();
        try {
            try (PreparedStatement ps = dbcon.prepareStatement("SELECT WURMID FROM PLAYERS WHERE NAME=?")) {
                ps.setString(1, LoginHandler.raiseFirstLetter(name));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(rs.getLong(1));
                    } else {
                        return Optional.empty();
                    }
                }
            }
        } finally {
            DbConnector.returnConnection(dbcon);
        }
    }

    public int getKingdomPlayerCount(byte kingdomId) throws SQLException {
        Connection dbcon = DbConnector.getPlayerDbCon();
        try {
            try (PreparedStatement ps = dbcon.prepareStatement("SELECT COUNT(DISTINCT(STEAM_ID)) FROM PLAYERS p INNER JOIN STEAM_IDS s ON p.WURMID = s.PLAYER_ID WHERE p.KINGDOM = ?")) {
                ps.setByte(1, kingdomId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    } else {
                        return 0;
                    }
                }
            }
        } finally {
            DbConnector.returnConnection(dbcon);
        }
    }
}
