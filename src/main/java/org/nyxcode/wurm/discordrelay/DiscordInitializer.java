package org.nyxcode.wurm.discordrelay;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.RoleAction;
import org.nyxcode.wurm.discordrelay.constants.Names;
import org.nyxcode.wurm.discordrelay.utils.DiscordUtils;
import org.nyxcode.wurm.discordrelay.utils.SteamIdConvertor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DiscordInitializer {

    private final ChatManager cm;

    public DiscordInitializer(ChatManager chatManager) {
        cm = chatManager;
    }

    public void init() {
        createKingdomChannels();
        createVillageChannels();
        createTradeChannel();
        assignRoles();
        if (cm.config().rumours()) {
            initRumours();
        }
    }

    private void createTradeChannel() {
        ensureChannel(createOrGetCategory(Names.GENERAL), Names.TRADE, null);
    }

    public void initRumours() {
        ensureChannel(createOrGetCategory(Names.GENERAL), Names.RUMOURS, null);
    }


    private void assignRoles() {
        Guild guild = cm.guild();
        Kingdom[] kingdoms = Kingdoms.getAllKingdoms();
        for (Kingdom kingdom : kingdoms) {
            kingdom.loadAllMembers();
            PlayerInfo[] members = kingdom.getAllMembers();
            for (PlayerInfo member : members) {
                assignKingdomRole(guild, kingdom, member.getPlayerId());
            }
        }
        Village[] villages = Villages.getVillages();
        for (Village village : villages) {
            Citizen[] members = village.getCitizens();
            for (Citizen member : members) {
                assignVillageRole(guild, village, member.getId());
            }
        }
    }

    private void assignVillageRole(Guild guild, Village village, long wurmId) {
        Optional<Long> discordId = cm.userManager().getDiscordIdForPlayer(wurmId);
        discordId.ifPresent(aLong -> {
            List<Role> roles = guild.getRolesByName("wurm_" + village.getName(), true);
            Member member = guild.getMemberById(aLong);
            roles.forEach(role -> {
                guild.addRoleToMember(member, role).queue();

            });
        });
    }

    private void assignKingdomRole(Guild guild, Kingdom kingdom, long wurmId) {
        Optional<Long> discordId = cm.userManager().getDiscordIdForPlayer(wurmId);
        discordId.ifPresent(aLong -> {
            List<Role> roles = guild.getRolesByName("wurm_" + kingdom.getName(), true);
            Member member = guild.getMemberById(aLong);
            roles.forEach(role -> {
                guild.addRoleToMember(member, role).queue();

            });
        });
    }


    private void assignRoles(Player player) {
        Kingdom kingdom = Kingdoms.getKingdom(player.getKingdomId());
        Village village;
        try {
            village = Villages.getVillage(player.getVillageId());
        } catch (NoSuchVillageException e) {
            village = null;
        }
        assignKingdomRole(cm.guild(), kingdom, player.getWurmId());
        if (village != null) {
            assignVillageRole(cm.guild(), village, player.getWurmId());
        }
    }

    private void createKingdomChannels() {
        Kingdom[] kingdoms = Kingdoms.getAllKingdoms();
        Category category = createOrGetCategory(Names.KINGDOMS);
        for (Kingdom kingdom : kingdoms) {
            if (cm.userManager().getUniqueKingdomPlayers(kingdom.getId()) > 1) {
                Role role = ensureRole(kingdom.getName());
                ensureChannel(category, kingdom.getName(), role);
            }
        }
    }

    private void ensureChannel(Category category, String entityName, Role role) {
        String channelName = DiscordUtils.discordifyName(entityName, cm.config().useUnderscore());
        List<TextChannel> categoryChannels = category.getTextChannels();
        for (TextChannel channel : categoryChannels) {
            if (channel.getName().equalsIgnoreCase(channelName)) {
                return;
            }
        }
        Guild guild = cm.guild();
        List<TextChannel> channels = guild.getTextChannelsByName(channelName, true);
        if (channels.isEmpty()) {
            TextChannel channel = category.createTextChannel(channelName).complete();
            if (role != null) {
                channel.putPermissionOverride(guild.getPublicRole()).setDeny(Permission.ALL_PERMISSIONS).complete();
                Role botRole = getBotRole();
                if (botRole != null) {
                    channel.putPermissionOverride(botRole).setAllow(Permission.ALL_PERMISSIONS).complete();
                }
                channel.putPermissionOverride(role).setAllow(Permission.VIEW_CHANNEL,
                        Permission.MESSAGE_SEND).complete();
            }
        }
    }

    private Role getBotRole() {
        Guild guild = cm.guild();
        String name = guild.getJDA().getSelfUser().getName();
        Optional<Role> role = guild.getSelfMember().getRoles().stream().filter(Role::isManaged).filter(r -> name.equals(r.getName())).findFirst();
        return role.orElse(null);
    }

    private Role ensureRole(String entityName) {
        Guild guild = cm.guild();
        List<Role> roles = guild.getRolesByName("wurm_" + entityName, true);
        if (roles.isEmpty()) {
            RoleAction roleAction = guild.createRole();
            roleAction.setName("wurm_" + entityName);
            return roleAction.complete();
        } else {
            return roles.get(0);
        }
    }

    private Category createOrGetCategory(String name) {
        Guild guild = cm.guild();

        List<Category> categories = guild.getCategoriesByName(name, true);
        Category category;
        if (categories.isEmpty()) {
            guild.createCategory(name).complete();
            category = guild.getCategoriesByName(name, true).get(0);
        } else {
            category = categories.get(0);
        }
        return category;
    }

    private void createVillageChannels() {
        Village[] villages = Villages.getVillages();
        Category category = createOrGetCategory(Names.VILLAGES);
        for (Village village : villages) {
            if (uniqueCitizensCount(village) > cm.config().minCitizens()) {
                Role role = ensureRole(village.getName());
                ensureChannel(category, village.getName(), role);
            }
        }
    }

    private int uniqueCitizensCount(Village village) {
        Citizen[] citizens = village.getCitizens();
        Set<Long> uniqueCitizens = Arrays.stream(citizens)
                .map(c -> c.getId()).map(id -> {
                    try {
                        return Players.getInstance().getPlayer(id);
                    } catch (NoSuchPlayerException e) {
                        return null;
                    }
                }).filter(p -> p != null)
                .map(p -> p.getSteamId().getSteamID64())
                .map(SteamIdConvertor::to32bit)
                .collect(Collectors.toSet());
        return uniqueCitizens.size();
    }

    public void fireLoginEvent(Player player) {
        assignRoles(player);
    }

}
