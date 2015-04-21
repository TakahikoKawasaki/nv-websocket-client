nv-websocket-client
===================

Overview
--------

High-quality WebSocket client implementation in Java which

- complies with [RFC 6455](http://tools.ietf.org/html/rfc6455) (The WebSocket Protocol),
- works on Java SE 1.5+ and Android,
- supports all the frame types (continuation/binary/text/close/ping/pong),
- provides a method to send a fragmented frame in addition to methods for unfragmented frames,
- provides a method to get the underlying raw socket of a web socket to configure it,
- provides a method for [Basic Authentication](http://tools.ietf.org/html/rfc2617),
- provides a factory class which utilizes javax.net.SocketFactory interface,
- provides a rich listener interface to hook web socket events,
- has fine-grained error codes for fine-grained controllability on errors,
- and allows to disable validity checks on RSV1/RSV2/RSV3 bits and opcode of frames.


License
-------

Apache License, Version 2.0


Maven
-----

```xml
<dependency>
    <groupId>com.neovisionaries</groupId>
    <artifactId>nv-websocket-client</artifactId>
    <version>1.0</version>
</dependency>
```


Source Download
---------------

    git clone https://github.com/TakahikoKawasaki/nv-websocket-client.git


JavaDoc
-------

[JavaDoc of nv-websocket-client](http://TakahikoKawasaki.github.io/nv-websocket-client/)


Description
-----------

`WebSocket` class represents a web socket. Its instances are created by calling
one of `createSocket` methods of a `WebSocketFactory` instance. `WebSocketFactory`
class provides methods such as `setSSLSocketFactory` to configure the underlying
socket factories. Below is the simplest example to create a `WebSocket` instance.

```java
// Create a web socket. The scheme part can be one of the following:
// 'ws', 'wss', 'http' and 'https' (case-insensitive). The user info
// part, if any, is interpreted as expected.
WebSocket ws = new WebSocketFactory().createSocket("ws://localhost/endpoint");
```

After creating a `WebSocket` instance, you should call `addListener` method
to register a `WebSocketListener` that receives web socket events.
`WebSocketAdapter` is an empty implementation of `WebSocketListener` interface.

```java
// Register a listener to receive web socket events.
ws.addListener(new WebSocketAdapter() {
    @Override
    public void onTextMessage(WebSocket websocket, String message) {
        // Received a text message.
        ......
    }
});
```

Before making a connection to the server, you can configure the web socket
instance by using the following methods.

| METHOD         | DESCRIPTION                                            |
|----------------|--------------------------------------------------------|
| `addProtocol`  | Adds an element to `Sec-WebSocket-Protocol`.           |
| `addExtension` | Adds an element to `Sec-WebSocket-Extensions`.         |
| `addHeaer`     | Adds an arbitrary HTTP header.                         |
| `setUserInfo`  | Adds `Authorization` header for Basic Authentication.  |
| `getSocket`    | Gets the underlying `Socket` instance to configure it. |
| `setExtended`  | Disables validity checks on RSV1/RSV2/RSV3 and opcode. |

By calling `connect()` method, an actual connection to the server is made and
the [opening handshake](https://tools.ietf.org/html/rfc6455#section-4) is
performed synchronously. When a connection could not be made or a protocol
error was detected during the handshake, a `WebSocketException` is thrown.
Instead, when the handshake succeeded, the `connect()` implementation creates
threads and starts them to read and write web socket frames asynchronously.

```java
try
{
    // Connect to the server and perform the opening handshake.
    ws.connect();
}
catch (WebSocketException e)
{
    // Failed.
}
```

Web socket frames can be sent by `sendFrame` method. Other `sendXxx`
methods such as `sendText` are aliases of `sendFrame` method. All of
the `sendXxx` methods work asynchronously. Below are some examples
of `sendXxx` methods. Note that in normal cases, you don't have to
call `sendClose` method and `sendPong` method (or their variants)
explicitly because they are called automatically when appropriate.

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
// Notethat the FIN bit of frames returned from
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

Before a web socket is closed, a closing handshake is performed. A closing
handshake is started (1) when the server sends a close frame to the client
or (2) when the client sends a close frame to the server. You can start a
closing handshake by calling `disconnect()` method (or by sending a close
frame manually).

```java
// Close the web socket connection.
ws.disconnect();
```


Sample Application
------------------

The following is a sample application that connects to the echo server on
[websocket.org](https://www.websocket.org) (`ws://echo.websocket.org`) and
repeats to (1) read a line from the standard input, (2) send the read line
to the server and (3) prints the response from the server, until `exit` is
entered.

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

        // Close the web socket.
        ws.disconnect();
    }


    /**
     * Connect to the server.
     */
    private static WebSocket connect() throws IOException, WebSocketException
    {
        return new WebSocketFactory()
            .createSocket(SERVER)
            .addListener(new WebSocketAdapter() {
                public void onTextMessage(WebSocket websocket, String message) {
                    System.out.println(message);
                }
            })
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


See Also
--------

- [RFC 6455](https://tools.ietf.org/html/rfc6455)


ToDo
----

Proxy support.


Author
------

Takahiko Kawasaki, Neo Visionaries Inc.
