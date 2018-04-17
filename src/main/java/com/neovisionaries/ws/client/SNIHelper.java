/*
 * Copyright (C) 2018 Neo Visionaries Inc.
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


import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;


class SNIHelper
{
    private static Constructor<?> sSNIHostNameConstructor;
    private static Method sSetServerNamesMethod;


    static
    {
        try
        {
            initialize();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private static void initialize() throws Exception
    {
        // Constructor which represents javax.net.ssl.SNIHostName(String).
        // The class is available since Java 1.8 / Android API Level 24 (Android 7.0)
        sSNIHostNameConstructor = Misc.getConstructor(
                "javax.net.ssl.SNIHostName", new Class<?>[] { String.class });

        // Method which represents javax.net.ssl.SSLParameters.setServerNames(List<SNIServerName>).
        // The method is available since Java 1.8 / Android API Level 24 (Android 7.0)
        sSetServerNamesMethod = Misc.getMethod(
                "javax.net.ssl.SSLParameters", "setServerNames", new Class<?>[] { List.class });
    }


    private static Object createSNIHostName(String hostname)
    {
        // return new SNIHostName(hostname);
        return Misc.newInstance(sSNIHostNameConstructor, hostname);
    }


    private static List<Object> createSNIHostNames(String[] hostnames)
    {
        List<Object> list = new ArrayList<Object>(hostnames.length);

        // Create a list of SNIHostName from the String array.
        for (String hostname : hostnames)
        {
            // Create a new SNIHostName instance and add it to the list.
            list.add(createSNIHostName(hostname));
        }

        return list;
    }


    private static void setServerNames(SSLParameters parameters, String[] hostnames)
    {
        // Call parameters.setServerNames(List<SNIServerName>) method.
        Misc.invoke(sSetServerNamesMethod, parameters, createSNIHostNames(hostnames));
    }


    static void setServerNames(Socket socket, String[] hostnames)
    {
        if ((socket instanceof SSLSocket) == false)
        {
            return;
        }

        if (hostnames == null)
        {
            return;
        }

        SSLParameters parameters = ((SSLSocket)socket).getSSLParameters();
        if (parameters == null)
        {
            return;
        }

        // Call SSLParameters.setServerNames(List<SNIServerName>) method.
        setServerNames(parameters, hostnames);
    }
}
