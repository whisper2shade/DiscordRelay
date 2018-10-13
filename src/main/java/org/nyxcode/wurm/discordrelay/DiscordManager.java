package org.nyxcode.wurm.discordrelay;

import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.GuildController;
import net.dv8tion.jda.core.requests.restaction.RoleAction;
import org.nyxcode.wurm.discordrelay.utils.DiscordUtils;

import java.sql.SQLException;
import java.util.*;

public class DiscordManager {

    private final Guild guild;
    ResourceBundle resourceBundle = ResourceBundle.getBundle("messages", Locale.ENGLISH);
    private final RelayConfig config;

    public DiscordManager(JDA jda, RelayConfig config) {
        guild = jda.getGuildById(config.serverId());
        this.config = config;
    }

    public void init() {
        createKingdomChannels();
        createVillageChannels();
        try {
            assignRoles();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void assignRoles() throws SQLException {
        List<Kingdom> kingdoms = Arrays.asList(Kingdoms.getAllKingdoms());
        for (Kingdom kingdom : kingdoms) {
            kingdom.loadAllMembers();
            PlayerInfo[] members = kingdom.getAllMembers();
            for (PlayerInfo member : members) {
                long wurmId = member.wurmId;
                Optional<Long> discordId = UserDb.getDiscordIdForPlayer(wurmId);
                if (discordId.isPresent()) {
                    guild.getController().addRolesToMember(guild.getMemberById(discordId.get()), guild.getRolesByName("wurm_" + kingdom.getName(), true)).complete();
                }
            }
        }
        List<Village> villages = Arrays.asList(Villages.getVillages());
        for (Village village : villages) {
            Citizen[] members = village.getCitizens();
            for (Citizen member : members) {
                long wurmId = member.wurmId;
                Optional<Long> discordId = UserDb.getDiscordIdForPlayer(wurmId);
                if (discordId.isPresent()) {
                    guild.getController().addRolesToMember(guild.getMemberById(discordId.get()), guild.getRolesByName("wurm_" + village.getName(), true)).complete();
                }
            }
        }
    }

    private void createKingdomChannels() {
        List<Kingdom> kingdoms = Arrays.asList(Kingdoms.getAllKingdoms());
        Category category = createOrGetCategory(resourceBundle.getString("core.category.kingdoms"));
        for (Kingdom kingdom : kingdoms) {
            Role role = ensureRole(kingdom.getName());
            ensureChannel(category, kingdom.getName(), role);
        }
    }

    private void ensureChannel(Category category, String entityName, Role role) {
        String channelName = DiscordUtils.discordifyName(entityName, config.useUnderscore());
        List<TextChannel> categoryChannels = category.getTextChannels();
        for (TextChannel channel : categoryChannels) {
            if (channel.getName().equalsIgnoreCase(channelName)) {
                return;
            }
        }
        List<TextChannel> channels = guild.getTextChannelsByName(channelName, true);
        if (channels.isEmpty()) {
            Channel channel = category.createTextChannel(channelName).complete();
            channel.createPermissionOverride(guild.getPublicRole()).setDeny(Permission.ALL_PERMISSIONS);
            channel.createPermissionOverride(role).setAllow(Permission.MESSAGE_READ,
                    Permission.MESSAGE_WRITE).complete();
        }
    }

    private Role ensureRole(String entityName) {
        List<Role> roles = guild.getRolesByName("wurm_" + entityName, true);
        if (roles.isEmpty()) {
            RoleAction roleAction = guild.getController().createRole();
            roleAction.setName("wurm_" + entityName);
            return roleAction.complete();
        } else {
            return roles.get(0);
        }
    }

    private Category createOrGetCategory(String name) {
        GuildController controller = guild.getController();

        List<Category> categories = guild.getCategoriesByName(name, true);
        Category category;
        if (categories.isEmpty()) {
            controller.createCategory(name).complete();
            category = guild.getCategoriesByName(name, true).get(0);
        } else {
            category = categories.get(0);
        }
        return category;
    }

    private void createVillageChannels() {
        List<Village> villages = Arrays.asList(Villages.getVillages());
        Category category = createOrGetCategory(resourceBundle.getString("core.category.villages"));
        for (Village village : villages) {
            Role role = ensureRole(village.getName());
            ensureChannel(category, village.getName(), role);
        }
    }

}
