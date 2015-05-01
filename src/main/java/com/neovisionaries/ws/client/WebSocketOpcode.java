/*
 * Copyright (C) 2015 Neo Visionaries Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.neovisionaries.ws.client;


/**
 * Opcode.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6455#section-5.2"
 *      >RFC 6455, 5.2. Base Framing Protocol</a>
 */
public class WebSocketOpcode
{
    /**
     * Opcode for "frame continuation" (0x0).
     */
    public static final int CONTINUATION = 0x0;


    /**
     * Opcode for "text frame" (0x1).
     */
    public static final int TEXT = 0x1;


    /**
     * Opcode for "binary frame" (0x2).
     */
    public static final int BINARY = 0x2;


    /**
     * Opcode for "connection close" (0x8).
     */
    public static final int CLOSE = 0x8;


    /**
     * Opcode for "ping" (0x9).
     */
    public static final int PING = 0x9;


    /**
     * Opcode for "pong" (0xA).
     */
    public static final int PONG = 0xA;


    private WebSocketOpcode()
    {
    }
}
