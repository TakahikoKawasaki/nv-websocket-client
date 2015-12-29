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
 * Per-Message Compression Extension (<a href="https://tools.ietf.org/html/rfc7692">RFC 7692</a>).
 *
 * @see <a href="https://tools.ietf.org/html/rfc7692">RFC 7692</a>
 */
abstract class PerMessageCompressionExtension extends WebSocketExtension
{
    public PerMessageCompressionExtension(String name)
    {
        super(name);
    }


    public PerMessageCompressionExtension(WebSocketExtension source)
    {
        super(source);
    }


    /**
     * Decompress the compressed message.
     */
    protected abstract byte[] decompress(byte[] compressed) throws WebSocketException;


    /**
     * Compress the plain message.
     */
    protected abstract byte[] compress(byte[] plain) throws WebSocketException;
}
