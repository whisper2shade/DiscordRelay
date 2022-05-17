package org.nyxcode.wurm.discordrelay;

import com.wurmonline.server.players.Player;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

public class ChatManager {

    private final Guild guild;
    private final DiscordRelay relay;
    private final JDA jda;
    private final UserManager userManager;
    private final RelayConfig config;
    private DiscordListener discordListener;
    private DiscordSender discordSender;
    private WurmListener wurmListener;
    private WurmSender wurmSender;
    private DiscordInitializer discordInitializer;


    public ChatManager(DiscordRelay relay, JDA jda, RelayConfig config, UserManager userManager) {
        this.relay = relay;
        this.jda = jda;
        this.userManager = userManager;
        guild = jda.getGuildById(config.serverId());
        this.config = config;

    }


    public void init() {
        discordInitializer = new DiscordInitializer(this);
        discordInitializer.init();
        discordListener = new DiscordListener(this);
        jda.addEventListener(discordListener);
        discordSender = new DiscordSender(this);
        wurmListener = new WurmListener(this);
        wurmSender = new WurmSender(this);
    }


    public UserManager userManager() {
        return userManager;
    }

    public Guild guild() {
        return guild;
    }

//    public ResourceBundle bundle() {
//        return relay.resourceBundle();
//    }

    public RelayConfig config() {
        return config;
    }

    public DiscordListener discordListener() {
        return discordListener;
    }

    public DiscordSender discordSender() {
        return discordSender;
    }

    public WurmListener wurmListener() {
        return wurmListener;
    }

    public WurmSender wurmSender() {
        return wurmSender;
    }

    public void fireLoginEvent(Player player) {
        discordInitializer.fireLoginEvent(player);
    }
}
