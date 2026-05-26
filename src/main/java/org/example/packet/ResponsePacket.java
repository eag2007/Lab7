package org.example.packet;

import org.example.packet.enums.Codes;

import java.io.Serializable;
import java.util.List;

public class ResponsePacket implements Serializable {
    private final Codes statusCode;
    private final String message;
    private final Object data;

    public ResponsePacket(Codes statusCode, String message, Object data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    public Codes getStatusCode() { return statusCode; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
}