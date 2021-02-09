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


import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;


class WebSocketOutputStream extends BufferedOutputStream
{
    public WebSocketOutputStream(OutputStream out)
    {
        super(out);
    }


    public void write(String string) throws IOException
    {
        // Convert the string into a byte array.
        byte[] bytes = Misc.getBytesUTF8(string);

        write(bytes);
    }


    public void write(WebSocketFrame frame) throws IOException
    {
        writeFrame0(frame);
        writeFrame1(frame);
        writeFrameExtendedPayloadLength(frame);

        // Generate a random masking key.
        byte[] maskingKey = Misc.nextBytes(4);

        // Write the masking key.
        write(maskingKey);

        // Write the payload.
        writeFramePayload(frame, maskingKey);
    }


    private void writeFrame0(WebSocketFrame frame) throws IOException
    {
        int b = (frame.getFin()  ? 0x80 : 0x00)
              | (frame.getRsv1() ? 0x40 : 0x00)
              | (frame.getRsv2() ? 0x20 : 0x00)
              | (frame.getRsv3() ? 0x10 : 0x00)
              | (frame.getOpcode() & 0x0F);

        write(b);
    }


    private void writeFrame1(WebSocketFrame frame) throws IOException
    {
        // Frames sent from a client are always masked.
        int b = 0x80;

        int len = frame.getPayloadLength();

        if (len <= 125)
        {
            b |= len;
        }
        else if (len <= 65535)
        {
            b |= 126;
        }
        else
        {
            b |= 127;
        }

        write(b);
    }


    private void writeFrameExtendedPayloadLength(WebSocketFrame frame) throws IOException
    {
        int len = frame.getPayloadLength();
        byte buf[];

        if (len <= 125)
        {
            return;
        }

        if (len <= 65535)
        {
            buf = new byte[2];
            // 2-byte in network byte order.
            buf[1] = (byte) (len & 0xFF);
            buf[0] = (byte) ((len >> 8) & 0xFF);
        } else {
            buf = new byte[8];
            for (int i = 7; i >= 0; i--) {
                buf[i] = (byte) (len & 0xFF);
                len >>>= 8;
            }
        }
        write(buf);
    }


    private void writeFramePayload(WebSocketFrame frame, byte[] maskingKey) throws IOException
    {
        byte[] payload = frame.getPayload();

        if (payload == null)
        {
            return;
        }

        byte[] masked = new byte[payload.length];

        for (int i = 0; i < payload.length; ++i)
        {
            // Mask
            masked[i] = (byte)((payload[i] ^ maskingKey[i % 4]) & 0xFF);
        }
        // Write
        write(masked);
    }
}
