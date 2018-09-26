/**
 * High-quality WebSocket client implementation in Java. This implementation
 *
 * <ul>
 * <li>complies with <a href="http://tools.ietf.org/html/rfc6455">RFC 6455</a> (The WebSocket Protocol),
 * <li>works on Java SE 1.5+ and Android,
 * <li>supports all the frame types (continuation, binary, text, close, ping and pong),
 * <li>provides a method to send a fragmented frame in addition to methods for unfragmented frames,
 * <li>provides a method to get the underlying raw socket of a WebSocket to configure it,
 * <li>provides a method for <a href="http://tools.ietf.org/html/rfc2617">Basic Authentication</a>,
 * <li>provides a factory class which utilizes {@link javax.net.SocketFactory} interface,
 * <li>provides a rich listener interface to hook WebSocket events,
 * <li>has fine-grained error codes for fine-grained controllability on errors,
 * <li>allows to disable validity checks on RSV1/RSV2/RSV3 bits and opcode of frames,
 * <li>supports HTTP proxy, especially "Secure WebSocket" (<code>wss</code>) through
 *     "Secure Proxy" (<code>https</code>),
 * <li>and supports <a href="http://tools.ietf.org/html/rfc7692">RFC 7692</a>
 *     (Compression Extensions for WebSocket), also known as <i>permessage-deflate</i>
 *     (not enabled by default).
 * </ul>
 *
 * <p>
 * See the description of {@link com.neovisionaries.ws.client.WebSocket WebSocket}
 * class for usage. The source code is hosted at
 * <a href="https://github.com/TakahikoKawasaki/nv-websocket-client">GitHub</a>.
 * </p>
 *
 * <p>
 * For Maven:
 * </p>
 * <blockquote>
 * <style type="text/css">
 * span.tag { color: #45818e; }
 * </style>
 * <pre style="margin: 1em; padding: 0.5em; border-left: solid 5px lightgray;">
 * <span class="tag">&lt;dependency&gt;
 *     &lt;groupId&gt;</span>com.neovisionaries<span class="tag">&lt;/groupId&gt;
 *     &lt;artifactId&gt;</span>nv-websocket-client<span class="tag">&lt;/artifactId&gt;
 *     &lt;version&gt;</span>2.6<span class="tag">&lt;/version&gt;
 * &lt;/dependency&gt;</span></pre>
 * </blockquote>
 *
 * @version 2.6
 *
 * @author Takahiko Kawasaki
 */
package com.neovisionaries.ws.client;
