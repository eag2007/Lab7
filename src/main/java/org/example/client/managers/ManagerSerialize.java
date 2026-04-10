package org.example.client.managers;

import org.example.packet.CommandPacket;

import java.io.*;

public class ManagerSerialize {
    public static byte[] serialize(CommandPacket packet) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(packet);
        return baos.toByteArray();
    }
}