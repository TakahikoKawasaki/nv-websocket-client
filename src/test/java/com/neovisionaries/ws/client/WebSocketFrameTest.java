/*
 * Copyright (C) 2015-2016 Neo Visionaries Inc.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
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


    @Test
    public void test007()
    {
        WebSocketFrame frame = WebSocketFrame.createTextFrame("0123456789");
        List<WebSocketFrame> list = WebSocketFrame.splitIfNecessary(frame, 3, null);

        assertNotNull(list);
        assertEquals(4, list.size());

        frame = list.get(0);
        assertEquals("012", frame.getPayloadText());
        assertEquals(true,  frame.isTextFrame());
        assertEquals(false, frame.getFin());

        frame = list.get(1);
        assertEquals("345", frame.getPayloadText());
        assertEquals(true,  frame.isContinuationFrame());
        assertEquals(false, frame.getFin());

        frame = list.get(2);
        assertEquals("678", frame.getPayloadText());
        assertEquals(true,  frame.isContinuationFrame());
        assertEquals(false, frame.getFin());

        frame = list.get(3);
        assertEquals("9",   frame.getPayloadText());
        assertEquals(true,  frame.isContinuationFrame());
        assertEquals(true,  frame.getFin());
    }


    @Test
    public void test008()
    {
        WebSocketFrame frame = WebSocketFrame.createContinuationFrame("ABCDEF");
        List<WebSocketFrame> list = WebSocketFrame.splitIfNecessary(frame, 2, null);

        assertNotNull(list);
        assertEquals(3, list.size());

        frame = list.get(0);
        assertEquals("AB",  frame.getPayloadText());
        assertEquals(true,  frame.isContinuationFrame());
        assertEquals(false, frame.getFin());

        frame = list.get(1);
        assertEquals("CD",  frame.getPayloadText());
        assertEquals(true,  frame.isContinuationFrame());
        assertEquals(false, frame.getFin());

        frame = list.get(2);
        assertEquals("EF",  frame.getPayloadText());
        assertEquals(true,  frame.isContinuationFrame());
        assertEquals(false, frame.getFin());
    }


    @Test
    public void test009()
    {
        String payload = "000000000000000000000000000000";
        WebSocketFrame frame = WebSocketFrame.createTextFrame(payload);
        PerMessageCompressionExtension pmce = new PerMessageDeflateExtension();

        // splitIfNecessary() compresses the WebSocket frame and does not split it.
        List<WebSocketFrame> list = WebSocketFrame.splitIfNecessary(frame, payload.length() - 1, pmce);

        assertNull(list);

        // splitIfNecessary() compresses the WebSocket frame and
        list = WebSocketFrame.splitIfNecessary(frame, 1, pmce);
        assertNotNull(list);
    }


    @Test
    public void test010()
    {
        String payload = "000000000000000000000000000000111111111111111111111111111111";
        WebSocketFrame frame = WebSocketFrame.createTextFrame(payload);
        PerMessageCompressionExtension pmce = new PerMessageDeflateExtension();

        // splitIfNecessary() compresses the WebSocket frame and splits it.
        List<WebSocketFrame> list = WebSocketFrame.splitIfNecessary(frame, 1, pmce);

        assertNotNull(list);

        // Compute the total payload length of the WebSocket frames.
        int totalLength = 0;
        for (WebSocketFrame f : list)
        {
            totalLength += f.getPayloadLength();
        }

        // The total payload length of the WebSocket frames should be less
        // than the payload length of the original WebSocket frame.
        assertTrue(totalLength < payload.length());
    }
}
