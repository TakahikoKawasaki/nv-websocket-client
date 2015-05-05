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


import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;


class SocketFactorySettings
{
    private SocketFactory mSocketFactory;
    private SSLSocketFactory mSSLSocketFactory;
    private SSLContext mSSLContext;


    public SocketFactory getSocketFactory()
    {
        return mSocketFactory;
    }


    public void setSocketFactory(SocketFactory factory)
    {
        mSocketFactory = factory;
    }


    public SSLSocketFactory getSSLSocketFactory()
    {
        return mSSLSocketFactory;
    }


    public void setSSLSocketFactory(SSLSocketFactory factory)
    {
        mSSLSocketFactory = factory;
    }


    public SSLContext getSSLContext()
    {
        return mSSLContext;
    }


    public void setSSLContext(SSLContext context)
    {
        mSSLContext = context;
    }


    public SocketFactory selectSocketFactory(boolean secure)
    {
        if (secure)
        {
            if (mSSLContext != null)
            {
                return mSSLContext.getSocketFactory();
            }

            if (mSSLSocketFactory != null)
            {
                return mSSLSocketFactory;
            }

            return SSLSocketFactory.getDefault();
        }

        if (mSocketFactory != null)
        {
            return mSocketFactory;
        }

        return SocketFactory.getDefault();
    }
}
