package org.nyxcode.wurm.discordrelay.oauth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Connection {

    private String id;
    private String type;

    public String id() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String type() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
