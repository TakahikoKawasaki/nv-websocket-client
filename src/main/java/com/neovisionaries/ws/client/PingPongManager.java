package com.neovisionaries.ws.client;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

class PingPongManager {

    private final WebSocket webSocket;
    // for first ping pong should be received, so default value is true
    private final AtomicBoolean pongInfoHolder = new AtomicBoolean(true);
    private final ScheduledExecutorService scheduler;
    private PayloadGenerator generator;
    private long pingInterval;
    private boolean isStarted = false;

    PingPongManager(WebSocket webSocket, PayloadGenerator generator) {
        this.webSocket = webSocket;
        this.generator = generator;
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * @see WebSocket#setPingInterval(long)
     */
    void setPingInterval(long pingInterval) {
        synchronized (this) {

            if (pingInterval == 0) {
                stop();
            } else if (pingInterval > 0) {
                if (isStarted) stop();
                startPingPong();
            }
        }
    }

    /**
     * @see WebSocket#setPingPayloadGenerator(PayloadGenerator)
     */
    public void setPayloadGenerator(PayloadGenerator generator) {
        synchronized (this) {
            this.generator = generator;
        }
    }

    void stop() {
        synchronized (this) {
            scheduler.shutdown();
            isStarted = false;
        }
    }

    void processPongFrame(WebSocketFrame pongFrame) {
        pongInfoHolder.set(true); // pong is received
    }

    private void startPingPong() {
        isStarted = true;
        // send first ping after given interval
        scheduler.scheduleAtFixedRate(getTask(), pingInterval, pingInterval, TimeUnit.MILLISECONDS);
    }

    private Runnable getTask() {
        return new Runnable() {
            public void run() {
                boolean isPongReceived = pongInfoHolder.get();
                if (isPongReceived) {
                    pongInfoHolder.set(false);
                    WebSocketFrame pingFrame = WebSocketFrame.createPingFrame(generator.generate());
                    webSocket.sendFrame(pingFrame);
                } else {
                    // server doesn't sent pond on our ping, so it may be disconnected
                    notifyDisconnected();
                }
            }
        };
    }

    private void notifyDisconnected() {
        webSocket.finish();
    }

}
