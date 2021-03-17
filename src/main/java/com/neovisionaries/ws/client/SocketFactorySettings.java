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


import com.neovisionaries.ws.client.pinning.KeyStoreProvider;
import com.neovisionaries.ws.client.pinning.PinningException;
import com.neovisionaries.ws.client.pinning.PinningKeyStore;
import com.neovisionaries.ws.client.pinning.PinningParams;
import com.neovisionaries.ws.client.pinning.PinningTrustManager;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


class SocketFactorySettings
{
    private SocketFactory mSocketFactory;
    private SSLSocketFactory mSSLSocketFactory;
    private SSLContext mSSLContext;

    public SocketFactorySettings() {}


    public SocketFactorySettings(SocketFactorySettings settings)
    {
        mSocketFactory = settings.mSocketFactory;
        mSSLSocketFactory = settings.mSSLSocketFactory;
        mSSLContext = settings.mSSLContext;
    }


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

    public void setKeyStoreProvider(KeyStoreProvider keyStoreProvider, PinningParams params) {
        if (mSSLSocketFactory == null) {
            throw new IllegalArgumentException("KeyStore provider can be passed only SSLSocket facotry is provided");
        }

	    final String[] pins = params.getPins().toArray(new String[params.getPins().size()]);
	    int pinningEnforceTimeout = params.getPinningEnforceTimeout();

	    try {
		    mSSLContext = SSLContext.getDefault();
		    mSSLContext.init(null, new TrustManager[]{new PinningTrustManager(PinningKeyStore.getInstance(keyStoreProvider), pins, pinningEnforceTimeout)}, new SecureRandom());
	    } catch (KeyManagementException e) {
		    throw new PinningException(e);
	    } catch (NoSuchAlgorithmException e) {
		    throw new PinningException(e);
	    }
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
