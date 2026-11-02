/*
 * @test
 * @summary javax.xml.stream.XMLStreamReader conformance: API type verification.
 * @run main XMLStreamReaderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLStreamReaderTest {
    static int passed;
    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getName().equals(n)) return true;
        }
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) {
            if (i.getName().equals(n)) return true;
        }
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        try { c.getField(n); return true; } catch (NoSuchFieldException e) { return false; }
    }


    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.stream.XMLStreamReader");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.stream.XMLStreamConstants"), "interface javax.xml.stream.XMLStreamConstants");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "getAttributeCount"), "method getAttributeCount");
        check(hasMethod(c, "getAttributeLocalName"), "method getAttributeLocalName");
        check(hasMethod(c, "getAttributeName"), "method getAttributeName");
        check(hasMethod(c, "getAttributeNamespace"), "method getAttributeNamespace");
        check(hasMethod(c, "getAttributePrefix"), "method getAttributePrefix");
        check(hasMethod(c, "getAttributeType"), "method getAttributeType");
        check(hasMethod(c, "getAttributeValue"), "method getAttributeValue");
        check(hasMethod(c, "getCharacterEncodingScheme"), "method getCharacterEncodingScheme");
        check(hasMethod(c, "getElementText"), "method getElementText");
        check(hasMethod(c, "getEncoding"), "method getEncoding");
        check(hasMethod(c, "getEventType"), "method getEventType");
        check(hasMethod(c, "getLocalName"), "method getLocalName");
        check(hasMethod(c, "getLocation"), "method getLocation");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getNamespaceContext"), "method getNamespaceContext");
        check(hasMethod(c, "getNamespaceCount"), "method getNamespaceCount");
        check(hasMethod(c, "getNamespacePrefix"), "method getNamespacePrefix");
        check(hasMethod(c, "getNamespaceURI"), "method getNamespaceURI");
        check(hasMethod(c, "getPIData"), "method getPIData");
        check(hasMethod(c, "getPITarget"), "method getPITarget");
        check(hasMethod(c, "getPrefix"), "method getPrefix");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "getText"), "method getText");
        check(hasMethod(c, "getTextCharacters"), "method getTextCharacters");
        check(hasMethod(c, "getTextLength"), "method getTextLength");
        check(hasMethod(c, "getTextStart"), "method getTextStart");
        check(hasMethod(c, "getVersion"), "method getVersion");
        check(hasMethod(c, "hasName"), "method hasName");
        check(hasMethod(c, "hasNext"), "method hasNext");
        check(hasMethod(c, "hasText"), "method hasText");
        check(hasMethod(c, "isAttributeSpecified"), "method isAttributeSpecified");
        check(hasMethod(c, "isCharacters"), "method isCharacters");
        check(hasMethod(c, "isEndElement"), "method isEndElement");
        check(hasMethod(c, "isStandalone"), "method isStandalone");
        check(hasMethod(c, "isStartElement"), "method isStartElement");
        check(hasMethod(c, "isWhiteSpace"), "method isWhiteSpace");
        check(hasMethod(c, "next"), "method next");
        check(hasMethod(c, "nextTag"), "method nextTag");
        check(hasMethod(c, "require"), "method require");
        check(hasMethod(c, "standaloneSet"), "method standaloneSet");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
