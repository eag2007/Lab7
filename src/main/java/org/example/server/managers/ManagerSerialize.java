package org.example.server.managers;

import org.example.packet.CommandPacket;
import org.example.packet.ResponsePacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class ManagerSerialize {
    public static byte[] serialize(ResponsePacket packet) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(packet);
        return baos.toByteArray();
    }
}