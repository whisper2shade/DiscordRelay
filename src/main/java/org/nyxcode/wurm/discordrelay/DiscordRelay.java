package org.nyxcode.wurm.discordrelay;

import com.wurmonline.server.Message;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.villages.PvPAlliance;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.webinterface.WcKingdomChat;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;
import org.gotti.wurmunlimited.modloader.interfaces.*;

import javax.security.auth.login.LoginException;
import java.util.*;


/**
 * Created by whisper2shade on 22.04.2017.
 */
public class DiscordRelay extends ListenerAdapter implements WurmServerMod, PreInitable, Configurable, ChannelMessageListener, ServerStartedListener {

    ResourceBundle resourceBundle = ResourceBundle.getBundle("messages", Locale.ENGLISH);

    private JDA jda;
    private String botToken;
    private String serverName;
    private String wurmBotName;
    private boolean useUnderscore;
    private Guild guild;


    public void preInit() {
        initJDA();
        guild = jda.getGuildsByName(serverName, true).get(0);
    }

    public void onServerStarted() {
        createKingdomChannels();
        createVillageChannels();
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

    private void initJDA() {
        try {
            jda = new JDABuilder(AccountType.BOT).setToken(botToken).addEventListener(this).buildBlocking();
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (RateLimitedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void configure(Properties properties) {
        botToken = properties.getProperty("botToken");
        serverName = properties.getProperty("discordServerName");
        wurmBotName = properties.getProperty("wurmBotName");
        useUnderscore = Boolean.parseBoolean(properties.getProperty("useUnderscore", "false"));
    }

    public MessagePolicy onKingdomMessage(Message message) {
        byte kingdomId = message.getSender().getKingdomId();
        Kingdom kingdom = Kingdoms.getKingdom(kingdomId);
        String kingdomName = discordifyName(kingdom.getName());
        MessageBuilder builder = new MessageBuilder();

        builder.append(message.getMessage());
        jda.getGuildsByName(serverName, true).get(0).getTextChannelsByName(kingdomName, true).get(0).sendMessage(builder.build()).queue();

        return MessagePolicy.PASS;
    }

    public void sendToGlobalKingdomChat(final String channel, final String message) {

        List<Kingdom> kingdoms = Arrays.asList(Kingdoms.getAllKingdoms());

        byte kingdomId = -1;

        for (Kingdom kingdom : kingdoms) {
            if (discordifyName(kingdom.getName()).equals(channel.toLowerCase())) {
                kingdomId = kingdom.getId();
            }
        }
        if (kingdomId != -1) {
            long wurmId = -10;

            final Message mess = new Message(null, Message.GLOBKINGDOM, Kingdoms.getChatNameFor(kingdomId), "<" + wurmBotName + "> "
                    + message);
            mess.setSenderKingdom(kingdomId);
            if (message.trim().length() > 1) {
                Server.getInstance().addMessage(mess);
                final WcKingdomChat wc = new WcKingdomChat(WurmId.getNextWCCommandId(),
                        wurmId, wurmBotName, message, false, kingdomId,
                        -1,
                        -1,
                        -1);
                if (Servers.localServer.LOGINSERVER)
                    wc.sendFromLoginServer();
                else
                    wc.sendToLoginServer();
            }
        }
    }

    public MessagePolicy onVillageMessage(Village village, Message message) {
        return MessagePolicy.PASS;
    }

    public MessagePolicy onAllianceMessage(PvPAlliance alliance, Message message) {
        return MessagePolicy.PASS;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        super.onMessageReceived(event);
        if (event.isFromType(ChannelType.TEXT) && !event.getAuthor().isBot()) {
            String name = event.getTextChannel().getName();
            sendToGlobalKingdomChat(name, "<" + event.getAuthor().getName() + "> " + event.getMessage().getContent());
        }
    }

    private String discordifyName(String name) {
        name = name.toLowerCase();
        if (useUnderscore) {
            return name.replace(" ", "_");
        } else {
            return name.replace(" ", "");
        }
    }
}
