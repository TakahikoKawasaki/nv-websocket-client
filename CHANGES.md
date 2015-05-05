CHANGES
=======

1.3-SNAPSHOT
------------

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
