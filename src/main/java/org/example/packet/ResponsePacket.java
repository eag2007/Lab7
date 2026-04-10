package org.example.packet;

import java.io.Serializable;
import java.util.List;

public class ResponsePacket implements Serializable {
    private final int statusCode;
    private final String message;
    private final Object data;

    public ResponsePacket(int statusCode, String message, Object data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    public int getStatusCode() { return statusCode; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
}