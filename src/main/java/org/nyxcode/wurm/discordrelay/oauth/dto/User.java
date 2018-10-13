package org.nyxcode.wurm.discordrelay.oauth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private long id;

    public long id() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
