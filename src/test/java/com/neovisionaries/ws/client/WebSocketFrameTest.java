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


import static org.junit.Assert.assertTrue;
import org.junit.Test;


public class WebSocketFrameTest
{
    @Test
    public void test001()
    {
        WebSocketFrame frame = WebSocketFrame.createTextFrame(null);

        assertTrue(frame.toString().endsWith("Payload=null)"));
    }


    @Test
    public void test002()
    {
        WebSocketFrame frame = WebSocketFrame.createTextFrame("dummy");
        frame.setRsv1(true);

        assertTrue(frame.toString().endsWith("Payload=compressed)"));
    }


    @Test
    public void test003()
    {
        WebSocketFrame frame = WebSocketFrame.createTextFrame("hello");

        assertTrue(frame.toString().endsWith("Payload=\"hello\")"));
    }


    @Test
    public void test004()
    {
        WebSocketFrame frame = WebSocketFrame.createBinaryFrame(null);

        assertTrue(frame.toString().endsWith("Payload=null)"));
    }


    @Test
    public void test005()
    {
        byte[] payload = new byte[] { (byte)0x01, (byte)0x23, (byte)0xAB };
        WebSocketFrame frame = WebSocketFrame.createBinaryFrame(payload);
        frame.setRsv1(true);

        assertTrue(frame.toString().endsWith("Payload=compressed)"));
    }


    @Test
    public void test006()
    {
        byte[] payload = new byte[] { (byte)0x01, (byte)0x23, (byte)0xAB };
        WebSocketFrame frame = WebSocketFrame.createBinaryFrame(payload);

        assertTrue(frame.toString().endsWith("Payload=01 23 AB)"));
    }
}
