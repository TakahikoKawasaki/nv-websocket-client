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


import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


class WebSocketInputStream extends FilterInputStream
{
    public WebSocketInputStream(InputStream in)
    {
        super(in);
    }


    public String readLine() throws IOException
    {
        return Misc.readLine(this, "UTF-8");
    }


    public WebSocketFrame readFrame() throws IOException, WebSocketException
    {
        // Buffer.
        byte[] buffer = new byte[8];

        // Read the first two bytes.
        readBytes(buffer, 2);

        // FIN
        boolean fin = ((buffer[0] & 0x80) != 0);

        // RSV1, RSV2, RSV3
        boolean rsv1 = ((buffer[0] & 0x40) != 0);
        boolean rsv2 = ((buffer[0] & 0x20) != 0);
        boolean rsv3 = ((buffer[0] & 0x10) != 0);

        // Opcode
        int opcode = (buffer[0] & 0x0F);

        // Mask flag. This should never be true because the specification
        // (RFC 6455, 5. Data Framing, 5.1. Overview) says as follows:
        //
        //     A server MUST NOT mask any frames that it sends to the client.
        //
        boolean mask = ((buffer[1] & 0x80) != 0);

        // The payload length. It is expressed in 7 bits.
        long payloadLength = buffer[1] & 0x7F;

        if (payloadLength == 126)
        {
            // Read the extended payload length.
            // It is expressed in 2 bytes in network byte order.
            readBytes(buffer, 2);

            // Interpret the bytes as a number.
            payloadLength = (((buffer[0] & 0xFF) << 8) |
                             ((buffer[1] & 0xFF)     ));
        }
        else if (payloadLength == 127)
        {
            // Read the extended payload length.
            // It is expressed in 8 bytes in network byte order.
            readBytes(buffer, 8);

            // From RFC 6455, p29.
            //
            //   the most significant bit MUST be 0
            //
            if ((buffer[0] & 0x80) != 0)
            {
                // The payload length in a frame is invalid.
                throw new WebSocketException(
                    WebSocketError.INVALID_PAYLOAD_LENGTH,
                    "The payload length of a frame is invalid.");
            }

            // Interpret the bytes as a number.
            payloadLength = (((buffer[0] & 0xFF) << 56) |
                             ((buffer[1] & 0xFF) << 48) |
                             ((buffer[2] & 0xFF) << 40) |
                             ((buffer[3] & 0xFF) << 32) |
                             ((buffer[4] & 0xFF) << 24) |
                             ((buffer[5] & 0xFF) << 16) |
                             ((buffer[6] & 0xFF) <<  8) |
                             ((buffer[7] & 0xFF)      ));
        }

        // Masking key
        byte[] maskingKey = null;

        if (mask)
        {
            // Read the masking key. (This should never happen.)
            maskingKey = new byte[4];
            readBytes(maskingKey, 4);
        }

        if (Integer.MAX_VALUE < payloadLength)
        {
            // In Java, the maximum array size is Integer.MAX_VALUE.
            // Skip the payload and raise an exception.
            skipQuietly(payloadLength);
            throw new WebSocketException(
                WebSocketError.TOO_LONG_PAYLOAD,
                "The payload length of a frame exceeds the maximum array size in Java.");
        }

        // Read the payload if the payload length is not 0.
        byte[] payload = readPayload(payloadLength, mask, maskingKey);

        // Create a WebSocketFrame instance that represents a frame.
        return new WebSocketFrame()
            .setFin(fin)
            .setRsv1(rsv1)
            .setRsv2(rsv2)
            .setRsv3(rsv3)
            .setOpcode(opcode)
            .setMask(mask)
            .setPayload(payload);
    }


    private void readBytes(byte[] buffer, int length) throws IOException, WebSocketException
    {
        // Read
    	int total = 0;
    	while (total < length)
    	{
    		int count = read(buffer, total, length-total);
    		
	        if (count <= 0)
	        {
	            // The end of the stream has been reached unexpectedly.
	            throw new WebSocketException(
	                WebSocketError.INSUFFICENT_DATA,
	                "The end of the stream has been reached unexpectedly.");
	        }
	        
	        total += count;
    	}
    }


    private void skipQuietly(long length)
    {
        try
        {
            skip(length);
        }
        catch (IOException e)
        {
        }
    }


    private byte[] readPayload(long payloadLength, boolean mask, byte[] maskingKey) throws IOException, WebSocketException
    {
        if (payloadLength == 0)
        {
            return null;
        }

        byte[] payload;

        try
        {
            // Allocate a memory area to hold the content of the payload.
            payload = new byte[(int)payloadLength];
        }
        catch (OutOfMemoryError e)
        {
            // OutOfMemoryError occurred during a trial to allocate a memory area
            // for a frame's payload. Skip the payload and raise an exception.
            skipQuietly(payloadLength);
            throw new WebSocketException(
                WebSocketError.INSUFFICIENT_MEMORY_FOR_PAYLOAD,
                "OutOfMemoryError occurred during a trial to allocate a memory area for a frame's payload.");
        }

        // Read the payload.
        readBytes(payload, payload.length);

        // If masked.
        if (mask)
        {
            // Unmasked the payload.
            WebSocketFrame.mask(maskingKey, payload);
        }

        return payload;
    }
}
