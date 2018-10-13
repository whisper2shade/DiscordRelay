package org.nyxcode.wurm.discordrelay;

import com.wurmonline.server.Message;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.villages.PvPAlliance;
import com.wurmonline.server.villages.Village;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.nyxcode.wurm.discordrelay.oauth.OAuthService;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;


/**
 * Created by whisper2shade on 22.04.2017.
 */
public class DiscordRelay implements WurmServerMod, PreInitable, Configurable, ChannelMessageListener, PlayerMessageListener, ServerStartedListener {

    public static final Logger logger = Logger.getLogger("DiscordRelay");

    private JDA jda;
    private RelayConfig config;
    private Guild guild;
    private DiscordManager discordManager;
    private OAuthService oauthService;
    private UserManager userManager;
    private WurmListener wurmListener;


    public void configure(Properties properties) {
        config = new RelayConfig(properties);
    }

    public void preInit() {
        initJDA();


        discordManager = new DiscordManager(jda, config);
        try {
            userManager = new UserManager();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void initOAuth() {
        try {
            oauthService = new OAuthService(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
        oauthService.init();
    }

    public void onServerStarted() {
        initOAuth();
        discordManager.init();
        jda.addEventListener(new DiscordListener(guild, userManager, config));
        wurmListener = new WurmListener(config, guild);
    }


    private void initJDA() {
        try {
            jda = new JDABuilder(AccountType.BOT).setToken(config.botToken()).addEventListener(this).buildBlocking();
            guild = jda.getGuildById(config.serverId());
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MessagePolicy onKingdomMessage(Message message) {
        return wurmListener.onKingdomMessage(message);
    }

    @Override
    public MessagePolicy onVillageMessage(Village village, Message message) {
        return wurmListener.onVillageMessage(village, message);
    }

    @Override
    public MessagePolicy onAllianceMessage(PvPAlliance alliance, Message message) {
        return wurmListener.onAllianceMessage(alliance, message);
    }

    @Override
    public MessagePolicy onPlayerMessage(Communicator communicator, String message, String title) {
        return wurmListener.onPlayerMessage(communicator, message, title);
    }

    @Override
    public boolean onPlayerMessage(Communicator communicator, String message) {
        return wurmListener.onPlayerMessage(communicator, message);
    }
}
