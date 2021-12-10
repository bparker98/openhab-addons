/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.openhab.binding.samsungtvjsonrpc.internal;

import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SamsungTVJsonRPCHandlerFactory} class does factory pattern stuff
 *
 * @author Brandon Parker - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = { "binding.samsungtvjsonrpc" }, service = { ThingHandlerFactory.class })
public class SamsungTVJsonRPCHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS;
    private final HttpClient httpsClient;
    private final ClientBuilder clientBuilder;
    private final HttpClientFactory httpClientFactory;
    private final Logger logger = LoggerFactory.getLogger(SamsungTVJsonRPCHandlerFactory.class);
    private final HttpClient httpClient;

    static {
        SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
                (Set) Stream.of(SamsungTVJsonRPCBindingConstants.THING_TYPE_SAMSUNG_TV).collect(Collectors.toSet()));
    }

    @Activate
    public SamsungTVJsonRPCHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference ClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder.connectTimeout(5L, TimeUnit.SECONDS).readTimeout(5L, TimeUnit.SECONDS);
        this.httpClientFactory = httpClientFactory;
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.httpsClient = httpClientFactory.createHttpClient(SamsungTVJsonRPCBindingConstants.BINDING_ID);

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");

            TrustManager[] trustAllCerts = new TrustManager[] { new X509ExtendedTrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate @Nullable [] chain, @Nullable String authType)
                        throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate @Nullable [] chain, @Nullable String authType)
                        throws CertificateException {
                }

                @Override
                public X509Certificate @Nullable [] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate @Nullable [] chain, @Nullable String authType,
                        @Nullable Socket socket) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate @Nullable [] chain, @Nullable String authType,
                        @Nullable Socket socket) throws CertificateException {
                }

                @Override
                public void checkClientTrusted(X509Certificate @Nullable [] chain, @Nullable String authType,
                        @Nullable SSLEngine engine) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate @Nullable [] chain, @Nullable String authType,
                        @Nullable SSLEngine engine) throws CertificateException {
                }
            } };
            sslContext.init(null, trustAllCerts, null);

            this.httpsClient.getSslContextFactory().setSslContext(sslContext);
        } catch (NoSuchAlgorithmException e) {
            logger.warn("An exception occurred while requesting the SSL encryption algorithm : '{}'", e.getMessage(),
                    e);
        } catch (KeyManagementException e) {
            logger.warn("An exception occurred while initialising the SSL context : '{}'", e.getMessage(), e);
        }
    }

    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);

        try {
            this.httpsClient.start();
        } catch (Exception var3) {
            this.logger.warn("Unable to start Jetty HttpClient", var3);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
    }

    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Nullable
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        return SamsungTVJsonRPCBindingConstants.THING_TYPE_SAMSUNG_TV.equals(thingTypeUID)
                ? new SamsungTVJsonRPCHandler(thing, this.httpsClient)
                : null;
    }
}
