package org.nyxcode.wurm.discordrelay;

import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.nyxcode.wurm.discordrelay.constants.Names;

public class DiscordListener extends ListenerAdapter {

    private final ChatManager cm;


    public DiscordListener(ChatManager chatManager) {
        cm = chatManager;
    }


//    @Override
//    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
//        super.onGuildMessageReceived(event);
//    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        super.onMessageReceived(event);
        if (event.isFromType(ChannelType.TEXT) && !event.getAuthor().isBot()) {
            TextChannel textChannel = event.getTextChannel();
            Category category = textChannel.getParentCategory();
            String name = textChannel.getName();
            if (category != null) {
                if (Names.KINGDOMS.equalsIgnoreCase(category.getName())) {
                    cm.wurmSender().sendToGlobalKingdomChat(name, event.getMessage().getContentStripped(), event.getAuthor().getName());
                    return;
                }

                if (Names.VILLAGES.equalsIgnoreCase(category.getName())) {
                    cm.wurmSender().sendVillageMessage(name, event.getMessage().getContentStripped(), event.getAuthor().getName());
                    return;
                }

                if (Names.GENERAL.equalsIgnoreCase(category.getName())) {
                    if (Names.TRADE.equalsIgnoreCase(textChannel.getName())) {
                        cm.wurmSender().sendTradeMessage(event.getMessage().getContentStripped(), event.getAuthor().getName());
                        return;
                    }
                }
            }
        }
    }
}
