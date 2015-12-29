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


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;


/**
 * DEFLATE (<a href="http://tools.ietf.org/html/rfc1951">RFC 1951</a>)
 * compressor implementation.
 */
class DeflateCompressor
{
    public static byte[] compress(byte[] input) throws IOException
    {
        // Destination where compressed data will be stored.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Create a compressor.
        Deflater deflater = createDeflater();
        DeflaterOutputStream dos = new DeflaterOutputStream(baos, deflater);

        // Compress the data.
        //
        // Some other implementations such as Jetty and Tyrus use
        // Deflater.deflate(byte[], int, int, int) with Deflate.SYNC_FLUSH,
        // but this implementation does not do it intentionally because the
        // method and the constant value are not available before Java 7.
        dos.write(input, 0, input.length);
        dos.close();

        // Release the resources held by the compressor.
        deflater.end();

        // Retrieve the compressed data.
        return baos.toByteArray();
    }


    private static Deflater createDeflater()
    {
        // The second argument (nowrap) is true to get only DEFLATE
        // blocks without the ZLIB header and checksum fields.
        return new Deflater(Deflater.DEFAULT_COMPRESSION, true);
    }
}
