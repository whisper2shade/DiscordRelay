package org.nyxcode.wurm.discordrelay;

import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.managers.GuildController;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class DiscordManager {

    ResourceBundle resourceBundle = ResourceBundle.getBundle("messages", Locale.ENGLISH);
    private boolean useUnderscore;

    public DiscordManager(JDA jda, String server, boolean useUnderscore) {
        this.useUnderscore = useUnderscore;

        guild = jda.getGuildsByName(serverName, true).get(0);
    }

    private void createKingdomChannels() {
        List<Kingdom> kingdoms = Arrays.asList(Kingdoms.getAllKingdoms());
        Category category = createOrGetCategory(resourceBundle.getString("core.category.kingdoms"));
        for (Kingdom kingdom : kingdoms) {
            ensureChannel(category, kingdom.getName());
        }
    }

    private void ensureChannel(Category category, String entityName) {
        String channelName = discordifyName(entityName);
        List<TextChannel> channels = guild.getTextChannelsByName(channelName, true);
        if (channels.isEmpty()) {
            category.createTextChannel(channelName).complete();
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
            ensureChannel(category, village.getName());
        }
    }

    String discordifyName(String name) {
        name = name.toLowerCase();
        if (useUnderscore) {
            return name.replace(" ", "_");
        } else {
            return name.replace(" ", "");
        }
    }
}
