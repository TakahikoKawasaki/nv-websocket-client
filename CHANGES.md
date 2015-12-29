CHANGES
=======

1.17-SNAPSHOT
-------------

- Added `PERMESSAGE_DEFLATE` to `WebSocketExtension`.
- Added `COMPRESSION_ERROR` to `WebSocketError`.


1.16 (2015-12-26)
-----------------

- Added `onMessageDecompressionError(WebSocket, WebSocketException, byte[])`
  to `WebSocketListener`.
- Added `DECOMPRESSION_ERROR` to `WebSocketError`.
- Fixed a deadlock issue reported in #21.
- Changed the frame queue in `WritingThread` from `List` to `Deque` for #21.
- Changed `WritingThread` to process PING & PONG frames immediately.


1.15 (2015-12-25)
-----------------

- OSGi support. `Bundle-SymbolicName` is `com.neovisionaries.ws.client`.
- Added `EXTENSIONS_CONFLICT` to `WebSocketError`.
- Added `PERMESSAGE_DEFLATE_INVALID_MAX_WINDOW_BITS` to `WebSocketError`.
- Added `PERMESSAGE_DEFLATE_UNSUPPORTED_PARAMETER` to `WebSocketError`.
- Added `UNEXPECTED_RESERVED_BIT` to `WebSocketError`.
- Modified `WebSocketFrame.toString()` to stringify the payload better.
- Copied Extended and AutoFlush settings in `WebSocket.recreate(int)`.
- Added `onSendingFrame(WebSocket, WebSocketFrame)` to `WebSocketListener`.
- Added `getFrameQueueSize()` and `setFrameQueueSize(int)` to `WebSocket`
  for congestion control.


1.14 (2015-12-10)
-----------------

- Performance tuning on `ListenerManager`.
- Added `WebSocket.addExtension(String)` method.
- Added `WebSocket.addListeners(List<WebSocketListener>)` method.
- Added `WebSocket.clearExtensions()` method.
- Added `WebSocket.clearHeaders()` method.
- Added `WebSocket.clearProtocols()` method.
- Added `WebSocket.clearUserInfo()` method.
- Added `WebSocket.removeExtension(WebSocketExtension)` method.
- Added `WebSocket.removeExtensions(String)` method.
- Added `WebSocket.removeHeaders(String)` method.
- Added `WebSocket.removeListeners(List<WebSocketListener>)` method.
- Added `WebSocket.removeProtocol(String)` method.


1.13 (2015-12-04)
-----------------

- Added `WebSocket.removeListener(WebSocketListener)` method.
- Added `WebSocket.clearListeners()` method.


1.12 (2015-10-12)
-----------------

- Fixed a bug in `WebSocketFactory`. `getRawPath()` and `getRawQuery()`
  should be used instead of `getPath()` and `getQuery()`.


1.11 (2015-10-12)
-----------------

- Changed some error messages of `WebSocketException`.
  (Appended `cause.getMessage()` in cases where a cause is available.)


1.10 (2015-09-24)
-----------------

- Added `WebSocket.recreate(int timeout)` method.
- Added `WebSocketFactory.getConnectionTimeout()` method.
- Added `WebSocketFactory.setConnectionTimeout(int timeout)` method.
- Added `WebSocketFactory.createSocket(String uri, int timeout)` method.
- Added `WebSocketFactory.createSocket(URL url, int timeout)` method.
- Added `WebSocketFactory.createSocket(URI uri, int timeout)` method.


1.9 (2015-09-03)
----------------

- Added `throws Exception` to all methods of `WebSocketListener`.
- Added `handleCallbackError()` to `WebSocketListener`.


1.8 (2015-08-28)
----------------

- Added `connectAsynchronously()` method to `WebSocket`.
- Added `onConnectError(WebSocket, WebSocketException)` to `WebSocketListener`.


1.7 (2015-08-28)
----------------

- Added `connect(ExecutorService)` and `connectable()` methods to `WebSocket`.
- Added `LICENSE`.


1.6 (2015-06-26)
----------------

- Added `WebSocket.recreate()` method.
- Added a copy constructor to `WebSocketExtension`.


1.5 (2015-06-04)
----------------

- Added `flush()`, `isAutoFlush()` and `setAutoFlush(boolean)` methods to `WebSocket`.
- Added variants of `WebSocket.disconnect` method.


1.4 (2015-05-19)
----------------

- Fixed a bug in `WebSocketInputStream.java` which did not
  consider the case where `read` returns less bytes than
  required. ([PR#2](https://github.com/TakahikoKawasaki/nv-websocket-client/pull/2))


1.3 (2015-05-06)
----------------

- Supported HTTP proxy.
- Changed the implementation class of the second argument of
  `WebSocketListener.onConnected` method from `HashMap` to `TreeMap`
  with `String.CASE_INSENSITIVE_ORDER`.


1.2 (2015-05-01)
----------------

- Added `getPingInterval`/`setPingInterval` methods to `WebSocket`.
- Added `getPongInterval`/`setPongInterval` methods to `WebSocket`.


1.1 (2015-05-01)
----------------

- Added `isOpen` method to `WebSocket`.
- Fixed a bug around Basic Authentication.
- Added `getURI` method to `WebSocket`.
- Added `onStateChanged` method to `WebSocketListener`.


1.0 (2015-04-22)
----------------

- The first release.
