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

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The {@link SamsungTvUtils} class does utility stuff
 *
 * @author Brandon Parker - Initial contribution
 */
@NonNullByDefault
public class SamsungTvUtils {
    public SamsungTvUtils() {
    }

    public static HashMap<String, String> buildHashMap(String... data) {
        HashMap<String, String> result = new HashMap();
        if (data.length % 2 != 0) {
            throw new IllegalArgumentException("Odd number of arguments");
        } else {
            String key = null;
            Integer step = -1;
            String[] var7 = data;
            int var6 = data.length;

            for (int var5 = 0; var5 < var6; ++var5) {
                String value = var7[var5];
                step = step + 1;
                switch (step % 2) {
                    case 0:
                        if (value == null) {
                            throw new IllegalArgumentException("Null key value");
                        }

                        key = value;
                        break;
                    case 1:
                        if (key != null) {
                            result.put(key, value);
                        }
                }
            }

            return result;
        }
    }

    @Nullable
    public static Document loadXMLFromString(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            return builder.parse(is);
        } catch (SAXException | IOException | ParserConfigurationException var4) {
            return null;
        }
    }
}
