package org.example.server.modules;

import org.example.packet.CommandPacket;
import org.example.server.logger.ServerLogger;
import org.example.server.managers.ManagerDeserialize;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class ReadModule {
    private static final int BUFFER_SIZE = 8192;

    public CommandPacket readPacketForServer(SocketChannel clientChannel) throws IOException {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            buffer.clear();

            int r = clientChannel.read(buffer);
            if (r == -1) {
                ServerLogger.info("Клиент закрыл соединение: {}", clientChannel.getRemoteAddress());
                return new CommandPacket(null, null, null, null, null);
            }

            if (r == 0) {
                ServerLogger.debug("Нет данных от клиента: {}", clientChannel.getRemoteAddress());
                return null;
            }


            buffer.flip();

            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);

            CommandPacket packet = ManagerDeserialize.deserialize(data);
            ServerLogger.debug("Получена команда: {} от {}", packet.getType(), clientChannel.getRemoteAddress());
            ServerLogger.debug("Получены аргументы: {} от {}", Arrays.toString(packet.getArgs()), clientChannel.getRemoteAddress());
            ServerLogger.debug("Получены значения: {} от {}", packet.getValues(), clientChannel.getRemoteAddress());

            return packet;
        } catch (ClassNotFoundException e) {
            ServerLogger.error("Ошибка десиарилизации: {}", clientChannel.getRemoteAddress());
            return null;
        } catch (IOException e) {
            ServerLogger.error("Ошибка чтения от {}: {}", clientChannel.getRemoteAddress(), e.getMessage());
            return null;
        }
    }
}