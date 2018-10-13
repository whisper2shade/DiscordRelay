package org.nyxcode.wurm.discordrelay;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.LoginHandler;
import org.nyxcode.wurm.common.ModDb;
import org.nyxcode.wurm.discordrelay.oauth.entity.WurmUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDb {
    private static Connection connection;

    public static void init() throws SQLException {
        ModDb.init();
        connection = ModDb.getConnection();
        int version = ModDb.getSchemaVer("DISCORDRELAY");
        if (version < 1) {
            DiscordRelay.logger.info("Creating database table");

            ModDb.execSQL("CREATE TABLE DISCORDRELAY_PLAYERS (" +
                    "DiscordID LONG NOT NULL," +
                    "WurmID LONG NOT NULL," +
                    "PRIMARY KEY(DiscordID)" +
                    ")");

            ModDb.setSchemaVer("DISCORDRELAY", 1);
        }
    }

    public static Optional<Long> getDiscordIdForPlayer(long wurmId) throws SQLException {
        try (PreparedStatement st = connection.prepareStatement("SELECT DiscordID from DISCORDRELAY_PLAYERS WHERE WurmID = ?")) {
            st.setLong(1, wurmId);
            try (ResultSet res = st.executeQuery()) {
                if (res.next())
                    return Optional.of(res.getLong(1));
                else
                    return Optional.empty();
            }
        }
    }

    public static List<Long> getPlayersForDiscordId(long discordId) throws SQLException {
        try (PreparedStatement st = connection.prepareStatement("SELECT WurmID FROM DISCORDRELAY_PLAYERS WHERE DiscordID = ?")) {
            st.setLong(1, discordId);
            List<Long> result = new ArrayList<>();
            try (ResultSet res = st.executeQuery()) {
                while (res.next())
                    result.add(res.getLong(1));
            }
            return result;
        }
    }

    public static void updateDiscordIdForPlayer(long discordId, long wurmId) throws SQLException {
        Optional<Long> current = getDiscordIdForPlayer(wurmId);
        if (current.isPresent() && current.get() != discordId) {
            DiscordRelay.logger.warning(String.format("Player %d changed discord ID from %d to %d", discordId, current.get(), wurmId));
        }
        try (PreparedStatement st = connection.prepareStatement("INSERT OR REPLACE INTO DISCORDRELAY_PLAYERS (WurmId, DiscordId) VALUES (?,?)")) {
            st.setLong(1, wurmId);
            st.setLong(2, discordId);
            st.execute();
        }
    }

    public static Optional<Long> getPlayerForSteamId(long steamId) throws SQLException {
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

    public static List<WurmUser> getAllUsers() throws SQLException {
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

    public static Optional<Long> getPlayerForSteamId32(long steamId) throws SQLException {
        Connection dbcon = DbConnector.getPlayerDbCon();
        try {
            try (PreparedStatement ps = dbcon.prepareStatement("SELECT PLAYER_ID FROM STEAM_IDS WHERE STEAM_ID << 32 = ?")) {
                ps.setLong(1, steamId << 32);
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

    public static Optional<Long> getPlayerId(String name) throws SQLException {
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

}
