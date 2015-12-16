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


import java.util.Map;


class PerMessageDeflateExtension extends WebSocketExtension
{
    public static final String EXTENSION_NAME = "permessage-deflate";

    private static final String SERVER_NO_CONTEXT_TAKEOVER = "server_no_context_takeover";
    private static final String CLIENT_NO_CONTEXT_TAKEOVER = "client_no_context_takeover";
    private static final String SERVER_MAX_WINDOW_BITS     = "server_max_window_bits";
    private static final String CLIENT_MAX_WINDOW_BITS     = "client_max_window_bits";

    private static final int MIN_BITS = 8;
    private static final int MAX_BITS = 15;
    private static final int MIN_WINDOW_SIZE = 256;
    private static final int DEFAULT_WINDOW_SIZE = 32768;


    private boolean mServerNoContextTakeover;
    private boolean mClientNoContextTakeover;
    private int mServerWindowSize = DEFAULT_WINDOW_SIZE;
    private int mClientWindowSize = DEFAULT_WINDOW_SIZE;


    public PerMessageDeflateExtension(String name)
    {
        super(name);
    }


    @Override
    void validate() throws WebSocketException
    {
        // For each parameter
        for (Map.Entry<String, String> entry : getParameters().entrySet())
        {
            validateParameter(entry.getKey(), entry.getValue());
        }
    }


    private void validateParameter(String key, String value) throws WebSocketException
    {
        if (SERVER_NO_CONTEXT_TAKEOVER.equals(key))
        {
            mServerNoContextTakeover = true;
        }
        else if (CLIENT_NO_CONTEXT_TAKEOVER.equals(key))
        {
            mClientNoContextTakeover = true;
        }
        else if (SERVER_MAX_WINDOW_BITS.equals(key))
        {
            mServerWindowSize = computeWindowSize(key, value);
        }
        else if (CLIENT_MAX_WINDOW_BITS.equals(key))
        {
            mClientWindowSize = computeWindowSize(key, value);
        }
        else
        {
            // permessage-deflate extension contains an unsupported parameter.
            throw new WebSocketException(
                WebSocketError.PERMESSAGE_DEFLATE_UNSUPPORTED_PARAMETER,
                "permessage-deflate extension contains an unsupported parameter: " + key);
        }
    }


    private int computeWindowSize(String key, String value) throws WebSocketException
    {
        int bits = extractMaxWindowBits(key, value);
        int size = MIN_WINDOW_SIZE;

        for (int i = MIN_BITS; i < bits; ++i)
        {
            size *= 2;
        }

        return size;
    }


    private int extractMaxWindowBits(String key, String value) throws WebSocketException
    {
        int bits = parseMaxWindowBits(value);

        if (bits < 0)
        {
            String message = String.format(
                    "The value of %s parameter of permessage-deflate extension is invalid: %s",
                    key, value);

            throw new WebSocketException(
                WebSocketError.PERMESSAGE_DEFLATE_INVALID_MAX_WINDOW_BITS, message);
        }

        return bits;
    }


    private int parseMaxWindowBits(String value)
    {
        if (value == null)
        {
            return -1;
        }

        int bits;

        try
        {
            bits = Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
            return -1;
        }

        if (bits < MIN_BITS || MAX_BITS < bits)
        {
            return -1;
        }

        return bits;
    }


    public boolean isServerNoContextTakeover()
    {
        return mServerNoContextTakeover;
    }


    public boolean isClientNoContextTakeover()
    {
        return mClientNoContextTakeover;
    }


    public int getServerWindowSize()
    {
        return mServerWindowSize;
    }


    public int getClientWindowSize()
    {
        return mClientWindowSize;
    }
}
