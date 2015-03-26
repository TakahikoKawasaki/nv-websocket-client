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


import java.io.ByteArrayOutputStream;
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        while (true)
        {
            // Read one byte from the stream.
            int b = super.read();

            // If the end of the stream was reached.
            if (b == -1)
            {
                if (baos.size() == 0)
                {
                    // No more line.
                    return null;
                }
                else
                {
                    // The end of the line was reached.
                    break;
                }
            }

            if (b == '\n')
            {
                // The end of the line was reached.
                break;
            }

            if (b != '\r')
            {
                // Normal character.
                baos.write(b);
                continue;
            }

            // Read one more byte.
            int b2 = super.read();

            // If the end of the stream was reached.
            if (b2 == -1)
            {
                // Treat the '\r' as a normal character.
                baos.write(b);

                // The end of the line was reached.
                break;
            }

            // If '\n' follows the '\r'.
            if (b2 == '\n')
            {
                // The end of the line was reached.
                break;
            }

            // Treat the '\r' as a normal character.
            baos.write(b);

            // Append the byte which follows the '\r'.
            baos.write(b2);
        }

        // Convert the byte array to a string.
        return baos.toString("UTF-8");
    }
}
