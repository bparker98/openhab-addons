//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.openhab.binding.samsungtvjsonrpc.internal;

import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.google.gson.Gson;

@NonNullByDefault({})
public class JsonRPCHandler {
    private String url;
    private String apiUrl;
    private String upnpUrl;
    private static int counter = 0;
    private final HttpClient httpClient;
    private final Gson gson;
    private String authToken;
    private static final String ACCESS_TOKEN = "AccessToken";
    public static final String TV_STATES = "getTVStates";
    public static final String REMOTE_KEY_CONTROL = "remoteKeyControl";
    public static final String REMOTE_KEY = "remoteKey";
    private static final String INPUT_SOURCE_CONTROL = "inputSourceControl";
    public static final String INPUT_SOURCE = "inputSource";
    private static final String POWER_CONTROL = "powerControl";
    private static final String POWER = "power";
    private static final String POWER_ON = "powerOn";
    private static final String POWER_OFF = "powerOff";
    private static final String MUTE_CONTROL = "muteControl";
    private static final String MUTE = "mute";
    private static final String MUTE_ON = "muteOn";
    private static final String MUTE_OFF = "muteOff";
    private static final int API_PORT = 8001;
    private static final int UPNP_PORT = 9110;
    private final Logger logger;

    public static int getCounter() {
        int value = counter++;
        return value;
    }

    public JsonRPCHandler(HttpClient httpClient, String hostname, int port, String authToken)
            throws URISyntaxException, MalformedURLException {
        this(httpClient, (String) null, authToken);
        URI uri = new URI("https", (String) null, hostname, port, (String) null, (String) null, (String) null);
        URI apiUri = new URI("http", (String) null, hostname, 8001, "/api/v2/", (String) null, (String) null);
        URI upnpUri = new URI("http", (String) null, hostname, 9110, "/ip_control", (String) null, (String) null);
        this.apiUrl = apiUri.toURL().toString();
        this.upnpUrl = upnpUri.toURL().toString();
        this.initalize(uri.toURL().toString());
    }

    public JsonRPCHandler.TVProperties getTVProperties() {
        ContentResponse response = this.makeJsonRequest(this.apiUrl, (Object) null);
        if (response == null) {
            return null;
        } else {
            String respString = response.getContentAsString();
            this.logger.info("Got {}", respString);
            JsonRPCHandler.TVProperties props = (JsonRPCHandler.TVProperties) this.gson.fromJson(respString,
                    JsonRPCHandler.TVProperties.class);
            return props;
        }
    }

    @Nullable
    public Document getIPControlUPnPDescription() {
        ContentResponse response = null;
        Request req = this.httpClient.newRequest(this.upnpUrl);
        req.method(HttpMethod.GET);

        try {
            response = req.send();
        } catch (TimeoutException | ExecutionException | InterruptedException var4) {
            this.logger.error("Error GET on {}", this.upnpUrl, var4);
            return null;
        }

        return SamsungTvUtils.loadXMLFromString(response.getContentAsString());
    }

    public JsonRPCHandler(HttpClient httpClient, String url, String authToken) {
        this.logger = LoggerFactory.getLogger(JsonRPCHandler.class);
        this.url = url;
        this.httpClient = httpClient;
        this.gson = new Gson();
        this.authToken = authToken;
        if (url != null) {
            this.initalize(url);
        }
    }

    public void initalize(String url) {
        this.url = url;
        this.logger.info("Initialized JsonRPCHandler at {}", this.url);
    }

    public void setAuthToken(String token) {
        this.authToken = token;
    }

    @Nullable
    public Map<String, ?> getTVStates() {
        JsonRPCHandler.JsonRPCResponse resp = this.makeJsonRPCRequest("getTVStates");
        return resp != null ? resp.result : null;
    }

    @Nullable
    public State setInputSource(Command command) {
        JsonRPCHandler.JsonRPCResponse resp = this.makeJsonRPCRequest("inputSourceControl", "inputSource",
                command.toString());
        return resp != null ? new StringType(resp.result.get("inputSource").toString()) : null;
    }

    public State getPowerState() {
        State state = UnDefType.UNDEF;
        JsonRPCHandler.JsonRPCResponse resp = this.makeJsonRPCRequest("powerControl");
        if (resp != null) {
            state = this.convertPowerStringToState(resp.result.get("power").toString());
        }

        return (State) state;
    }

