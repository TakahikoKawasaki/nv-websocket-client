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


class WebSocketFrame
{
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
