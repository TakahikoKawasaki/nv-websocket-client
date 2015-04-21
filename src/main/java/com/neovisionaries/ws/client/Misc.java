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


import static com.neovisionaries.ws.client.WebSocketOpcode.BINARY;
import static com.neovisionaries.ws.client.WebSocketOpcode.CLOSE;
import static com.neovisionaries.ws.client.WebSocketOpcode.CONTINUATION;
import static com.neovisionaries.ws.client.WebSocketOpcode.PING;
import static com.neovisionaries.ws.client.WebSocketOpcode.PONG;
import static com.neovisionaries.ws.client.WebSocketOpcode.TEXT;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;


class Misc
{
    private static final SecureRandom sRandom = new SecureRandom();


    private Misc()
    {
    }


    public static byte[] getBytesUTF8(String string)
    {
        if (string == null)
        {
            return null;
        }

        try
        {
            return string.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            // This never happens.
            return null;
        }
    }


    public static String toStringUTF8(byte[] bytes)
    {
        if (bytes == null)
        {
            return null;
        }

        return toStringUTF8(bytes, 0, bytes.length);
    }


    public static String toStringUTF8(byte[] bytes, int offset, int length)
    {
        if (bytes == null)
        {
            return null;
        }

        try
        {
            return new String(bytes, offset, length, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            // This never happens.
            return null;
        }
        catch (IndexOutOfBoundsException e)
        {
            return null;
        }
    }


    public static byte[] nextBytes(byte[] buffer)
    {
        sRandom.nextBytes(buffer);

        return buffer;
    }


    public static byte[] nextBytes(int nBytes)
    {
        byte[] buffer = new byte[nBytes];

        return nextBytes(buffer);
    }


    public static String toOpcodeName(int opcode)
    {
        switch (opcode)
        {
            case CONTINUATION:
                return "CONTINUATION";

            case TEXT:
                return "TEXT";

            case BINARY:
                return "BINARY";

            case CLOSE:
                return "CLOSE";

            case PING:
                return "PING";

            case PONG:
                return "PONG";

            default:
                break;
        }

        if (0x1 <= opcode && opcode <= 0x7)
        {
            return String.format("DATA(0x%X)", opcode);
        }

        if (0x8 <= opcode && opcode <= 0xF)
        {
            return String.format("CONTROL(0x%X)", opcode);
        }

        return String.format("0x%X", opcode);
    }
}
