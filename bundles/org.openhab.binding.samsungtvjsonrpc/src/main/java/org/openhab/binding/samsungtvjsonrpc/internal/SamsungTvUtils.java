//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

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
