package org.nyxcode.wurm.discordrelay;

import com.wurmonline.server.Message;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.villages.PvPAlliance;
import com.wurmonline.server.villages.Village;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import mod.sin.lib.Util;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.nyxcode.wurm.discordrelay.oauth.OAuthService;

import javax.security.auth.login.LoginException;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Properties;
import java.util.logging.Logger;


/**
 * Created by whisper2shade on 22.04.2017.
 */
public class DiscordRelay implements WurmServerMod, PreInitable, Configurable, ChannelMessageListener, PlayerMessageListener, ServerStartedListener, PlayerLoginListener {

    public static final Logger logger = Logger.getLogger("DiscordRelay");

    private static DiscordRelay instance;

    private JDA jda;
    private RelayConfig config;
    private ChatManager cm;
    private OAuthService oauthService;
    private UserManager userManager;


//    private final ResourceBundle resourceBundle = ResourceBundle.getBundle("messages", Locale.ENGLISH);

    public void configure(Properties properties) {
        config = new RelayConfig(properties);
    }

    private static DiscordRelay getInstance() {
        return instance;
    }

    public static void sendRumour(Creature creature) {
        getInstance().chatManager().discordSender().sendRumour(creature);
    }

    public void preInit() {
        instance = this;

        if (config.rumours()) {
            initRumours();
        }

    }

    private void initRumours() {
        ClassPool classPool = HookManager.getInstance().getClassPool();
        Class<DiscordRelay> thisClass = DiscordRelay.class;

        // - Send rumour messages to discord - //
        try {
            CtClass ctCreature = classPool.get("com.wurmonline.server.creatures.Creature");
            CtClass[] params1 = {
                    CtClass.intType,
                    CtClass.booleanType,
                    CtClass.floatType,
                    CtClass.floatType,
                    CtClass.floatType,
                    CtClass.intType,
                    classPool.get("java.lang.String"),
                    CtClass.byteType,
                    CtClass.byteType,
                    CtClass.byteType,
                    CtClass.booleanType,
                    CtClass.byteType,
                    CtClass.intType
            };
            String desc1 = Descriptor.ofMethod(ctCreature, params1);
            Util.setReason("Send rumour messages to Discord.");
            String replace = "$proceed($$);"
                    + DiscordRelay.class.getName() + ".sendRumour(toReturn);";
            Util.instrumentDescribed(thisClass, ctCreature, "doNew", desc1, "broadCastSafe", replace);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initOAuth(UserManager userManager) {
        oauthService = new OAuthService(config, userManager);
        oauthService.init();
    }

    public void onServerStarted() {
        initJDA();
        try {
            userManager = new UserManager();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        cm = new ChatManager(this, jda, config, userManager);
        initOAuth(userManager);
        cm.init();
    }

    private void initJDA() {
        try {
            EnumSet<GatewayIntent> intents = EnumSet.allOf(GatewayIntent.class);
            this.jda = JDABuilder.createDefault(config.botToken(), intents).build().awaitReady();
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MessagePolicy onKingdomMessage(Message message) {
        return cm.wurmListener().onKingdomMessage(message);
    }

    @Override
    public MessagePolicy onVillageMessage(Village village, Message message) {
        return cm.wurmListener().onVillageMessage(village, message);
    }

    @Override
    public MessagePolicy onAllianceMessage(PvPAlliance alliance, Message message) {
        return cm.wurmListener().onAllianceMessage(alliance, message);
    }

    @Override
    public MessagePolicy onPlayerMessage(Communicator communicator, String message, String title) {
        return cm.wurmListener().onPlayerMessage(communicator, message, title);
    }

    @Override
    public boolean onPlayerMessage(Communicator communicator, String message) {
        return cm.wurmListener().onPlayerMessage(communicator, message);
    }

    public ChatManager discordManager() {
        return cm;
    }

    @Override
    public void onPlayerLogin(Player player) {
        cm.fireLoginEvent(player);
    }

    @Override
    public void onPlayerLogout(Player player) {
        // empty
    }

    private ChatManager chatManager() {
        return cm;
    }


//    public ResourceBundle resourceBundle() {
//        return resourceBundle;
//    }
}
