nv-websocket-client
===================

Overview
--------

WebSocket client implementation in Java.


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


Example
-------

```java
// Create a web socket.
WebSocket ws = new WebSocketFactory().createSocket("ws://localhost/endpoint");

// Register a listener to receive web socket events.
ws.addListener(new WebSocketAdapter() {
    @Override
    public void onTextMessage(WebSocket websocket, String text) {
        // Received a text message.
        ......
    }
});

// Connect to the server and perform the opening handshake.
ws.open();
```


Limitations
-----------

* According to the specification ([RFC 6455](https://tools.ietf.org/html/rfc6455)),
  the maximum length of the payload part of a frame is (2^63 - 1), but this
  library cannot treat frames whose payload length is greater than (2^31 - 1).


Note
----

Just started. Not usable yet.


Author
------

Takahiko Kawasaki, Neo Visionaries Inc.
