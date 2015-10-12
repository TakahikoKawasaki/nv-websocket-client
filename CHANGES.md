CHANGES
=======

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


----------------
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
