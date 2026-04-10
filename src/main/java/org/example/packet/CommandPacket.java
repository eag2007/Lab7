package org.example.packet;

import org.example.packet.collection.RouteClient;

import java.io.Serializable;

public class CommandPacket implements Serializable {
    private final String type;
    private final String[] args;
    private final RouteClient values;
    private final String login;
    private final String password;

    public CommandPacket(String type, String[] args, RouteClient values, String login, String password) {
        this.type = type;
        this.args = args;
        this.values = values;
        this.login = login;
        this.password = password;
    }

    public String getType() { return this.type; }
    public String[] getArgs() { return this.args; }
    public RouteClient getValues() { return this.values; }
    public String getLogin() { return this.login; }
    public String getPassword() { return this.password; }
}