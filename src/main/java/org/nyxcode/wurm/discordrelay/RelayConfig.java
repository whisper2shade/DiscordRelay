package org.nyxcode.wurm.discordrelay;

import com.sun.istack.internal.NotNull;

import java.util.Properties;

public class RelayConfig {

    @NotNull
    private Properties properties;

    public RelayConfig(@NotNull Properties properties) {
        this.properties = properties;
    }

    public String clientId() {
        return properties.getProperty("discordClientId");
    }

    public String clientSecret() {
        return properties.getProperty("discordClientSecret");
    }

    public CharSequence redirectUrl() {
        return properties.getProperty("discordRedirectUrl");
    }
}
