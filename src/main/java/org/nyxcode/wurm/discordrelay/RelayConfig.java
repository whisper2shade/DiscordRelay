package org.nyxcode.wurm.discordrelay;

import com.sun.istack.internal.NotNull;

import java.util.Properties;

public class RelayConfig {

    @NotNull
    private Properties properties;

    public RelayConfig(@NotNull Properties properties) {
        this.properties = properties;
    }
}
