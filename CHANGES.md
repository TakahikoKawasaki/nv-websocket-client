CHANGES
=======

1.4 (2015-05-19)
----------------

- Fixed a bug in `WebSocketInputStream.java` which did not
  consider the case where `read` returns less bytes than
  required. ([PR#2](https://github.com/TakahikoKawasaki/nv-websocket-client/pull/2))


1.3 (2015-05-06)
----------------

- Supported HTTP proxy.
- Changed the implementation class of the second argument of
  WebSocketListener.onConnected method from HashMap to TreeMap
  with String.CASE_INSENSITIVE_ORDER.


1.2 (2015-05-01)
----------------

- Added getPingInterval/setPingInterval methods to WebSocket.
- Added getPongInterval/setPongInterval methods to WebSocket.


1.1 (2015-05-01)
----------------

- Added isOpen method to WebSocket.
- Fixed a bug around Basic Authentication.
- Added getURI method to WebSocket.
- Added onStateChanged method to WebSocketListener.


1.0 (2015-04-22)
----------------

- The first release.
