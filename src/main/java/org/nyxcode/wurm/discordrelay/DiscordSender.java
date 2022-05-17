package org.nyxcode.wurm.discordrelay;

import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import net.dv8tion.jda.api.MessageBuilder;
import org.nyxcode.wurm.discordrelay.constants.Names;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DiscordSender {

    private static final DateFormat df = new SimpleDateFormat("HH:mm:ss");
    private final ChatManager cm;

    public DiscordSender(ChatManager chatManager) {
        cm = chatManager;
    }

    public void sendRumour(Creature creature) {
        sendToDiscord(Names.RUMOURS, "Rumours of " + creature.getName() + " are starting to spread.", true);
    }

    public void sendToDiscord(String channel, String message, boolean includeMap) {
        MessageBuilder builder = new MessageBuilder();
        message = "[" + df.format(new Date(System.currentTimeMillis())) + "] " + message; // Add timestamp
        if (includeMap) {
            message = message + " (" + Servers.localServer.mapname + ")";
        }

        builder.append(message);
        try {
            cm.guild().getTextChannelsByName(channel, true).get(0).sendMessage(builder.build()).queue();
        } catch (Exception e) {
            e.printStackTrace();
            DiscordRelay.logger.info("Discord Relay failure: #" + channel + " - " + message);
        }
    }
}
