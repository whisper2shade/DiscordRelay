package org.nyxcode.wurm.discordrelay.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.message.BasicNameValuePair;
import org.nyxcode.wurm.discordrelay.RelayConfig;
import org.nyxcode.wurm.discordrelay.UserManager;
import org.nyxcode.wurm.discordrelay.oauth.dto.Connection;
import org.nyxcode.wurm.discordrelay.oauth.dto.TokenResponse;
import org.nyxcode.wurm.discordrelay.oauth.dto.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TokenProcessor {

    private final RelayConfig config;
    private final UserManager manager;

    public TokenProcessor(RelayConfig config, UserManager manager) {
        this.config = config;
        this.manager = manager;
    }


    public String process(String token) {
        List<NameValuePair> params = new ArrayList<>();

        params.add(new BasicNameValuePair("client_id", config.clientId()));
        params.add(new BasicNameValuePair("client_secret", config.clientSecret()));
        params.add(new BasicNameValuePair("code", token));
        params.add(new BasicNameValuePair("redirect_uri", config.redirectUrl()));
        params.add(new BasicNameValuePair("grant_type", "authorization_code"));
        params.add(new BasicNameValuePair("scope", "identify connections"));

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Content content = Request.Post("https://discordapp.com/api/oauth2/token").bodyForm(params).execute().returnContent();
            TokenResponse tokenResponse = objectMapper.readValue(content.asString(), TokenResponse.class);

            content = Request.Get("https://discordapp.com/api/users/@me").addHeader("Authorization", "Bearer " + tokenResponse.accessToken()).execute().returnContent();
            User user = objectMapper.readValue(content.asString(), User.class);

            content = Request.Get("https://discordapp.com/api/users/@me/connections").addHeader("Authorization", "Bearer " + tokenResponse.accessToken()).execute().returnContent();
            List<Connection> connections = objectMapper.readValue(content.asString(), new TypeReference<List<Connection>>() {
            });

            for (Connection connection : connections) {
                if ("steam".equals(connection.type())) {
                    long steamId = Long.parseLong(connection.id());
                    long discordId = user.id();
                    manager.addUser(discordId, steamId);
                }

            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "NOT OK (JSON)";
        } catch (IOException e) {
            e.printStackTrace();
            return "NOT OK (IO)";
        }

        return "OK";
    }


}
