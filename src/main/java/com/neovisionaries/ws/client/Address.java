/*
 * Copyright (C) 2016 Neo Visionaries Inc.
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


import java.net.InetSocketAddress;


class Address
{
    private final String mHost;
    private final int mPort;
    private transient String mString;


    Address(String host, int port)
    {
        mHost = host;
        mPort = port;
    }


    InetSocketAddress toInetSocketAddress()
    {
        return new InetSocketAddress(mHost, mPort);
    }


    String getHostname()
    {
        return mHost;
    }


    @Override
    public String toString()
    {
        if (mString == null)
        {
            mString = String.format("%s:%d", mHost, mPort);
        }

        return mString;
    }
}
