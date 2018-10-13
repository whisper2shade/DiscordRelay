package org.nyxcode.wurm.discordrelay;

import com.wurmonline.server.Message;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.villages.PvPAlliance;
import com.wurmonline.server.villages.Village;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import org.gotti.wurmunlimited.modloader.interfaces.MessagePolicy;
import org.jetbrains.annotations.NotNull;
import org.nyxcode.wurm.discordrelay.utils.DiscordUtils;

public class WurmListener {

    private final RelayConfig config;
    private final Guild guild;

    public WurmListener(RelayConfig config, Guild guild) {
        this.config = config;
        this.guild = guild;
    }


    public MessagePolicy onKingdomMessage(Message message) {
        byte kingdomId = message.getSender().getKingdomId();
        Kingdom kingdom = Kingdoms.getKingdom(kingdomId);
        String kingdomName = DiscordUtils.discordifyName(kingdom.getName(), config.useUnderscore());
        return onMessage(message, kingdomName);
    }

    public MessagePolicy onVillageMessage(Village village, Message message) {
        return onMessage(message, village.getName());
    }

    public MessagePolicy onAllianceMessage(PvPAlliance alliance, Message message) {
        return onMessage(message, alliance.getName());
    }

    @NotNull
    private MessagePolicy onMessage(Message message, String name) {
        String allianceName = DiscordUtils.discordifyName(name, config.useUnderscore());
        MessageBuilder builder = new MessageBuilder();

        builder.append(message.getMessage());
        guild.getTextChannelsByName(allianceName, true).get(0).sendMessage(builder.build()).queue();
        return MessagePolicy.PASS;
    }

    public MessagePolicy onPlayerMessage(Communicator communicator, String message, String title) {
        return MessagePolicy.PASS;
    }

    public boolean onPlayerMessage(Communicator communicator, String message) {
        return false;
    }
}
