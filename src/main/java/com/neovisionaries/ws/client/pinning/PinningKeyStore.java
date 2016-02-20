/**
 * Copyright (C) 2011-2013 Moxie Marlinspike
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.neovisionaries.ws.client.pinning;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;

/**
 * Out custom keystore used for ssl pinning.
 */
public class PinningKeyStore {

	private static final int CACERTS_FILE_SIZE = 1024 * 140;

	private static PinningKeyStore instance;

	public static synchronized PinningKeyStore getInstance(KeyStoreProvider provider) {
		if (instance == null) {
			instance = new PinningKeyStore(provider);
		}
		return instance;
	}

	private final HashMap<Principal, X509Certificate> trustRoots;
	final KeyStore trustStore;

	private PinningKeyStore(KeyStoreProvider provider) {
		final KeyStore trustStore = getTrustStore(provider);
		this.trustRoots = initializeTrustedRoots(trustStore);
		this.trustStore = trustStore;
	}

	public boolean isTrustRoot(X509Certificate certificate) {
		final X509Certificate trustRoot = trustRoots.get(certificate.getSubjectX500Principal());
		return trustRoot != null && trustRoot.getPublicKey().equals(certificate.getPublicKey());
	}

	public X509Certificate getTrustRootFor(X509Certificate certificate) {
		final X509Certificate trustRoot = trustRoots.get(certificate.getIssuerX500Principal());

		if (trustRoot == null) {
			return null;
		}

		if (trustRoot.getSubjectX500Principal().equals(certificate.getSubjectX500Principal())) {
			return null;
		}

		try {
			certificate.verify(trustRoot.getPublicKey());
		} catch (GeneralSecurityException e) {
			return null;
		}

		return trustRoot;
	}

	private HashMap<Principal, X509Certificate> initializeTrustedRoots(KeyStore trustStore) {
		try {
			final HashMap<Principal, X509Certificate> trusted =
					new HashMap<Principal, X509Certificate>();

			for (Enumeration<String> aliases = trustStore.aliases(); aliases.hasMoreElements(); ) {
				final String alias = aliases.nextElement();
				final X509Certificate cert = (X509Certificate) trustStore.getCertificate(alias);

				if (cert != null) {
					trusted.put(cert.getSubjectX500Principal(), cert);
				}
			}

			return trusted;
		} catch (KeyStoreException e) {
			throw new PinningException(e);
		}
	}

	private KeyStore getTrustStore(KeyStoreProvider provider) {
		try {
			final KeyStore trustStore = KeyStore.getInstance("BKS");
			final BufferedInputStream bin = new BufferedInputStream(provider.getKeyStoreStream(),
							CACERTS_FILE_SIZE);

			try {
				trustStore.load(bin, provider.getPassword().toCharArray());
			} finally {
				try {
					bin.close();
				} catch (IOException ioe) {
					// TODO : log
				}
			}

			return trustStore;
		} catch (KeyStoreException kse) {
			throw new PinningException(kse);
		} catch (NoSuchAlgorithmException e) {
			throw new PinningException(e);
		} catch (CertificateException e) {
			throw new PinningException(e);
		} catch (IOException e) {
			throw new PinningException(e);
		}
	}
}