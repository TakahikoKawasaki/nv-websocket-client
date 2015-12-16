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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;


public class PerMessageDeflateExtensionTest
{
    private static final int DEFAULT_WINDOW_SIZE = 32768;


    private static PerMessageDeflateExtension parse(String text)
    {
        return (PerMessageDeflateExtension)WebSocketExtension.parse(text);
    }


    private static PerMessageDeflateExtension parseValid(String text)
    {
        PerMessageDeflateExtension extension = parse(text);

        try
        {
            extension.validate();
        }
        catch (WebSocketException e)
        {
            return null;
        }

        return extension;
    }


    private static WebSocketException parseInvalid(String text)
    {
        PerMessageDeflateExtension extension = parse(text);

        try
        {
            extension.validate();
        }
        catch (WebSocketException e)
        {
            return e;
        }

        return null;
    }


    @Test
    public void test001()
    {
        PerMessageDeflateExtension extension = parseValid("permessage-deflate");

        assertNotNull(extension);
        assertFalse(extension.isServerNoContextTakeover());
        assertFalse(extension.isClientNoContextTakeover());
        assertEquals(DEFAULT_WINDOW_SIZE, extension.getServerWindowSize());
        assertEquals(DEFAULT_WINDOW_SIZE, extension.getClientWindowSize());
    }


    @Test
    public void test002()
    {
        PerMessageDeflateExtension extension = parseValid("permessage-deflate; server_no_context_takeover; client_no_context_takeover");

        assertNotNull(extension);
        assertTrue(extension.isServerNoContextTakeover());
        assertTrue(extension.isClientNoContextTakeover());
    }


    @Test
    public void test003()
    {
        PerMessageDeflateExtension extension = parseValid("permessage-deflate; server_max_window_bits=8; client_max_window_bits=8");

        assertNotNull(extension);
        assertEquals(256, extension.getServerWindowSize());
        assertEquals(256, extension.getClientWindowSize());
    }


    @Test
    public void test004()
    {
        WebSocketException exception = parseInvalid("permessage-deflate; unknown_parameter");

        assertNotNull(exception);
        assertSame(WebSocketError.PERMESSAGE_DEFLATE_UNSUPPORTED_PARAMETER, exception.getError());
    }


    @Test
    public void test005()
    {
        WebSocketException exception = parseInvalid("permessage-deflate; server_max_window_bits");

        assertNotNull(exception);
        assertSame(WebSocketError.PERMESSAGE_DEFLATE_INVALID_MAX_WINDOW_BITS, exception.getError());
    }


    @Test
    public void test006()
    {
        WebSocketException exception = parseInvalid("permessage-deflate; server_max_window_bits=abc");

        assertNotNull(exception);
        assertSame(WebSocketError.PERMESSAGE_DEFLATE_INVALID_MAX_WINDOW_BITS, exception.getError());
    }


    @Test
    public void test007()
    {
        WebSocketException exception = parseInvalid("permessage-deflate; server_max_window_bits=0");

        assertNotNull(exception);
        assertSame(WebSocketError.PERMESSAGE_DEFLATE_INVALID_MAX_WINDOW_BITS, exception.getError());
    }


    @Test
    public void test008()
    {
        WebSocketException exception = parseInvalid("permessage-deflate; server_max_window_bits=7");

        assertNotNull(exception);
        assertSame(WebSocketError.PERMESSAGE_DEFLATE_INVALID_MAX_WINDOW_BITS, exception.getError());
    }


    @Test
    public void test009()
    {
        WebSocketException exception = parseInvalid("permessage-deflate; server_max_window_bits=16");

        assertNotNull(exception);
        assertSame(WebSocketError.PERMESSAGE_DEFLATE_INVALID_MAX_WINDOW_BITS, exception.getError());
    }


    @Test
    public void test010()
    {
        WebSocketException exception = parseInvalid("permessage-deflate; client_max_window_bits");

        assertNotNull(exception);
        assertSame(WebSocketError.PERMESSAGE_DEFLATE_INVALID_MAX_WINDOW_BITS, exception.getError());
    }


    @Test
    public void test011()
    {
        WebSocketException exception = parseInvalid("permessage-deflate; client_max_window_bits=abc");

        assertNotNull(exception);
        assertSame(WebSocketError.PERMESSAGE_DEFLATE_INVALID_MAX_WINDOW_BITS, exception.getError());
    }


    @Test
    public void test012()
    {
        WebSocketException exception = parseInvalid("permessage-deflate; client_max_window_bits=0");

        assertNotNull(exception);
        assertSame(WebSocketError.PERMESSAGE_DEFLATE_INVALID_MAX_WINDOW_BITS, exception.getError());
    }


    @Test
    public void test013()
    {
        WebSocketException exception = parseInvalid("permessage-deflate; client_max_window_bits=7");

        assertNotNull(exception);
        assertSame(WebSocketError.PERMESSAGE_DEFLATE_INVALID_MAX_WINDOW_BITS, exception.getError());
    }


    @Test
    public void test014()
    {
        WebSocketException exception = parseInvalid("permessage-deflate; client_max_window_bits=16");

        assertNotNull(exception);
        assertSame(WebSocketError.PERMESSAGE_DEFLATE_INVALID_MAX_WINDOW_BITS, exception.getError());
    }
}