    private State convertPowerStringToState(String state) {
        return state.equals("powerOn") ? OnOffType.ON : OnOffType.OFF;
    }

    private State convertMuteStringToState(String state) {
        return state.equals("muteOn") ? OnOffType.ON : OnOffType.OFF;
    }

    public State getMuteState() {
        State state = UnDefType.UNDEF;
        JsonRPCHandler.JsonRPCResponse resp = this.makeJsonRPCRequest("muteControl");
        if (resp != null) {
            state = this.convertMuteStringToState(resp.result.get("mute").toString());
        }

        return (State) state;
    }

    public State setMuteState(Command command) {
        String state = command.equals(OnOffType.ON) ? "muteOn" : "muteOff";
        JsonRPCHandler.JsonRPCResponse resp = this.makeJsonRPCRequest("muteControl", "mute", state);
        return resp != null ? this.convertMuteStringToState(resp.result.get("mute").toString()) : null;
    }

    @Nullable
    public State setPowerState(Command command) {
        String state = command.equals(OnOffType.ON) ? "powerOn" : "powerOff";
        JsonRPCHandler.JsonRPCResponse resp = this.makeJsonRPCRequest("powerControl", "power", state);
        return resp != null ? this.convertPowerStringToState(resp.result.get("power").toString()) : null;
    }

    public JsonRPCHandler.JsonRPCResponse makeJsonRPCRequest(String method) {
        return this.makeJsonRPCRequest(method, false);
    }

    public JsonRPCHandler.JsonRPCResponse makeJsonRPCRequest(String method, boolean returnError) {
        Map<String, Object> params = new HashMap();
        return this.makeJsonRPCRequest(method, params, returnError);
    }

    public JsonRPCHandler.JsonRPCResponse makeJsonRPCRequest(String method, String paramKey, Object paramValue) {
        return this.makeJsonRPCRequest(method, paramKey, paramValue, false);
    }

    public JsonRPCHandler.JsonRPCResponse makeJsonRPCRequest(String method, String paramKey, Object paramValue,
            boolean returnError) {
        Map<String, Object> params = new HashMap();
        params.put(paramKey, paramValue);
        return this.makeJsonRPCRequest(method, params, returnError);
    }

    public JsonRPCHandler.JsonRPCResponse makeJsonRPCRequest(String method, Map<String, Object> params) {
        return this.makeJsonRPCRequest(method, params, false);
    }

    public String convertWithStream(Map<String, ?> map) {
        String mapAsString = (String) map.keySet().stream().map((key) -> {
            return key + "=" + map.get(key);
        }).collect(Collectors.joining(", ", "{", "}"));
        return mapAsString;
    }

    private ContentResponse makeJsonRequest(String url, Object body) {
        if (this.httpClient.isStopped()) {
            try {
                this.httpClient.start();
            } catch (Exception var6) {
                this.logger.error("Error starting httpClient");
                return null;
            }
        }

        Request req = this.httpClient.newRequest(url);
        if (body != null) {
            String jsonBody = this.gson.toJson(body);
            this.logger.debug("BODY: {}", jsonBody);
            req.content(new StringContentProvider(jsonBody));
        }

        req.header(HttpHeader.ACCEPT, "application/json");
        req.header(HttpHeader.CONTENT_TYPE, "application/json");
        if (body == null) {
            this.logger.debug("GET REQUEST TO {}", url);
            req.method(HttpMethod.GET);
        } else {
            req.method(HttpMethod.POST);
        }

        try {
            return req.send();
        } catch (TimeoutException | ExecutionException | InterruptedException var7) {
            Throwable rootCause = var7.getCause();
            if (rootCause instanceof NoRouteToHostException) {
                this.logger.debug("NoRouteToHost {}", this.url);
            } else {
                this.logger.error("Error making JSON-RPC request", var7);
            }

            return null;
        }
    }

