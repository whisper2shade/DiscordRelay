package org.nyxcode.wurm.discordrelay;



import java.util.Properties;

public class RelayConfig {

    private Properties properties;

    public RelayConfig(Properties properties) {
        this.properties = properties;
    }

    public String clientId() {
        return properties.getProperty("discordClientId");
    }

    public String clientSecret() {
        return properties.getProperty("discordClientSecret");
    }

    public String redirectUrl() {
        return properties.getProperty("discordRedirectUrl");
    }

    public long serverId() {
        return Long.parseLong(properties.getProperty("discordServerID"));
    }

    public String botToken() {
        return properties.getProperty("botToken");
    }

    public boolean useUnderscore() {
        return Boolean.parseBoolean(properties.getProperty("useUnderscore", "true"));
    }
}
