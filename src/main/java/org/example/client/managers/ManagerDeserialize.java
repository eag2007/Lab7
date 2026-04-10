package org.example.client.managers;

import org.example.packet.ResponsePacket;

import java.io.*;

public class ManagerDeserialize {
    public static ResponsePacket deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return (ResponsePacket) ois.readObject();
    }
}