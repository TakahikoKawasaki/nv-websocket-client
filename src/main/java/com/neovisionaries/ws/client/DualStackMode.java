package com.neovisionaries.ws.client;


/**
 * The dual stack mode defines which IP address families will be used to
 * establish a connection.
 */
public enum DualStackMode
{
    /**
     * Try both IPv4 and IPv6 to establish a connection. Used by default and
     * should generally be preferred.
     */
    BOTH,


    /**
     * Only use IPv4 to establish a connection.
     */
    IPV4_ONLY,


    /**
     * Only use IPv6 to establish a connection.
     */
    IPV6_ONLY,
}
