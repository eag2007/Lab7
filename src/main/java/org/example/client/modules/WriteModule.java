package org.example.client.modules;

import org.example.packet.CommandPacket;
import org.example.client.managers.ManagerSerialize;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class WriteModule {
    public void writePacketForServer(SocketChannel server, CommandPacket commandPacket) throws IOException {
        byte[] data = ManagerSerialize.serialize(commandPacket);
        ByteBuffer buffer = ByteBuffer.wrap(data);
        server.write(buffer);
    }
}
