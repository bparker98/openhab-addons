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

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.samsungtvjsonrpc.internal.JsonRPCHandler.TVProperties;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The {@link SamsungTVJsonRPCHandler} class handles JSON RPC stuff
 *
 * @author Brandon Parker - Initial contribution
 */
@NonNullByDefault
public class SamsungTVJsonRPCHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(SamsungTVJsonRPCHandler.class);
    @Nullable
    private SamsungTVJsonRPCConfiguration config;
    private final Map<String, State> lastStates = new HashMap();
    private final HttpClient httpClient;
    @Nullable
    private ScheduledFuture<?> pollJob = null;
    @Nullable
    private JsonRPCHandler jsonRPCHandler = null;
    private static final int WOL_PACKET_RETRY_COUNT = 10;

    public void dispose() {
        try {
            ScheduledFuture<?> job = this.pollJob;
            if (job != null) {
                job.cancel(true);
                this.pollJob = null;
            }

            this.httpClient.stop();
        } catch (Exception var2) {
        }
    }

    public SamsungTVJsonRPCHandler(Thing thing, HttpClient httpsClient) {
        super(thing);
        this.httpClient = httpsClient;
    }

    public void handleCommand(ChannelUID channelUID, Command command) {
        String channel = channelUID.getId();
        if (this.jsonRPCHandler != null) {
            if (command.equals(RefreshType.REFRESH)) {
                switch (channel) {
                    case SamsungTVJsonRPCBindingConstants.CHANNEL_KEYCODE:
                        this.updateState(channel, (State) this.lastStates.getOrDefault("keycode", UnDefType.UNDEF));
                        break;
                    case SamsungTVJsonRPCBindingConstants.CHANNEL_POWER:
                        this.updateState(channel, this.getPowerState());
                        break;
                    case SamsungTVJsonRPCBindingConstants.CHANNEL_INPUT_SOURCE:
                        this.updateState(channel, this.getInputSource());
                        break;
                }
            } else {
                switch (channel) {
                    case SamsungTVJsonRPCBindingConstants.CHANNEL_KEYCODE:
                        State state = this.jsonRPCHandler.sendKey(command);
                        if (state != null) {
                            this.updateState(channel, state);
                        }
                    case SamsungTVJsonRPCBindingConstants.CHANNEL_POWER:
                        if (command == OnOffType.ON) {
                            this.sendPowerCommandWithWOL(command);
                        } else {
                            this.jsonRPCHandler.setPowerState(command);
                        }
                        break;
                    case SamsungTVJsonRPCBindingConstants.CHANNEL_INPUT_SOURCE:
                        this.jsonRPCHandler.setInputSource(command);
                        break;
                }
            }

        }
    }

    private State getInputSource() {
        State inputSource = UnDefType.UNDEF;
        if (this.jsonRPCHandler != null) {
            Map<String, ?> tvStates = this.jsonRPCHandler.getTVStates();
            if (tvStates != null) {
                inputSource = new StringType(this.jsonRPCHandler.getTVStates().get("inputSource").toString());
            }
        }

        return (State) inputSource;
    }

    private State getPowerState() {
        State state = UnDefType.UNDEF;
        if (this.jsonRPCHandler != null) {
            state = this.jsonRPCHandler.getPowerState();
        }

        return (State) state;
    }

    private void sendPowerCommandWithWOL(final Command command) {
        if (this.config != null && this.config.macAddress != null && !this.config.macAddress.isEmpty()
                && this.jsonRPCHandler != null) {
            if (command == OnOffType.ON) {
                this.lastStates.put("power", UnDefType.UNDEF);
            }

            this.scheduler.schedule(new Runnable() {
                int count = 0;

                public void run() {
                    ++this.count;
                    if (this.count < 10) {
                        WakeOnLanUtility.sendWOLPacket(SamsungTVJsonRPCHandler.this.config.macAddress);
                        SamsungTVJsonRPCHandler.this.scheduler.schedule(this, 100L, TimeUnit.MILLISECONDS);
                    } else {
                        State state = SamsungTVJsonRPCHandler.this.jsonRPCHandler.setPowerState(command);
                        if (state != null) {
                            SamsungTVJsonRPCHandler.this.updateState("power", state);
                        }
                    }
                }
            }, 1L, TimeUnit.MILLISECONDS);
            this.scheduler.schedule(new Runnable() {
                int count = 0;

                public void run() {
                    ++this.count;
                    if (this.count < 10 && !(SamsungTVJsonRPCHandler.this.lastStates.get("power").equals(command))) {
                        SamsungTVJsonRPCHandler.this.scheduler.schedule(this, 100L, TimeUnit.MILLISECONDS);
                        State state = SamsungTVJsonRPCHandler.this.jsonRPCHandler.setPowerState(command);
                        if (state != null) {
                            SamsungTVJsonRPCHandler.this.updateState("power", state);
                        }
                    }
                }
            }, 100L, TimeUnit.MILLISECONDS);
        }
    }

    protected void updateState(String channelID, State state) {
        super.updateState(channelID, state);
        State lastState = (State) this.lastStates.get(channelID);
        if ((lastState == UnDefType.UNDEF || lastState == null) && state != UnDefType.UNDEF) {
            this.updateStatus(ThingStatus.ONLINE);
        }

        this.lastStates.put(channelID, state);
        if (state == UnDefType.UNDEF) {
            this.updateStatus(ThingStatus.OFFLINE);
        }
    }

    private void pollForStatus() {
        if (this.jsonRPCHandler == null) {
            this.logger.warn("Skipping poll due to null JsonRPCHandler");
        } else {
            State inputSource = this.getInputSource();
            if (!this.lastStates.get("inputsource").equals(inputSource)) {
                this.updateState("inputsource", inputSource);
            }

            State powerState = this.getPowerState();
            if (!this.lastStates.get("power").equals(powerState)) {
                this.updateState("power", powerState);
            }

        }
    }

    private void startPoll(int delay) {
        this.logger.info("Starting poll job with refresh of {} seconds.", delay);
        if (this.pollJob != null) {
            this.pollJob.cancel(true);
            this.pollJob = null;
        }

        this.pollJob = this.scheduler.scheduleWithFixedDelay(this::pollForStatus, 0L, (long) delay, TimeUnit.SECONDS);
        this.updateState("keycode", UnDefType.UNDEF);
    }

    public void initialize() {
        this.config = (SamsungTVJsonRPCConfiguration) this.getConfigAs(SamsungTVJsonRPCConfiguration.class);
        this.updateStatus(ThingStatus.UNKNOWN);
        if (this.config != null && this.config.hostname != null && !this.config.hostname.isEmpty()
                && !this.config.hostname.equals("null")) {
            try {
                this.jsonRPCHandler = new JsonRPCHandler(this.httpClient, this.config.hostname, this.config.port,
                        this.config.authToken);
            } catch (MalformedURLException | URISyntaxException var2) {
                this.logger.error("Error creating JsonRPCHandler", var2);
                return;
            }
        }

        if (this.jsonRPCHandler == null) {
            this.logger.error("Skipping discovery due to null JsonRPCHandler");
        } else {
            this.scheduler.execute(() -> {
                String token = null;
                String hostname = null;
                this.logger.info("Attempting Discovery");
                if (this.config != null) {
                    if (this.config.authToken != null && !this.config.authToken.isEmpty()) {
                        token = this.config.authToken;
                    }

                    if (this.config.hostname != null && !this.config.hostname.isEmpty()) {
                        hostname = this.config.hostname;
                    }
                }

                if (token == null) {
                    this.logger.info("authToken is null, attempting to get token at {}", hostname);

                    try {
                        token = this.jsonRPCHandler.getAuthToken();
                    } catch (InterruptedException | TimeoutException | ExecutionException var8) {
                        this.logger.error("Error getting JsonRPC Auth Token", var8);
                    }

                    this.logger.info("Got token {}", token);
                    if (token != null && !"".equals(token)) {
                        this.updateStatus(ThingStatus.ONLINE);
                        Configuration conf = this.editConfiguration();
                        conf.put("authToken", token);
                        this.updateConfiguration(conf);
                        this.config.authToken = token;
                        this.startPoll(this.config.refreshInterval);
                    } else {
                        this.updateStatus(ThingStatus.OFFLINE);
                    }
                }

                String deviceName;
                if (token != null && hostname != null
                        && (this.config.macAddress == null || this.config.macAddress.isEmpty())) {
                    this.logger.info("Attempting to discover MAC address at {}", hostname);
                    deviceName = WakeOnLanUtility.getMACAddress(hostname);
                    if (deviceName != null) {
                        Configuration confx = this.editConfiguration();
                        confx.put("macAddress", deviceName);
                        this.updateConfiguration(confx);
                        this.config.macAddress = deviceName;
                        this.updateProperty("macAddress", deviceName);
                    } else {
                        this.logger.error("Error getting mac address for {}", hostname);
                    }
                }

                if (hostname != null) {
                    deviceName = null;
                    TVProperties props = this.jsonRPCHandler.getTVProperties();
                    if (props != null) {
                        this.updateProperty("modelId", props.device.modelName);
                        this.updateProperty("vendor", "Samsung");
                        this.updateProperty("firmwareVersion", props.version);
                        deviceName = props.device.name;
                    }

                    Document dom = this.jsonRPCHandler.getIPControlUPnPDescription();
                    Element element = null;
                    if (dom != null) {
                        NodeList nodeList = dom.getDocumentElement().getElementsByTagName("manufacturer");
                        if (nodeList != null) {
                            element = (Element) nodeList.item(0);
                            this.updateProperty("vendor", element.getTextContent());
                        }

                        nodeList = dom.getDocumentElement().getElementsByTagName("serialNumber");
                        if (nodeList != null) {
                            element = (Element) nodeList.item(0);
                            this.updateProperty("serialNumber", element.getTextContent());
                        }

                        if (props == null) {
                            nodeList = dom.getDocumentElement().getElementsByTagName("modelName");
                            if (nodeList != null) {
                                element = (Element) nodeList.item(0);
                                this.updateProperty("modelId", element.getTextContent());
                            }

                            nodeList = dom.getDocumentElement().getElementsByTagName("friendlyName");
                            if (nodeList != null) {
                                element = (Element) nodeList.item(0);
                                deviceName = element.getTextContent();
                            }
                        }
                    }

                    if (deviceName != null) {
                        this.updateProperty("name", deviceName);
                        ThingBuilder thing = this.editThing();
                        thing.withLabel(String.format("%s (JSON-RPC)", deviceName));
                        this.updateThing(thing.build());
                    }
                }

                if (token != null) {
                    this.startPoll(this.config.refreshInterval);
                }

            });
        }
    }
}
