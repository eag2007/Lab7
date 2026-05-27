package org.example.client.managers;

import org.example.packet.ResponsePacket;
import org.example.packet.enums.Codes;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public class ManagerResponseQueue {

    private static final ManagerResponseQueue INSTANCE = new ManagerResponseQueue();

    private final BlockingQueue<ResponsePacket> queue = new LinkedBlockingQueue<>();
    private final AtomicReference<CompletableFuture<ResponsePacket>> pendingUpdate =
            new AtomicReference<>(null);

    private ManagerResponseQueue() {
    }

    public static ManagerResponseQueue getInstance() {
        return INSTANCE;
    }

    public void put(ResponsePacket packet) throws InterruptedException {
        if (packet.getStatusCode() == Codes.PUSH) {
            queue.put(packet);
            return;
        }

        CompletableFuture<ResponsePacket> future = pendingUpdate.getAndSet(null);
        if (future != null) {
            future.complete(packet);
        } else {
            queue.put(packet);
        }
    }

    public ResponsePacket take() throws InterruptedException {
        return queue.take();
    }

    public CompletableFuture<ResponsePacket> expectResponse() {
        CompletableFuture<ResponsePacket> future = new CompletableFuture<>();
        pendingUpdate.set(future);
        return future;
    }

    public void cancelExpected() {
        CompletableFuture<ResponsePacket> future = pendingUpdate.getAndSet(null);
        if (future != null) {
            future.cancel(false);
        }
    }
}