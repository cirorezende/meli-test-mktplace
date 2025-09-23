package br.com.ml.mktplace.orders.integration.support;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/**
 * Starts a single WireMock server on port 9999 reused across tests.
 */
public final class SharedWireMock {
    private static WireMockServer server;

    private SharedWireMock() {}

    public static synchronized void startIfNeeded() {
        if (server == null) {
            server = new WireMockServer(WireMockConfiguration.options().port(9999));
            server.start();
            WireMock.configureFor("localhost", 9999);
        }
    }

    public static synchronized void stop() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }
}