    public JsonRPCHandler.JsonRPCResponse makeJsonRPCRequest(String method, Map<String, Object> params,
            boolean returnError) {
        JsonRPCHandler.JsonRPCRequest obj = new JsonRPCHandler.JsonRPCRequest(method, this.authToken, params);
        ContentResponse response = this.makeJsonRequest(this.url, obj);
        if (response == null) {
            return null;
        } else {
            JsonRPCHandler.JsonRPCResponse parsedResp = (JsonRPCHandler.JsonRPCResponse) this.gson
                    .fromJson(response.getContentAsString(), JsonRPCHandler.JsonRPCResponse.class);
            if ((parsedResp.error == null || parsedResp.error.code == 0) && parsedResp.code == 0) {
                return parsedResp;
            } else {
                if (parsedResp.code != 0) {
                    this.logger.error("Received error code {} with message {} for request on {} with {}",
                            new Object[] { parsedResp.code, parsedResp.message, method, this.gson.toJson(obj) });
                } else {
                    this.logger.error("Received error code {} with message {} for request on {} with {}", new Object[] {
                            parsedResp.error.code, parsedResp.error.code, method, this.gson.toJson(obj) });
                }

                return returnError ? parsedResp : null;
            }
        }
    }

    @Nullable
    public String getAuthToken() throws ExecutionException, InterruptedException, TimeoutException {
        JsonRPCHandler.JsonRPCBase obj = new JsonRPCHandler.JsonRPCBase("createAccessToken");
        ContentResponse response = this.makeJsonRequest(this.url, obj);
        if (response == null) {
            return null;
        } else {
            String stringResp = response.getContentAsString();
            JsonRPCHandler.JsonRPCError err = (JsonRPCHandler.JsonRPCError) this.gson.fromJson(stringResp,
                    JsonRPCHandler.JsonRPCError.class);
            if (err.code != 0) {
                this.logger.error("Received error code {} with message {}", err.code, err.message);
                return null;
            } else {
                JsonRPCHandler.JsonRPCResponse resp = (JsonRPCHandler.JsonRPCResponse) this.gson.fromJson(stringResp,
                        JsonRPCHandler.JsonRPCResponse.class);
                this.authToken = resp.result.get("AccessToken").toString();
                return this.authToken;
            }
        }
    }

    public State sendKey(Command command) {
        JsonRPCHandler.JsonRPCResponse resp = this.makeJsonRPCRequest("remoteKeyControl", "remoteKey",
                command.toString());
        return resp == null ? null : new StringType(resp.result.get("remoteKey").toString());
    }

    @NonNullByDefault({})
    public static class JsonRPCBase {
        public String jsonrpc;
        public String method;
        public int id;

        public JsonRPCBase(String method) {
            this.method = method;
            this.id = JsonRPCHandler.getCounter();
            this.jsonrpc = "2.0";
        }
    }

    @NonNullByDefault({})
    public static class JsonRPCError {
        public int code;
        public String message;
        public String data;

        public JsonRPCError() {
        }
    }

    @NonNullByDefault({})
    public static class JsonRPCRequest extends JsonRPCHandler.JsonRPCBase {
        public Map<String, Object> params;

        public JsonRPCRequest(String method, String authToken) {
            super(method);
            this.params = new HashMap();
            this.params.put("AccessToken", authToken);
        }

        public JsonRPCRequest(String method, String authToken, Map<String, Object> params) {
            super(method);
            this.params = params;
            if (this.params == null) {
                this.params = new HashMap();
            }

            this.params.put("AccessToken", authToken);
        }
    }

    @NonNullByDefault({})
    public static class JsonRPCResponse extends JsonRPCHandler.JsonRPCBase {
        public int code;
        public String message;
        public JsonRPCHandler.JsonRPCError error;
        public Map<String, ?> result;

        public JsonRPCResponse() {
            super((String) null);
        }
    }

    @NonNullByDefault({})
    static class TVProperties {
        JsonRPCHandler.TVProperties.Device device;
        String isSupport;
        String version;

        TVProperties() {
        }

        @NonNullByDefault({})
        static class Device {
            boolean FrameTVSupport;
            boolean GamePadSupport;
            boolean ImeSyncedSupport;
            String OS;
            boolean TokenAuthSupport;
            boolean VoiceSupport;
            String countryCode;
            String description;
            String firmwareVersion;
            String modelName;
            String name;
            String networkType;
            String resolution;

            Device() {
            }
        }
    }
}
