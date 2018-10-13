package org.nyxcode.wurm.discordrelay.oauth;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.nyxcode.wurm.discordrelay.DiscordManager;
import org.nyxcode.wurm.discordrelay.RelayConfig;
import org.nyxcode.wurm.discordrelay.UserManager;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.sql.SQLException;

public class OAuthService extends ListenerAdapter {

    private final RelayConfig config;
    private UserManager userManager;

    public OAuthService(RelayConfig config) throws IOException {
        this.config = config;
    }


    public void init() {
        try {
            userManager = new UserManager();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JDA jda = null;
        try {
            jda = new JDABuilder(AccountType.BOT).setToken(config.botToken()).addEventListener(this).buildBlocking();
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new DiscordManager(jda, config).init();


        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpContext context = server.createContext("/");
        context.setHandler(this::handleRequest);
        server.start();

    }

    private void handleRequest(HttpExchange exchange) throws IOException {
        URI requestURI = exchange.getRequestURI();

        String query = requestURI.getQuery();
        String[] split = query.split("&");
        String token = null;
        for (String s : split) {
            if (s.startsWith("code")) {
                token = s.substring("code=".length());
            }
        }
        if (token != null) {
            String message = new TokenProcessor(config, userManager).process(token);
            exchange.sendResponseHeaders(200, message.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(message.getBytes());
            os.close();
        } else {
            exchange.sendResponseHeaders(400, 0);
        }
    }


}
