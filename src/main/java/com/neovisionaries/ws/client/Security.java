package com.neovisionaries.ws.client;

import java.security.SecureRandom;

/**
 * WebSocket Security.
 */
public final class Security {

    private final SecureRandom sRandom;

    private static Security instance;

    private Security() {
        sRandom = new SecureRandom();
    }

    /**
     * Returns the security instance.
     *
     * @return security instance.
     */
    public static Security getInstance() {
        if (instance == null) {
            instance = new Security();
        }
        return instance;
    }

    /**
     * Fill the given buffer with random bytes.
     */
    public byte[] nextBytes(byte[] buffer) {
        sRandom.nextBytes(buffer);
        return buffer;
    }

    /**
     * Create a buffer of the given size filled with random bytes.
     */
    public byte[] nextBytes(int nBytes) {
        byte[] buffer = new byte[nBytes];
        return nextBytes(buffer);
    }

}