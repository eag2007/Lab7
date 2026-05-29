package org.example.packet.enums;

import java.io.Serializable;

public enum Codes implements Serializable {
    OK(200),
    WARNING(400),
    ERROR(500),
    PUSH(300),
    PUSH_ERROR(350);

    private final int code;

    Codes(int code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code + "";
    }
}
