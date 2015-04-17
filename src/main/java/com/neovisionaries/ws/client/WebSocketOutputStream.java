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


import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;


class WebSocketOutputStream extends FilterOutputStream
{
    public WebSocketOutputStream(OutputStream out)
    {
        super(out);
    }


    public void write(String string) throws IOException
    {
        // Convert the string into a byte array.
        byte[] bytes = Misc.getBytesUTF8(string);

        super.write(bytes);
    }
}
