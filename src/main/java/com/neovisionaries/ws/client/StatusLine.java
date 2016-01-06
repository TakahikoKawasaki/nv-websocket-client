/*
 * Copyright (C) 2016 Neo Visionaries Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package com.neovisionaries.ws.client;


/**
 * HTTP status line returned from an HTTP server.
 *
 * @since 1.19
 */
public class StatusLine
{
    /**
     * HTTP version.
     */
    private final String mHttpVersion;


    /**
     * Status code.
     */
    private final int mStatusCode;


    /**
     * Reason phrase.
     */
    private final String mReasonPhrase;


    /**
     * String representation of this instance (= the raw status line).
     */
    private final String mString;


    /**
     * Constructor with a raw status line.
     *
     * @param line
     *         A status line.
     *
     * @throws NullPointerException
     *         {@code line} is {@code null}
     *
     * @throws IllegalArgumentException
     *         The number of elements in {@code line} is less than 2.
     *
     * @throws NumberFormatException
     *         Failed to parse the second element in {@code line}
     *         as an integer.
     */
    StatusLine(String line)
    {
        // HTTP-Version Status-Code Reason-Phrase
        String[] elements = line.split(" +", 3);

        if (elements.length < 2)
        {
            throw new IllegalArgumentException();
        }

        mHttpVersion  = elements[0];
        mStatusCode   = Integer.parseInt(elements[1]);
        mReasonPhrase = (elements.length == 3) ? elements[2] : null;
        mString       = line;
    }


    /**
     * Get the HTTP version.
     *
     * @return
     *         The HTTP version. For example, {@code "HTTP/1.1"}.
     */
    public String getHttpVersion()
    {
        return mHttpVersion;
    }


    /**
     * Get the status code.
     *
     * @return
     *         The status code. For example, {@code 404}.
     */
    public int getStatusCode()
    {
        return mStatusCode;
    }


    /**
     * Get the reason phrase.
     *
     * @return
     *         The reason phrase. For example, {@code "Not Found"}.
     */
    public String getReasonPhrase()
    {
        return mReasonPhrase;
    }


    /**
     * Get the string representation of this instance, which is
     * equal to the raw status line.
     *
     * @return
     *         The raw status line. For example,
     *         {@code "HTTP/1.1 404 Not Found"}.
     */
    @Override
    public String toString()
    {
        return mString;
    }
}
