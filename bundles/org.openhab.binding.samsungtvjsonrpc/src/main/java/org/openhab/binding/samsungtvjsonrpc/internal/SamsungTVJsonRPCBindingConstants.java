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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SamsungTVJsonRPCBindingConstants} class holds constants
 *
 * @author Brandon Parker - Initial contribution
 */
@NonNullByDefault
public class SamsungTVJsonRPCBindingConstants {
    public static final String BINDING_ID = "samsungtvjsonrpc";
    public static final ThingTypeUID THING_TYPE_SAMSUNG_TV = new ThingTypeUID("samsungtvjsonrpc", "samsungtv");
    public static final String CHANNEL_INPUT_SOURCE = "inputsource";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_KEYCODE = "keycode";
    public static final String AUTH_TOKEN = "authToken";
    public static final String MAC_ADDRESS = "macAddress";
    public static final String HOST_NAME = "hostname";

    public SamsungTVJsonRPCBindingConstants() {
    }
}
