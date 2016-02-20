package com.neovisionaries.ws.client.pinning;

import java.io.InputStream;

public interface KeyStoreProvider {

	InputStream getKeyStoreStream();
	String getPassword();
}
