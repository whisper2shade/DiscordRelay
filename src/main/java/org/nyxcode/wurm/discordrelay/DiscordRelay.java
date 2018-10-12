package org.nyxcode.wurm.discordrelay;

import com.wurmonline.server.Message;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Communicator;
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
import org.dmfs.httpessentials.client.HttpRequestExecutor;
import org.dmfs.httpessentials.httpurlconnection.HttpUrlConnectionExecutor;
import org.dmfs.oauth2.client.*;
import org.dmfs.rfc3986.encoding.Precoded;
import org.dmfs.rfc3986.uris.LazyUri;
import org.dmfs.rfc5545.Duration;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.TkFork;
import org.takes.http.Exit;
import org.takes.http.FtBasic;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URI;
import java.util.*;


/**
 * Created by whisper2shade on 22.04.2017.
 */
public class DiscordRelay extends ListenerAdapter implements WurmServerMod, PreInitable, Configurable, ChannelMessageListener, PlayerMessageListener, ServerStartedListener {


    private JDA jda;
    private RelayConfig config;
    private String botToken;
    private String serverName;
    private String wurmBotName;
    private boolean useUnderscore;
    private Guild guild;
    private OAuth2Client oAuth2Client;
    private HttpRequestExecutor connectionExecutor;


    public void configure(Properties properties) {
        config = new RelayConfig(properties);


        botToken = properties.getProperty("botToken");
        serverName = properties.getProperty("discordServerName");
        wurmBotName = properties.getProperty("wurmBotName");
        useUnderscore = Boolean.parseBoolean(properties.getProperty("useUnderscore", "false"));
    }

    public void preInit() {
        initJDA();
        initOAuth();

        try {
            new FtBasic(
                    new TkFork(new FkRegex("/oauth", "It's done")), 8080
            ).start(Exit.NEVER);
        } catch (IOException e) {
            e.printStackTrace();
        }

        DiscordManager discordManager = new DiscordManager(jda, config);
    }

    private void initOAuth() {

    }

    public void onServerStarted() {
        createKingdomChannels();
        createVillageChannels();
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


    public MessagePolicy onPlayerMessage(Communicator communicator, String message, String title) {
        return null;
    }

    public boolean onPlayerMessage(Communicator communicator, String message) {
        return false;
    }
}
