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


import java.util.LinkedHashMap;
import java.util.Map;


/**
 * A class to hold the name and the parameters of
 * a web socket extension.
 */
public class WebSocketExtension
{
    private final String mName;
    private final Map<String, String> mParameters;


    /**
     * Constructor with an extension name.
     *
     * @param name
     *         The extension name.
     *
     * @throws IllegalArgumentException
     *         The given name is not a valid token.
     */
    public WebSocketExtension(String name)
    {
        // Check the validity of the name.
        if (Token.isValid(name) == false)
        {
            // The name is not a valid token.
            throw new IllegalArgumentException("'name' is not a valid token.");
        }

        mName       = name;
        mParameters = new LinkedHashMap<String, String>();
    }


    /**
     * Copy constructor.
     *
     * @param source
     *         A source extension. Must not be {@code null}.
     *
     * @throws IllegalArgumentException
     *         The given argument is {@code null}.
     *
     * @since 1.6
     */
    public WebSocketExtension(WebSocketExtension source)
    {
        if (source == null)
        {
            // If the given instance is null.
            throw new IllegalArgumentException("'source' is null.");
        }

        mName       = source.getName();
        mParameters = new LinkedHashMap<String, String>(source.getParameters());
    }


    /**
     * Get the extension name.
     *
     * @return
     *         The extension name.
     */
    public String getName()
    {
        return mName;
    }


    /**
     * Get the parameters.
     *
     * @return
     *         The parameters.
     */
    public Map<String, String> getParameters()
    {
        return mParameters;
    }


    /**
     * Check if the parameter identified by the key is contained.
     *
     * @param key
     *         The name of the parameter.
     *
     * @return
     *         {@code true} if the parameter is contained.
     */
    public boolean containsParameter(String key)
    {
        return mParameters.containsKey(key);
    }


    /**
     * Get the value of the specified parameter.
     *
     * @param key
     *         The name of the parameter.
     *
     * @return
     *         The value of the parameter. {@code null} may be returned.
     */
    public String getParameter(String key)
    {
        return mParameters.get(key);
    }


    /**
     * Set a value to the specified parameter.
     *
     * @param key
     *         The name of the parameter.
     *
     * @param value
     *         The value of the parameter. If not {@code null}, it must be
     *         a valid token. Note that <a href="http://tools.ietf.org/html/rfc6455"
     *         >RFC 6455</a> says "<i>When using the quoted-string syntax
     *         variant, the value after quoted-string unescaping MUST
     *         conform to the 'token' ABNF.</i>"
     *
     * @return
     *         {@code this} object.
     *
     * @throws IllegalArgumentException
     *         <ul>
     *         <li>The key is not a valid token.
     *         <li>The value is not {@code null} and it is not a valid token.
     *         </ul>
     */
    public WebSocketExtension setParameter(String key, String value)
    {
        // Check the validity of the key.
        if (Token.isValid(key) == false)
        {
            // The key is not a valid token.
            throw new IllegalArgumentException("'key' is not a valid token.");
        }

        // If the value is not null.
        if (value != null)
        {
            // Check the validity of the value.
            if (Token.isValid(value) == false)
            {
                // The value is not a valid token.
                throw new IllegalArgumentException("'value' is not a valid token.");
            }
        }

        mParameters.put(key, value);

        return this;
    }


    /**
     * Stringify this object into the format "{name}[; {key}[={value}]]*".
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(mName);

        for (Map.Entry<String, String> entry : mParameters.entrySet())
        {
            // "; {key}"
            builder.append("; ").append(entry.getKey());

            String value = entry.getValue();

            if (value != null && value.length() != 0)
            {
                // "={value}"
                builder.append("=").append(value);
            }
        }

        return builder.toString();
    }


    public static WebSocketExtension parse(String string)
    {
        if (string == null)
        {
            return null;
        }

        // Split the string by semi-colons.
        String[] elements = string.trim().split("\\s*;\\s*");

        if (elements.length == 0)
        {
            // Even an extension name is not included.
            return null;
        }

        // The first element is the extension name.
        String name = elements[0];

        if (Token.isValid(name) == false)
        {
            // The extension name is not a valid token.
            return null;
        }

        // The first element is the extension name.
        WebSocketExtension extension = new WebSocketExtension(name);

        // For each "{key}[={value}]".
        for (int i = 1; i < elements.length; ++i)
        {
            // Split by '=' to get the key and the value.
            String[] pair = elements[i].split("\\s*=\\s*", 2);

            // If {key} is not contained.
            if (pair.length == 0 || pair[0].length() == 0)
            {
                // Ignore.
                continue;
            }

            // The name of the parameter.
            String key = pair[0];

            if (Token.isValid(key) == false)
            {
                // The parameter name is not a valid token.
                // Ignore this parameter.
                continue;
            }

            // The value of the parameter.
            String value = extractValue(pair);

            if (value != null)
            {
                if (Token.isValid(value) == false)
                {
                    // The parameter value is not a valid token.
                    // Ignore this parameter.
                    continue;
                }
            }

            // Add the pair of the key and the value.
            extension.setParameter(key, value);
        }

        return extension;
    }


    private static String extractValue(String[] pair)
    {
        if (pair.length != 2)
        {
            return null;
        }

        return Token.unquote(pair[1]);
    }
}
