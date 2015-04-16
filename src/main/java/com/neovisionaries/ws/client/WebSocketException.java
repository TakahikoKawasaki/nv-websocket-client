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


/**
 * Web socket exception.
 */
public class WebSocketException extends Exception
{
    private static final long serialVersionUID = 1L;
    private final WebSocketError mError;


    public WebSocketException(WebSocketError error)
    {
        mError = error;
    }


    public WebSocketException(WebSocketError error, String message)
    {
        super(message);

        mError = error;
    }


    public WebSocketException(WebSocketError error, Throwable cause)
    {
        super(cause);

        mError = error;
    }


    public WebSocketException(WebSocketError error, String message, Throwable cause)
    {
        super(message, cause);

        mError = error;
    }


    public WebSocketError getError()
    {
        return mError;
    }
}
