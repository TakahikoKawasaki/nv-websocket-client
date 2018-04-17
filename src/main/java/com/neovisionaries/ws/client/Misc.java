/*
 * Copyright (C) 2015-2018 Neo Visionaries Inc.
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class Misc
{
    private static final SecureRandom sRandom = new SecureRandom();


    private Misc()
    {
    }


    /**
     * Get a UTF-8 byte array representation of the given string.
     */
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


    /**
     * Convert a UTF-8 byte array into a string.
     */
    public static String toStringUTF8(byte[] bytes)
    {
        if (bytes == null)
        {
            return null;
        }

        return toStringUTF8(bytes, 0, bytes.length);
    }


    /**
     * Convert a UTF-8 byte array into a string.
     */
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


    /**
     * Fill the given buffer with random bytes.
     */
    public static byte[] nextBytes(byte[] buffer)
    {
        sRandom.nextBytes(buffer);

        return buffer;
    }


    /**
     * Create a buffer of the given size filled with random bytes.
     */
    public static byte[] nextBytes(int nBytes)
    {
        byte[] buffer = new byte[nBytes];

        return nextBytes(buffer);
    }


    /**
     * Convert a WebSocket opcode into a string representation.
     */
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


    /**
     * Read a line from the given stream.
     */
    public static String readLine(InputStream in, String charset) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        while (true)
        {
            // Read one byte from the stream.
            int b = in.read();

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
            int b2 = in.read();

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
        return baos.toString(charset);
    }


    /**
     * Find the minimum value from the given array.
     */
    public static int min(int[] values)
    {
        int min = Integer.MAX_VALUE;

        for (int i = 0; i < values.length; ++i)
        {
            if (values[i] < min)
            {
                min = values[i];
            }
        }

        return min;
    }


    /**
     * Find the maximum value from the given array.
     */
    public static int max(int[] values)
    {
        int max = Integer.MIN_VALUE;

        for (int i = 0; i < values.length; ++i)
        {
            if (max < values[i])
            {
                max = values[i];
            }
        }

        return max;
    }


    public static String join(Collection<?> values, String delimiter)
    {
        StringBuilder builder = new StringBuilder();

        join(builder, values, delimiter);

        return builder.toString();
    }


    private static void join(StringBuilder builder, Collection<?> values, String delimiter)
    {
        boolean first = true;

        for (Object value : values)
        {
            if (first)
            {
                first = false;
            }
            else
            {
                builder.append(delimiter);
            }

            builder.append(value.toString());
        }
    }


    public static String extractHost(URI uri)
    {
        // Extract the host part from the URI.
        String host = uri.getHost();

        if (host != null)
        {
            return host;
        }

        // According to Issue#74, URI.getHost() method returns null in
        // the following environment when the host part of the URI is
        // a host name.
        //
        //   - Samsung Galaxy S3 + Android API 18
        //   - Samsung Galaxy S4 + Android API 21
        //
        // The following is a workaround for the issue.

        // Extract the host part from the authority part of the URI.
        host = extractHostFromAuthorityPart(uri.getRawAuthority());

        if (host != null)
        {
            return host;
        }

        // Extract the host part from the entire URI.
        return extractHostFromEntireUri(uri.toString());
    }


    static String extractHostFromAuthorityPart(String authority)
    {
        // If the authority part is not available.
        if (authority == null)
        {
            // Hmm... This should not happen.
            return null;
        }

        // Parse the authority part. The expected format is "[id:password@]host[:port]".
        Matcher matcher = Pattern.compile("^(.*@)?([^:]+)(:\\d+)?$").matcher(authority);

        // If the authority part does not match the expected format.
        if (matcher == null || matcher.matches() == false)
        {
            // Hmm... This should not happen.
            return null;
        }

        // Return the host part.
        return matcher.group(2);
    }


    static String extractHostFromEntireUri(String uri)
    {
        if (uri == null)
        {
            // Hmm... This should not happen.
            return null;
        }

        // Parse the URI. The expected format is "scheme://[id:password@]host[:port][...]".
        Matcher matcher = Pattern.compile("^\\w+://([^@/]*@)?([^:/]+)(:\\d+)?(/.*)?$").matcher(uri);

        // If the URI does not match the expected format.
        if (matcher == null || matcher.matches() == false)
        {
            // Hmm... This should not happen.
            return null;
        }

        // Return the host part.
        return matcher.group(2);
    }


    public static Constructor<?> getConstructor(String className, Class<?>[] parameterTypes)
    {
        try
        {
            return Class.forName(className).getConstructor(parameterTypes);
        }
        catch (Exception e)
        {
            return null;
        }
    }


    public static Object newInstance(Constructor<?> constructor, Object... parameters)
    {
        if (constructor == null)
        {
            return null;
        }

        try
        {
            return constructor.newInstance(parameters);
        }
        catch (Exception e)
        {
            return null;
        }
    }


    public static Method getMethod(String className, String methodName, Class<?>[] parameterTypes)
    {
        try
        {
            return Class.forName(className).getMethod(methodName, parameterTypes);
        }
        catch (Exception e)
        {
            return null;
        }
    }


    public static Object invoke(Method method, Object object, Object... parameters)
    {
        if (method == null)
        {
            return null;
        }

        try
        {
            return method.invoke(object, parameters);
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
