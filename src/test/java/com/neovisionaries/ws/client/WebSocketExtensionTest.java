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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;


public class WebSocketExtensionTest
{
    private static WebSocketExtension parse(String text)
    {
        return WebSocketExtension.parse(text);
    }


    @Test
    public void test001()
    {
        WebSocketExtension extension = parse("abc");

        assertNotNull(extension);
        assertEquals("abc", extension.getName());
    }


    @Test
    public void test002()
    {
        WebSocketExtension extension = parse("abc; x=1; y=2");

        assertNotNull(extension);
        assertEquals("abc", extension.getName());
        assertEquals("1", extension.getParameter("x"));
        assertEquals("2", extension.getParameter("y"));
    }


    @Test
    public void test003()
    {
        WebSocketExtension extension = parse("abc; x");

        assertNotNull(extension);
        assertEquals("abc", extension.getName());
        assertNull(extension.getParameter("x"));
        assertTrue(extension.containsParameter("x"));
    }


    @Test
    public void test004()
    {
        WebSocketExtension extension = parse("abc; x=");

        assertNotNull(extension);
        assertEquals("abc", extension.getName());
        assertFalse(extension.containsParameter("x"));
    }


    @Test
    public void test005()
    {
        WebSocketExtension extension = parse("abc; x=\"1\"; y=\"2\"");

        assertNotNull(extension);
        assertEquals("abc", extension.getName());
        assertEquals("1", extension.getParameter("x"));
        assertEquals("2", extension.getParameter("y"));
    }
}
