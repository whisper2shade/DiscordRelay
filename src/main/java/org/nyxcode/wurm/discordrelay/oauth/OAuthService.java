package org.nyxcode.wurm.discordrelay.oauth;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.nyxcode.wurm.discordrelay.RelayConfig;
import org.nyxcode.wurm.discordrelay.UserManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

public class OAuthService extends ListenerAdapter {

    private final RelayConfig config;
    private final UserManager userManager;

    public OAuthService(RelayConfig config, UserManager userManager) {
        this.config = config;
        this.userManager = userManager;
    }


    public void init() {
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpContext context = server.createContext("/oauth");
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
