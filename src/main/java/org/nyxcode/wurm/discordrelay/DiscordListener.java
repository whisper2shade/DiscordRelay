package org.nyxcode.wurm.discordrelay;

import com.wurmonline.server.Message;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.webinterface.WcKingdomChat;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.nyxcode.wurm.discordrelay.utils.DiscordUtils;

import java.util.Arrays;
import java.util.List;

public class DiscordListener extends ListenerAdapter {

    private final Guild guild;
    private final UserManager manager;
    private final RelayConfig config;

    public DiscordListener(Guild guild, UserManager manager, RelayConfig config) {
        this.guild = guild;
        this.manager = manager;
        this.config = config;
    }


    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        super.onGuildMessageReceived(event);
    }


    public void sendToGlobalKingdomChat(final String channel, final String message) {

        List<Kingdom> kingdoms = Arrays.asList(Kingdoms.getAllKingdoms());

        byte kingdomId = -1;

        for (Kingdom kingdom : kingdoms) {
            if (DiscordUtils.discordifyName(kingdom.getName(), config.useUnderscore()).equals(channel.toLowerCase())) {
                kingdomId = kingdom.getId();
            }
        }
        if (kingdomId != -1) {
            long wurmId = -10;

            final Message mess = new Message(null, Message.GLOBKINGDOM, Kingdoms.getChatNameFor(kingdomId), "<" + "temp" + "> "
                    + message);
            mess.setSenderKingdom(kingdomId);
            if (message.trim().length() > 1) {
                Server.getInstance().addMessage(mess);
                final WcKingdomChat wc = new WcKingdomChat(WurmId.getNextWCCommandId(),
                        wurmId, "temp2", message, false, kingdomId,
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


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        super.onMessageReceived(event);
        if (event.isFromType(ChannelType.TEXT) && !event.getAuthor().isBot()) {
            String name = event.getTextChannel().getName();

            sendToGlobalKingdomChat(name, "<" + event.getAuthor().getName() + "> " + event.getMessage().getContentStripped());
        }
    }
}
