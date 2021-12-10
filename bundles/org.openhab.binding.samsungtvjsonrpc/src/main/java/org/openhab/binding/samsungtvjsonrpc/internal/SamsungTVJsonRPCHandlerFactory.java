//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.openhab.binding.samsungtvjsonrpc.internal;

import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.net.ssl.KeyManager;
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
        this.httpsClient = httpClientFactory.createHttpClient("samsungtvjsonrpc");

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            TrustManager[] trustAllCerts = new TrustManager[] { new X509ExtendedTrustManager() {
                public void checkClientTrusted(@Nullable X509Certificate[] chain, @Nullable String authType)
                        throws CertificateException {
                }

                public void checkServerTrusted(@Nullable X509Certificate[] chain, @Nullable String authType)
                        throws CertificateException {
                }

                @Nullable
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(@Nullable X509Certificate[] chain, @Nullable String authType,
                        @Nullable Socket socket) throws CertificateException {
                }

                public void checkServerTrusted(@Nullable X509Certificate[] chain, @Nullable String authType,
                        @Nullable Socket socket) throws CertificateException {
                }

                public void checkClientTrusted(@Nullable X509Certificate[] chain, @Nullable String authType,
                        @Nullable SSLEngine engine) throws CertificateException {
                }

                public void checkServerTrusted(@Nullable X509Certificate[] chain, @Nullable String authType,
                        @Nullable SSLEngine engine) throws CertificateException {
                }
            } };
            sslContext.init((KeyManager[]) null, trustAllCerts, (SecureRandom) null);
            this.httpsClient.getSslContextFactory().setSslContext(sslContext);
        } catch (NoSuchAlgorithmException var5) {
            this.logger.warn("An exception occurred while requesting the SSL encryption algorithm : '{}'",
                    var5.getMessage(), var5);
        } catch (KeyManagementException var6) {
            this.logger.warn("An exception occurred while initialising the SSL context : '{}'", var6.getMessage(),
                    var6);
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
