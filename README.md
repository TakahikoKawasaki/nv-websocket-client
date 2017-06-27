nv-websocket-client
===================

Overview
--------

High-quality WebSocket client implementation in Java which

- complies with [RFC 6455](http://tools.ietf.org/html/rfc6455) (The WebSocket Protocol),
- works on Java SE 1.5+ and Android,
- supports all the frame types (continuation, binary, text, close, ping and pong),
- provides a method to send a fragmented frame in addition to methods for unfragmented frames,
- provides a method to get the underlying raw socket of a WebSocket to configure it,
- provides a method for [Basic Authentication](http://tools.ietf.org/html/rfc2617),
- provides a factory class which utilizes javax.net.SocketFactory interface,
- provides a rich listener interface to hook WebSocket events,
- has fine-grained error codes for fine-grained controllability on errors,
- allows to disable validity checks on RSV1/RSV2/RSV3 bits and opcode of frames,
- supports HTTP proxy, especially "Secure WebSocket" (`wss`) through "Secure Proxy" (`https`),
- and supports [RFC 7692](http://tools.ietf.org/html/rfc7692) (Compression Extensions for WebSocket),
  also known as _permessage-deflate_ (not enabled by default).


License
-------

Apache License, Version 2.0


Maven
-----

```xml
<dependency>
    <groupId>com.neovisionaries</groupId>
    <artifactId>nv-websocket-client</artifactId>
    <version>2.3</version>
</dependency>
```


Gradle
------

```Gradle
dependencies {
    compile 'com.neovisionaries:nv-websocket-client:2.3'
}
```


OSGi
----

    Bundle-SymbolicName: com.neovisionaries.ws.client
    Export-Package: com.neovisionaries.ws.client;version="2.3.0"


Source Code
-----------

  <code>https://github.com/TakahikoKawasaki/nv-websocket-client.git</code>


JavaDoc
-------

  <code>http://TakahikoKawasaki.github.io/nv-websocket-client/</code>


Description
-----------

#### Create WebSocketFactory

`WebSocketFactory` is a factory class that creates `WebSocket` instances. The
first step is to create a `WebSocketFactory` instance.

```java
// Create a WebSocketFactory instance.
WebSocketFactory factory = new WebSocketFactory();
```

By default, `WebSocketFactory` uses `SocketFactory.getDefault()` for non-secure
WebSocket connections (`ws:`) and `SSLSocketFactory.getDefault()` for secure
WebSocket connections (`wss:`). You can change this default behavior by using
`WebSocketFactory.setSocketFactory` method, `WebSocketFactory.setSSLSocketFactory`
method and `WebSocketFactory.setSSLContext` method. Note that you don't have to
call a `setSSL*` method at all if you use the default SSL configuration. Also
note that calling `setSSLSocketFactory` method has no meaning if you have called
`setSSLContext` method. See the
[description](http://takahikokawasaki.github.io/nv-websocket-client/com/neovisionaries/ws/client/WebSocketFactory.html#createSocket-java.net.URI-)
of `WebSocketFactory.createSocket(URI)` method for details.

The following is an example to set a custom SSL context to a `WebSocketFactory`
instance. (Again, you don't have to call a `setSSL*` method if you use the default
SSL configuration.)

```java
// Create a custom SSL context.
SSLContext context = NaiveSSLContext.getInstance("TLS");

// Set the custom SSL context.
factory.setSSLContext(context);

// Disable manual hostname verification for NaiveSSLContext.
//
// Manual hostname verification has been enabled since the
// version 2.1. Because the verification is executed manually
// after Socket.connect(SocketAddress, int) succeeds, the
// hostname verification is always executed even if you has
// passed an SSLContext which naively accepts any server
// certificate. However, this behavior is not desirable in
// some cases and you may want to disable the hostname
// verification. You can disable the hostname verification
// by calling WebSocketFactory.setVerifyHostname(false).
factory.setVerifyHostname(false);
```

[NaiveSSLContext](https://gist.github.com/TakahikoKawasaki/d07de2218b4b81bf65ac)
used in the above example is a factory class to create an `SSLContext` which
naively accepts all certificates without verification. It's enough for testing
purposes. When you see an error message "unable to find valid certificate path
to requested target" while testing, try `NaiveSSLContext`.


#### HTTP Proxy

If a WebSocket endpoint needs to be accessed via an HTTP proxy, information
about the proxy server has to be set to a `WebSocketFactory` instance before
creating a `WebSocket` instance. Proxy settings are represented by
`ProxySettings` class. A `WebSocketFactory` instance has an associated
`ProxySettings` instance and it can be obtained by calling
`WebSocketFactory.getProxySettings()` method.

```java
// Get the associated ProxySettings instance.
ProxySettings settings = factory.getProxySettings();
```

`ProxySettings` class has methods to set information about a proxy server such
as `setHost` method and `setPort` method. The following is an example to set a
secure (`https`) proxy server.

```java
// Set a proxy server.
settings.setServer("https://proxy.example.com");
```

If credentials are required for authentication at a proxy server, `setId`
method and `setPassword` method, or `setCredentials` method can be used to set
the credentials. Note that, however, the current implementation supports only
Basic Authentication.

```java
// Set credentials for authentication at a proxy server.
settings.setCredentials(id, password);
```


#### Create WebSocket

`WebSocket` class represents a WebSocket. Its instances are created by calling
one of `createSocket` methods of a `WebSocketFactory` instance. Below is the
simplest example to create a `WebSocket` instance.

```java
// Create a WebSocket. The scheme part can be one of the following:
// 'ws', 'wss', 'http' and 'https' (case-insensitive). The user info
// part, if any, is interpreted as expected. If a raw socket failed
// to be created, an IOException is thrown.
WebSocket ws = new WebSocketFactory().createSocket("ws://localhost/endpoint");
```

There are two ways to set a timeout value for socket connection. The first way
is to call `setConnectionTimeout(int timeout)` method of `WebSocketFactory`.

```java
// Create a WebSocket factory and set 5000 milliseconds as a timeout
// value for socket connection.
WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(5000);

// Create a WebSocket. The timeout value set above is used.
WebSocket ws = factory.createSocket("ws://localhost/endpoint");
```

The other way is to give a timeout value to a `createSocket` method.

```java
// Create a WebSocket factory. The timeout value remains 0.
WebSocketFactory factory = new WebSocketFactory();

// Create a WebSocket with a socket connection timeout value.
WebSocket ws = factory.createSocket("ws://localhost/endpoint", 5000);
```

The timeout value is passed to `connect(SocketAddress, int)` method of
`java.net.Socket`.


#### Register Listener

After creating a `WebSocket` instance, you should call `addListener` method
to register a `WebSocketListener` that receives WebSocket events.
`WebSocketAdapter` is an empty implementation of `WebSocketListener` interface.

```java
// Register a listener to receive WebSocket events.
ws.addListener(new WebSocketAdapter() {
    @Override
    public void onTextMessage(WebSocket websocket, String message) throws Exception {
        // Received a text message.
        ......
    }
});
```

The table below is the list of callback methods defined in `WebSocketListener`
interface.

| Method                        | Description                                          |
|:------------------------------|:-----------------------------------------------------|
| `handleCallbackError`         | Called when an `onXxx()` method threw a `Throwable`. |
| `onBinaryFrame`               | Called when a binary frame was received.             |
| `onBinaryMessage`             | Called when a binary message was received.           |
| `onCloseFrame`                | Called when a close frame was received.              |
| `onConnected`                 | Called after the opening handshake succeeded.        |
| `onConnectError`              | Called when `connectAsynchronously()` failed.        |
| `onContinuationFrame`         | Called when a continuation frame was received.       |
| `onDisconnected`              | Called after a WebSocket connection was closed.      |
| `onError`                     | Called when an error occurred.                       |
| `onFrame`                     | Called when a frame was received.                    |
| `onFrameError`                | Called when a frame failed to be read.               |
| `onFrameSent`                 | Called when a frame was sent.                        |
| `onFrameUnsent`               | Called when a frame was not sent.                    |
| `onMessageDecompressionError` | Called when a message failed to be decompressed.     |
| `onMessageError`              | Called when a message failed to be constructed.      |
| `onPingFrame`                 | Called when a ping frame was received.               |
| `onPongFrame`                 | Called when a pong frame was received.               |
| `onSendError`                 | Called when an error occurred on sending a frame.    |
| `onSendingFrame`              | Called before a frame is sent.                       |
| `onSendingHandshake`          | Called before an opening handshake is sent.          |
| `onStateChanged`              | Called when the state of WebSocket changed.          |
| `onTextFrame`                 | Called when a text frame was received.               |
| `onTextMessage`               | Called when a text message was received.             |
| `onTextMessageError`          | Called when a text message failed to be constructed. |
| `onThreadCreated`             | Called after a thread was created.                   |
| `onThreadStarted`             | Called at the beginning of a thread's run() method.  |
| `onThreadStopping`            | Called at the end of a thread's run() method.        |
| `onUnexpectedError`           | Called when an uncaught throwable was detected.      |


#### Configure WebSocket

Before starting a WebSocket
[opening handshake](http://tools.ietf.org/html/rfc6455#section-4)
with the server, you can
configure the WebSocket instance by using the following methods.

| METHOD              | DESCRIPTION                                             |
|---------------------|---------------------------------------------------------|
| `addProtocol`       | Adds an element to `Sec-WebSocket-Protocol`.            |
| `addExtension`      | Adds an element to `Sec-WebSocket-Extensions`.          |
| `addHeader`         | Adds an arbitrary HTTP header.                          |
| `setUserInfo`       | Adds `Authorization` header for Basic Authentication.   |
| `getSocket`         | Gets the underlying `Socket` instance to configure it.  |
| `setExtended`       | Disables validity checks on RSV1/RSV2/RSV3 and opcode.  |
| `setFrameQueueSize` | Set the size of the frame queue for congestion control. |
| `setMaxPayloadSize` | Set the maximum payload size.                           |
| `setMissingCloseFrameAllowed` | Set to whether to allow the server to close the connection without sending a close frame. |


#### Connect To Server

By calling `connect()` method, connection to the server is established and a
WebSocket opening handshake is performed synchronously. If an error occurred
during the handshake, a `WebSocketException` would be thrown. Instead, if
the handshake succeeds, the `connect()` implementation creates threads and
starts them to read and write WebSocket frames asynchronously.

```java
try
{
    // Connect to the server and perform an opening handshake.
    // This method blocks until the opening handshake is finished.
    ws.connect();
}
catch (OpeningHandshakeException e)
{
    // A violation against the WebSocket protocol was detected
    // during the opening handshake.
}
catch (HostnameUnverifiedException e)
{
    // The certificate of the peer does not match the expected hostname.
}
catch (WebSocketException e)
{
    // Failed to establish a WebSocket connection.
}
```

In some cases, `connect()` method throws `OpeningHandshakeException` which
is a subclass of `WebSocketException` (since version 1.19).
`OpeningHandshakeException` provides additional methods such as
`getStatusLine()`, `getHeaders()` and `getBody()` to access the response
from a server. The following snippet is an example to print information
that the exception holds.

```java
catch (OpeningHandshakeException e)
{
    // Status line.
    StatusLine sl = e.getStatusLine();
    System.out.println("=== Status Line ===");
    System.out.format("HTTP Version  = %s\n", sl.getHttpVersion());
    System.out.format("Status Code   = %d\n", sl.getStatusCode());
    System.out.format("Reason Phrase = %s\n", sl.getReasonPhrase());

    // HTTP headers.
    Map<String, List<String>> headers = e.getHeaders();
    System.out.println("=== HTTP Headers ===");
    for (Map.Entry<String, List<String>> entry : headers.entrySet())
    {
        // Header name.
        String name = entry.getKey();

        // Values of the header.
        List<String> values = entry.getValue();

        if (values == null || values.size() == 0)
        {
            // Print the name only.
            System.out.println(name);
            continue;
        }

        for (String value : values)
        {
            // Print the name and the value.
            System.out.format("%s: %s\n", name, value);
        }
    }
}
```

Also, `connect()` method throws `HostnameUnverifiedException` which is a
subclass of `WebSocketException` (since version 2.1) when the certificate
of the peer does not match the expected hostname.


#### Connect To Server Asynchronously

The simplest way to call `connect()` method asynchronously is to use
`connectAsynchronously()` method. The implementation of the method creates
a thread and calls `connect()` method in the thread. When the `connect()`
call failed, `onConnectError()` of `WebSocketListener` would be called.
Note that `onConnectError()` is called only when `connectAsynchronously()`
was used and the `connect()` call executed in the background thread failed.
Neither direct synchronous `connect()` nor `connect(ExecutorService)`
(described below) will trigger the callback method.

```java
// Connect to the server asynchronously.
ws.connectAsynchronously();
```

Another way to call `connect()` method asynchronously is to use
`connect(ExecutorService)` method. The method performs a WebSocket opening
handshake asynchronously using the given `ExecutorService`.

```java
// Prepare an ExecutorService.
ExecutorService es = Executors.newSingleThreadExecutor();

// Connect to the server asynchronously.
Future<WebSocket> future = ws.connect(es);

try
{
    // Wait for the opening handshake to complete.
    future.get();
}
catch (ExecutionException e)
{
    if (e.getCause() instanceof WebSocketException)
    {
        ......
    }
}
```

The implementation of `connect(ExecutorService)` method creates a
`Callable<WebSocket>` instance by calling `connectable()` method and
passes the instance to `submit(Callable)` method of the given
`ExecutorService`. What the implementation of `call()` method of the
`Callable` instance does is just to call the synchronous `connect()`.


#### Send Frames

WebSocket frames can be sent by `sendFrame` method. Other `sendXxx`
methods such as `sendText` are aliases of `sendFrame` method. All of
the `sendXxx` methods work asynchronously. However, under some
conditions, `sendXxx` methods may block. See Congestion Control for
details.

Below are some examples of `sendXxx` methods. Note that in normal
cases, you don't have to call `sendClose` method and `sendPong`
method (or their variants) explicitly because they are called
automatically when appropriate.

```java
// Send a text frame.
ws.sendText("Hello.");

// Send a binary frame.
byte[] binary = ......;
ws.sendBinary(binary);

// Send a ping frame.
ws.sendPing("Are you there?");
```

If you want to send fragmented frames, you have to know the details of the
specification ([5.4. Fragmentation](https://tools.ietf.org/html/rfc6455#section-5.4)).
Below is an example to send a text message (`"How are you?"`) which consists
of 3 fragmented frames.

```java
// The first frame must be either a text frame or a binary frame.
// And its FIN bit must be cleared.
WebSocketFrame firstFrame = WebSocketFrame
    .createTextFrame("How ")
    .setFin(false);

// Subsequent frames must be continuation frames. The FIN bit of
// all continuation frames except the last one must be cleared.
// Note that the FIN bit of frames returned from
// WebSocketFrame.createContinuationFrame() method is cleared,
// so the example below does not clear the FIN bit explicitly.
WebSocketFrame secondFrame = WebSocketFrame
    .createContinuationFrame("are ");

// The last frame must be a continuation frame with the FIN bit
// set. Note that the FIN bit of frames returned from
// WebSocketFrame.createContinuationFrame methods is cleared,
// so the FIN bit of the last frame must be set explicitly.
WebSocketFrame lastFrame = WebSocketFrame
    .createContinuationFrame("you?")
    .setFin(true);

// Send a text message which consists of 3 frames.
ws.sendFrame(firstFrame)
  .sendFrame(secondFrame)
  .sendFrame(lastFrame);
```

Alternatively, the same as above can be done like this.

```java
ws.sendText("How ", false)
  .sendContinuation("are ")
  .sendContinuation("you?", true);
```


#### Send Ping/Pong Frames Periodically

You can send ping frames periodically by calling `setPingInterval` method
with an interval in milliseconds between ping frames. This method can be
called both before and after `connect()` method. Passing zero stops the
periodical sending.

```java
// Send a ping per 60 seconds.
ws.setPingInterval(60 * 1000);

// Stop the periodical sending.
ws.setPingInterval(0);
```

Likewise, you can send pong frames periodically by calling `setPongInterval`
method. "_A Pong frame MAY be sent **unsolicited**._"
([RFC 6455, 5.5.3. Pong](https://tools.ietf.org/html/rfc6455#section-5.5.3))

You can customize payload of ping/pong frames that are sent automatically
by using `setPingPayloadGenerator()` and `setPongPayloadGenerator()` methods.
Both methods take an instance of `PayloadGenerator` interface. The following
is an example to use the string representation of the current date as payload
of ping frames.

```java
ws.setPingPayloadGenerator(new PayloadGenerator() {
    @Override
    public byte[] generate() {
        // The string representation of the current date.
        return new Date().toString().getBytes();
    }
});
```

Note that the maximum payload length of control frames (e.g. ping frames)
is 125. Therefore, the length of a byte array returned from `generate()`
method must not exceed 125.


#### Auto Flush

By default, a frame is automatically flushed to the server immediately
after `sendFrame` method is executed. This automatic flush can be disabled
by calling `setAutoFlush(false)`.

```java
// Disable auto-flush.
ws.setAutoFlush(false);
```

To flush frames manually, call `flush()` method. Note that this method
works asynchronously.

```java
// Flush frames to the server manually.
ws.flush();
```


#### Congestion Control

`sendXxx` methods queue a `WebSocketFrame` instance to the internal queue.
By default, no upper limit is imposed on the queue size, so `sendXxx`
methods do not block. However, this behavior may cause a problem if your
WebSocket client application sends too many WebSocket frames in a short
time for the WebSocket server to process. In such a case, you may want
`sendXxx` methods to block when many frames are queued.

You can set an upper limit on the internal queue by calling
`setFrameQueueSize(int)` method. As a result, if the number of frames
in the queue has reached the upper limit when a `sendXxx` method is called,
the method blocks until the queue gets spaces. The code snippet below is
an example to set 5 as the upper limit of the internal frame queue.

```java
// Set 5 as the frame queue size.
ws.setFrameQueueSize(5);
```

Note that under some conditions, even if the queue is full, `sendXxx`
methods do not block. For example, in the case where the thread to send
frames (`WritingThread`) is going to stop or has already stopped. In
addition, method calls to send a
[control frame](https://tools.ietf.org/html/rfc6455#section-5.5) (e.g.
`sendClose()` and `sendPing()`) do not block.


#### Maximum Payload Size

You can set an upper limit on the payload size of WebSocket frames by
calling `setMaxPayloadSize(int)` method with a positive value. Text, binary
and continuation frames whose payload size is bigger than the maximum payload
size you have set will be split into multiple frames.

```java
// Set 1024 as the maximum payload size.
ws.setMaxPayloadSize(1024);
```

Control frames (close, ping and pong frames) are never split as per the
specification.

If permessage-deflate extension is enabled and if the payload size of a
WebSocket frame after compression does not exceed the maximum payload size,
the WebSocket frame is not split even if the payload size before compression
exceeds the maximum payload size.


#### Compression

The **permessage-deflate** extension ([RFC 7692](http://tools.ietf.org/html/rfc7692))
has been supported since the version 1.17. To enable the extension, call
`addExtension` method with `permessage-deflate`.

```java
// Enable "permessage-deflate" extension (RFC 7692).
ws.addExtension(WebSocketExtension.PERMESSAGE_DEFLATE);
```


#### Missing Close Frame

Some server implementations close a WebSocket connection without sending a
close frame to a client in some cases. Strictly speaking, this is a violation
against the specification ([RFC 6455](http://tools.ietf.org/html/rfc6455)).
However, this library has allowed the behavior by default since the version
1.29. Even if the end of the input stream of a WebSocket connection were
reached without a close frame being received, it would trigger neither
`onError` method nor `onFrameError` method of `WebSocketListener`. If you
want to make a `WebSocket` instance report an error in the case, pass `false`
to `setMissingCloseFrameAllowed` method.

```java
// Make this library report an error when the end of the input stream
// of the WebSocket connection is reached before a close frame is read.
ws.setMissingCloseFrameAllowed(false);
```


#### Disconnect WebSocket

Before a WebSocket is closed, a closing handshake is performed. A closing
handshake is started (1) when the server sends a close frame to the client
or (2) when the client sends a close frame to the server. You can start a
closing handshake by calling `disconnect()` method (or by sending a close
frame manually).

```java
// Close the WebSocket connection.
ws.disconnect();
```

`disconnect()` method has some variants. If you want to change the close
code and the reason phrase of the close frame that this client will send
to the server, use a variant method such as `disconnect(int, String)`.
`disconnect()` itself is an alias of
`disconnect(WebSocketCloseCode.NORMAL, null)`.


#### Reconnection

`WebSocket.connect()` method can be called at most only once regardless of
whether the method succeeded or failed. If you want to re-connect to the
WebSocket endpoint, you have to create a new `WebSocket` instance again
by calling one of `createSocket` methods of a `WebSocketFactory`. You may
find `recreate()` method useful if you want to create a new `WebSocket`
instance that has the same settings as the original instance. Note that,
however, settings you made on the raw socket of the original `WebSocket`
instance are not copied.

```java
// Create a new WebSocket instance and connect to the same endpoint.
ws = ws.recreate().connect();
```

There is a variant of `recreate()` method that takes a timeout value for
socket connection. If you want to use a timeout value that is different
from the one used when the existing `WebSocket` instance was created,
use `recreate(int timeout)` method.

Note that you should not trigger reconnection in `onError()` method because
`onError()` may be called multiple times due to one error. Instead,
`onDisconnected()` is the right place to trigger reconnection.

Also note that the reason I use an expression of "to trigger reconnection"
instead of "to call `recreate().connect()`" is that I myself won't do it
_synchronously_ in `WebSocketListener` callback methods but will just
schedule reconnection or will just go to the top of a kind of _application
loop_ that repeats to establish a WebSocket connection until it succeeds.


#### Error Handling

`WebSocketListener` has some `onXxxError()` methods such as `onFrameError()`
and `onSendError()`. Among such methods, `onError()` is a special one. It
is always called before any other `onXxxError()` is called. For example,
in the implementation of `run()` method of `ReadingThread`, `Throwable` is
caught and `onError()` and `onUnexpectedError()` are called in this order.
The following is the implementation.

```java
@Override
public void run()
{
    try
    {
        main();
    }
    catch (Throwable t)
    {
        // An uncaught throwable was detected in the reading thread.
        WebSocketException cause = new WebSocketException(
            WebSocketError.UNEXPECTED_ERROR_IN_READING_THREAD,
            "An uncaught throwable was detected in the reading thread", t);

        // Notify the listeners.
        ListenerManager manager = mWebSocket.getListenerManager();
        manager.callOnError(cause);
        manager.callOnUnexpectedError(cause);
    }
}
```

So, you can handle all error cases in `onError()` method. However, note that
`onError()` may be called multiple times for one error cause, so don't try to
trigger reconnection in `onError()`. Instead, `onDisconnected()` is the right
place to trigger reconnection.

All `onXxxError()` methods receive a `WebSocketException` instance as the
second argument (the first argument is a `WebSocket` instance). The exception
class provides `getError()` method which returns a `WebSocketError` enum entry.
Entries in `WebSocketError` enum are possible causes of errors that may occur
in the implementation of this library. The error causes are so granular that
they can make it easy for you to find the root cause when an error occurs.

`Throwable`s thrown by implementations of `onXxx()` callback methods are
passed to `handleCallbackError()` of `WebSocketListener`.

```java
public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
    // Throwables thrown by onXxx() callback methods come here.
}
```


#### Thread Callbacks

Some threads are created internally in the implementation of `WebSocket`.
Known threads are as follows.

| Thread Type      | Description                                                 |
|:-----------------|:------------------------------------------------------------|
| `READING_THREAD` | A thread which reads WebSocket frames from the server.      |
| `WRITING_THREAD` | A thread which sends WebSocket frames to the server.        |
| `CONNECT_THREAD` | A thread which calls `WebSocket.connect()` asynchronously.  |
| `FINISH_THREAD`  | A thread which does finalization of a `WebSocket` instance. |

The following callack methods of `WebSocketListener` are called according
to the life cycle of the threads.

| Method               | Description                                             |
|:---------------------|:--------------------------------------------------------|
| `onThreadCreated()`  | Called after a thread was created.                      |
| `onThreadStarted()`  | Called at the beginning of the thread's `run()` method. |
| `onThreadStopping()` | Called at the end of the thread's `run()` method.       |

For example, if you want to change the name of the reading thread,
implement `onThreadCreated()` method like below.

```java
@Override
public void onThreadCreated(WebSocket ws, ThreadType type, Thread thread)
{
    if (type == ThreadType.READING_THREAD)
    {
        thread.setName("READING_THREAD");
    }
}
```


Sample Application
------------------

The following is a sample application that connects to the echo server on
[websocket.org](https://www.websocket.org) (`ws://echo.websocket.org`) and
repeats to (1) read a line from the standard input, (2) send the read line
to the server and (3) prints the response from the server, until `exit` is
entered. The source code can be downloaded from
[Gist](https://gist.github.com/TakahikoKawasaki/e79d36bf91bf9508ddd2).

```java
import java.io.*;
import com.neovisionaries.ws.client.*;


public class EchoClient
{
    /**
     * The echo server on websocket.org.
     */
    private static final String SERVER = "ws://echo.websocket.org";

    /**
     * The timeout value in milliseconds for socket connection.
     */
    private static final int TIMEOUT = 5000;


    /**
     * The entry point of this command line application.
     */
    public static void main(String[] args) throws Exception
    {
        // Connect to the echo server.
        WebSocket ws = connect();

        // The standard input via BufferedReader.
        BufferedReader in = getInput();

        // A text read from the standard input.
        String text;

        // Read lines until "exit" is entered.
        while ((text = in.readLine()) != null)
        {
            // If the input string is "exit".
            if (text.equals("exit"))
            {
                // Finish this application.
                break;
            }

            // Send the text to the server.
            ws.sendText(text);
        }

        // Close the WebSocket.
        ws.disconnect();
    }


    /**
     * Connect to the server.
     */
    private static WebSocket connect() throws IOException, WebSocketException
    {
        return new WebSocketFactory()
            .setConnectionTimeout(TIMEOUT)
            .createSocket(SERVER)
            .addListener(new WebSocketAdapter() {
                // A text message arrived from the server.
                public void onTextMessage(WebSocket websocket, String message) {
                    System.out.println(message);
                }
            })
            .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
            .connect();
    }


    /**
     * Wrap the standard input with BufferedReader.
     */
    private static BufferedReader getInput() throws IOException
    {
        return new BufferedReader(new InputStreamReader(System.in));
    }
}
```


Limitations
-----------

* According to the specification ([RFC 6455](https://tools.ietf.org/html/rfc6455)),
  the maximum length of the payload part of a frame is (2^63 - 1), but this
  library cannot treat frames whose payload length is greater than (2^31 - 1).

* HTTP response codes other than "101 Switching Protocols" from a WebSocket
  endpoint are not supported. Note that this means redirection (3xx) is not
  supported.

* Currently, by default, `permessage-deflate` extension is not enabled.

* As for [RFC 7692](http://tools.ietf.org/html/rfc7692). The current implementation
  does not support context takeover on the client side. Also, if the agreed size of
  the sliding window on the client side is less than the maximum size allowed by the
  specification (32,768) (this happens when a WebSocket server returns
  `client_max_window_bits` parameter with a value that is less than 15), outgoing
  frames are not compressed when the payload size before compression is bigger than
  the agreed sliding window size.


See Also
--------

- [RFC 6455](http://tools.ietf.org/html/rfc6455) The WebSocket Protocol
- [RFC 7692](http://tools.ietf.org/html/rfc7692) Compression Extensions for WebSocket


TODO
----

- SOCKS support
- Public Key Pinning support ([RFC 7469](http://tools.ietf.org/html/rfc7469))
- And other [issues](https://github.com/TakahikoKawasaki/nv-websocket-client/issues)


Acknowledgement
---------------

- PR #107 has incorporated `DistinguishedNameParser` and `OkHostnameVerifier`
  into this library and they are from [okhttp](https://github.com/square/okhttp).


Author
------

[Authlete, Inc.](https://www.authlete.com/) & Neo Visionaries Inc.<br/>
Takahiko Kawasaki &lt;taka@authlete.com&gt;
