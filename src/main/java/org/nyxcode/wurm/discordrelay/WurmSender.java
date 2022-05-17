package org.nyxcode.wurm.discordrelay;

import com.wurmonline.server.Message;
import com.wurmonline.server.Server;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import org.nyxcode.wurm.discordrelay.utils.DiscordUtils;

public class WurmSender {

    private final ChatManager cm;

    public WurmSender(ChatManager chatManager) {
        cm = chatManager;
    }

    public void sendToGlobalKingdomChat(final String channel, final String message, final String author) {

        Kingdom[] kingdoms = Kingdoms.getAllKingdoms();
        for (Kingdom kingdom : kingdoms) {
            if (DiscordUtils.discordifyName(kingdom.getName(), cm.config().useUnderscore()).equals(channel.toLowerCase()) && kingdom.existsHere()) {
                byte kingdomId = kingdom.getId();

                final Message mess = new Message(null, Message.GLOBKINGDOM, Kingdoms.getChatNameFor(kingdomId), discordifyMessage(message, author));
                mess.setSenderKingdom(kingdomId);
                if (message.trim().length() > 1) {
                    Server.getInstance().addMessage(mess);
//                    final WcKingdomChat wc = new WcKingdomChat(WurmId.getNextWCCommandId(),
//                            wurmId, "temp2", message, false, kingdomId,
//                            -1,
//                            -1,
//                            -1);
//                    if (Servers.localServer.LOGINSERVER)
//                        wc.sendFromLoginServer();
//                    else
//                        wc.sendToLoginServer();
                }
            }
        }
    }

    public void sendVillageMessage(final String villageName, final String message, final String author) {
        Village[] villages = Villages.getVillages();

        for (Village village : villages) {
            if (DiscordUtils.discordifyName(village.getName(), cm.config().useUnderscore()).equals(villageName.toLowerCase())) {
                village.broadCastMessage(new Message(null,
                        Message.VILLAGE, "Village", discordifyMessage(message, author)));
            }
        }
    }

    private String discordifyMessage(String message, String author) {
        return "(Discord) <" + author + "> " + message;
    }

    public void sendTradeMessage(String message, final String author) {
        String window = "Trade";
        final Message mess = new Message(null, Message.TRADE, window, discordifyMessage(message, author));
        if (message.trim().length() > 1) {
            Server.getInstance().addMessage(mess);
        }
    }
}
