package com.neovisionaries.ws.client.pinning;


public class PinningException extends RuntimeException {

	public PinningException(String message) {
		super(message);
	}

	public PinningException(Throwable cause) {
		super(cause);
	}

	public PinningException(String message, Throwable cause) {
		super(message, cause);
	}
}
