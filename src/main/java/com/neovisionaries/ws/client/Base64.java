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


class Base64
{
    private static final byte[] INDEX_TABLE = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
        'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
        'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
        'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };


    public static String encode(String data)
    {
        if (data == null)
        {
            return null;
        }

        return encode(Misc.getBytesUTF8(data));
    }


    public static String encode(byte[] data)
    {
        if (data == null)
        {
            return null;
        }

        int capacity = (((((data.length * 8) + 5) / 6) + 3) / 4) * 4;

        StringBuilder builder = new StringBuilder(capacity);

        for (int bitIndex = 0; ; bitIndex += 6)
        {
            int bits = extractBits(data, bitIndex);

            if (bits < 0)
            {
                break;
            }

            builder.append((char)INDEX_TABLE[bits]);
        }

        for (int i = builder.length(); i < capacity; ++i)
        {
            builder.append('=');
        }

        return builder.toString();
    }


    private static int extractBits(byte[] data, int bitIndex)
    {
        int byteIndex = bitIndex / 8;
        byte nextByte;

        if (data.length <= byteIndex)
        {
            return -1;
        }
        else if (data.length - 1 == byteIndex)
        {
            nextByte = 0;
        }
        else
        {
            nextByte = data[byteIndex + 1];
        }

        switch ((bitIndex % 24) / 6)
        {
            case 0:
                return ((data[byteIndex] >> 2) & 0x3F);

            case 1:
                return (((data[byteIndex] << 4) & 0x30) | ((nextByte >> 4) & 0x0F));

            case 2:
                return (((data[byteIndex] << 2) & 0x3C) | ((nextByte >> 6) & 0x03));

            case 3:
                return (data[byteIndex] & 0x3F);

            default:
                // Never reach here.
                return 0;
        }
    }
}
