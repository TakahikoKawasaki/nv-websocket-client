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


public class WebSocketFrame
{
    public static class Opcode
    {
        public static final int CONTINUATION = 0x0;
        public static final int TEXT         = 0x1;
        public static final int BINARY       = 0x2;
        public static final int CLOSE        = 0x8;
        public static final int PING         = 0x9;
        public static final int PONG         = 0xA;
    }


    private boolean mFin;
    private boolean mRsv1;
    private boolean mRsv2;
    private boolean mRsv3;
    private int mOpcode;
    private byte[] mMask;
    private byte[] mPayload;


    public boolean getFin()
    {
        return mFin;
    }


    public WebSocketFrame setFin(boolean fin)
    {
        mFin = fin;

        return this;
    }


    public boolean getRsv1()
    {
        return mRsv1;
    }


    public WebSocketFrame setRsv1(boolean rsv1)
    {
        mRsv1 = rsv1;

        return this;
    }


    public boolean getRsv2()
    {
        return mRsv2;
    }


    public WebSocketFrame setRsv2(boolean rsv2)
    {
        mRsv2 = rsv2;

        return this;
    }


    public boolean getRsv3()
    {
        return mRsv3;
    }


    public WebSocketFrame setRsv3(boolean rsv3)
    {
        mRsv3 = rsv3;

        return this;
    }


    /**
     * Get the opcode.
     *
     * <table border="1" cellpadding="5" style="table-collapse: collapse;">
     *   <thead>
     *     <tr>
     *       <th>Value</th>
     *       <th>Description</th>
     *     </tr>
     *   </thead>
     *   <tbody>
     *     <tr>
     *       <td>0x0</td>
     *       <td>Frame continuation</td>
     *     </tr>
     *     <tr>
     *       <td>0x1</td>
     *       <td>Text frame</td>
     *     </tr>
     *     <tr>
     *       <td>0x2</td>
     *       <td>Binary frame</td>
     *     </tr>
     *     <tr>
     *       <td>0x3-0x7</td>
     *       <td>Reserved</td>
     *     </tr>
     *     <tr>
     *       <td>0x8</td>
     *       <td>Connection close</td>
     *     </tr>
     *     <tr>
     *       <td>0x9</td>
     *       <td>Ping</td>
     *     </tr>
     *     <tr>
     *       <td>0xA</td>
     *       <td>Pong</td>
     *     </tr>
     *     <tr>
     *       <td>0xB-0xF</td>
     *       <td>Reserved</td>
     *     </tr>
     *   </tbody>
     * </table>
     *
     * @return
     *         The opecode.
     */
    public int getOpcode()
    {
        return mOpcode;
    }


    public WebSocketFrame setOpcode(int opcode)
    {
        mOpcode = opcode;

        return this;
    }


    public byte[] getMask()
    {
        return mMask;
    }


    public WebSocketFrame setMask(byte[] mask)
    {
        mMask = mask;

        return this;
    }


    public byte[] getPayload()
    {
        return mPayload;
    }


    public WebSocketFrame setPayload(byte[] payload)
    {
        mPayload = payload;

        return this;
    }


    static void mask(byte[] mask, byte[] payload)
    {
        for (int i = 0; i < payload.length; ++i)
        {
            payload[i] ^= mask[i%4];
        }
    }
}
