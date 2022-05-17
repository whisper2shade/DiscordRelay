package org.nyxcode.wurm.discordrelay;

import com.wurmonline.server.Message;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.villages.PvPAlliance;
import com.wurmonline.server.villages.Village;
import net.dv8tion.jda.api.MessageBuilder;
import org.gotti.wurmunlimited.modloader.interfaces.MessagePolicy;
import org.jetbrains.annotations.NotNull;
import org.nyxcode.wurm.discordrelay.utils.DiscordUtils;

public class WurmListener {

    private final ChatManager cm;

    public WurmListener(ChatManager chatManager) {
        cm = chatManager;
    }


    public MessagePolicy onKingdomMessage(Message message) {
        if (message.getWindow().equals("Trade")) {
            return onTradeMessage(message);
        }
        byte kingdomId = message.getSender().getKingdomId();
        Kingdom kingdom = Kingdoms.getKingdom(kingdomId);
        String kingdomName = DiscordUtils.discordifyName(kingdom.getName(), cm.config().useUnderscore());
        return onMessage(message, kingdomName);
    }

    private MessagePolicy onTradeMessage(Message message) {
        return onMessage(message, "trade");
    }

    public MessagePolicy onVillageMessage(Village village, Message message) {
        if (message.getSender() == null) {
            return MessagePolicy.PASS;
        }
        return onMessage(message, village.getName());
    }

    public MessagePolicy onAllianceMessage(PvPAlliance alliance, Message message) {
        return onMessage(message, alliance.getName());
    }

    @NotNull
    private MessagePolicy onMessage(Message message, String name) {
        String allianceName = DiscordUtils.discordifyName(name, cm.config().useUnderscore());
        MessageBuilder builder = new MessageBuilder();

        builder.append(message.getMessage());
        cm.guild().getTextChannelsByName(allianceName, true).get(0).sendMessage(builder.build()).queue();
        return MessagePolicy.PASS;
    }

    public MessagePolicy onPlayerMessage(Communicator communicator, String message, String title) {
        return MessagePolicy.PASS;
    }

    public boolean onPlayerMessage(Communicator communicator, String message) {
        return false;
    }
}
