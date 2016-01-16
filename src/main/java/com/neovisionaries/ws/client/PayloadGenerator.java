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


/**
 * Payload generator.
 *
 * @since 1.20
 *
 * @author Takahiko Kawasaki
 */
public interface PayloadGenerator
{
    /**
     * Generate a payload of a frame.
     *
     * <p>
     * Note that the maximum payload length of control frames
     * (e.g. ping frames) is 125 in bytes. Therefore, the length
     * of a byte array returned from this method must not exceed
     * 125 bytes.
     * </p>
     *
     * @return
     *         A payload of a frame.
     */
    byte[] generate();
}
