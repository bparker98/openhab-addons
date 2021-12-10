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

/**
 * The {@link KeyCode} enum defines keycodes
 *
 * @author Brandon Parker - Initial contribution
 */
@NonNullByDefault
public enum KeyCode {
    cursorUp,
    cursorDn,
    cursorLeft,
    cursorRight,
    menu,
    firstScreen,
    enter,
    fastforward,
    rewind,
    play,
    stop,
    pause,
    exit,
    power,
    number1,
    number2,
    number3,
    number4,
    number5,
    number6,
    number7,
    number8,
    number9,
    number0,
    caption,
    dash,
    red,
    green,
    yellow,
    blue,
    volumeUp,
    volumeDn;

    private KeyCode() {
    }
}
