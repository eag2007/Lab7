package org.example.server.modules;

import org.example.packet.ResponsePacket;
import org.example.server.logger.ServerLogger;
import org.example.server.managers.ManagerSerialize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.zip.GZIPOutputStream;

public class WriteModule {

    public void writeResponseForClient(SocketChannel client, ResponsePacket response) throws IOException {
        /**
         * Сериализуем
         */
        byte[] data = ManagerSerialize.serialize(response);

        /**
         * Сжимаем данные
         */

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            gzipOut.write(data);
        }
        byte[] compressedData = baos.toByteArray();

        ServerLogger.debug("Исходный размер: {} байт", data.length);
        ServerLogger.debug("Сжатый размер: {} байт", compressedData.length);
        ServerLogger.debug("Сжатие: {}%", (100 - (compressedData.length * 100 / data.length)));

        /**
         * Отправляем размер
         */

        ByteBuffer sizeBuffer = ByteBuffer.allocate(8);
        sizeBuffer.putLong(compressedData.length);
        sizeBuffer.flip();
        client.write(sizeBuffer);

        /**
         * Отправляем сжатые данные
         */

        ByteBuffer buffer = ByteBuffer.wrap(compressedData);
        client.write(buffer);

        ServerLogger.debug("Данные отправлены с кодом {} клиенту {}", response.getStatusCode(), client.getRemoteAddress());
    }
}