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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.RemoteDeviceIdentity;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SamsungTvDiscoveryParticipant} class does UPNP stuff
 *
 * @author Brandon Parker - Initial contribution
 */
@NonNullByDefault
@Component
public class SamsungTvDiscoveryParticipant implements UpnpDiscoveryParticipant {
    private final Logger logger = LoggerFactory.getLogger(SamsungTvDiscoveryParticipant.class);

    public SamsungTvDiscoveryParticipant() {
    }

    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(SamsungTVJsonRPCBindingConstants.THING_TYPE_SAMSUNG_TV);
    }

    @Nullable
    public DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = this.getThingUID(device);
        if (uid != null) {
            Map<String, Object> properties = new HashMap();
            properties.put("hostname", ((RemoteDeviceIdentity) device.getIdentity()).getDescriptorURL().getHost());
            properties.put("vendor", device.getDetails().getManufacturerDetails().getManufacturer());
            properties.put("serialNumber", device.getDetails().getSerialNumber());
            properties.put("modelId", device.getDetails().getModelDetails().getModelName());
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withRepresentationProperty("hostname").withLabel(this.getLabel(device)).build();
            this.logger.debug("Created a DiscoveryResult for device '{}' with UDN '{}' and properties: {}",
                    new Object[] { device.getDetails().getModelDetails().getModelName(),
                            ((RemoteDeviceIdentity) device.getIdentity()).getUdn().getIdentifierString(), properties });
            return result;
        } else {
            return null;
        }
    }

    private String getLabel(RemoteDevice device) {
        String label = "Samsung TV";

        try {
            label = device.getDetails().getFriendlyName() + "(JSON-RPC)";
        } catch (Exception var4) {
        }

        return label;
    }

    @Nullable
    public ThingUID getThingUID(RemoteDevice device) {
        if (device.getDetails() != null && device.getDetails().getManufacturerDetails() != null) {
            String manufacturer = device.getDetails().getManufacturerDetails().getManufacturer();
            if (manufacturer != null && manufacturer.toUpperCase().contains("SAMSUNG ELECTRONICS")
                    && device.getType() != null && "IPControlServer".equals(device.getType().getType())) {
                String udn = ((RemoteDeviceIdentity) device.getIdentity()).getUdn().getIdentifierString().replace("-",
                        "_");
                String modelName = device.getDetails().getModelDetails().getModelName();
                String friendlyName = device.getDetails().getFriendlyName();
                this.logger.debug("Retrieved Thing UID for a Samsung TV '{}' model '{}' thing with UDN '{}'",
                        new Object[] { friendlyName, modelName, udn });
                return new ThingUID(SamsungTVJsonRPCBindingConstants.THING_TYPE_SAMSUNG_TV, udn);
            }
        }

        return null;
    }
}
