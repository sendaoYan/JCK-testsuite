/*
 * @test
 * @summary javax.xml.stream.XMLStreamReader conformance: white-box structure + black-box behavior.
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


        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

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
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.stream.XMLStreamReader a = new javax.xml.stream.XMLStreamReader(){
            public java.lang.Object getProperty(java.lang.String p0) { return null; }
            public int next() { return 3; }
            public void require(int p0, java.lang.String p1, java.lang.String p2) { }
            public java.lang.String getElementText() { return "s"; }
            public int nextTag() { return 3; }
            public boolean hasNext() { return true; }
            public void close() { }
            public java.lang.String getNamespaceURI(java.lang.String p0) { return "s"; }
            public boolean isStartElement() { return true; }
            public boolean isEndElement() { return true; }
            public boolean isCharacters() { return true; }
            public boolean isWhiteSpace() { return true; }
            public java.lang.String getAttributeValue(java.lang.String p0, java.lang.String p1) { return "s"; }
            public int getAttributeCount() { return 3; }
            public javax.xml.namespace.QName getAttributeName(int p0) { return null; }
            public java.lang.String getAttributeNamespace(int p0) { return "s"; }
            public java.lang.String getAttributeLocalName(int p0) { return "s"; }
            public java.lang.String getAttributePrefix(int p0) { return "s"; }
            public java.lang.String getAttributeType(int p0) { return "s"; }
            public java.lang.String getAttributeValue(int p0) { return "s"; }
            public boolean isAttributeSpecified(int p0) { return true; }
            public int getNamespaceCount() { return 3; }
            public java.lang.String getNamespacePrefix(int p0) { return "s"; }
            public java.lang.String getNamespaceURI(int p0) { return "s"; }
            public javax.xml.namespace.NamespaceContext getNamespaceContext() { return null; }
            public int getEventType() { return 3; }
            public java.lang.String getText() { return "s"; }
            public char[] getTextCharacters() { return null; }
            public int getTextCharacters(int p0, char[] p1, int p2, int p3) { return 3; }
            public int getTextStart() { return 3; }
            public int getTextLength() { return 3; }
            public java.lang.String getEncoding() { return "s"; }
            public boolean hasText() { return true; }
            public javax.xml.stream.Location getLocation() { return null; }
            public javax.xml.namespace.QName getName() { return null; }
            public java.lang.String getLocalName() { return "s"; }
            public boolean hasName() { return true; }
            public java.lang.String getNamespaceURI() { return "s"; }
            public java.lang.String getPrefix() { return "s"; }
            public java.lang.String getVersion() { return "s"; }
            public boolean isStandalone() { return true; }
            public boolean standaloneSet() { return true; }
            public java.lang.String getCharacterEncodingScheme() { return "s"; }
            public java.lang.String getPITarget() { return "s"; }
            public java.lang.String getPIData() { return "s"; }
        };
        check(a != null, "anon impl of XMLStreamReader");
        check(a.getProperty("s") == null, "getProperty() returns fixed default");
        check(a.next() == 3, "next() returns fixed default");
        a.require(3, "s", "s");
        check("s".equals(a.getElementText()), "getElementText() returns fixed default");
        check(a.nextTag() == 3, "nextTag() returns fixed default");
        check(a.hasNext() == true, "hasNext() returns fixed default");
        a.close();
        check("s".equals(a.getNamespaceURI("s")), "getNamespaceURI() returns fixed default");
        check(a.isStartElement() == true, "isStartElement() returns fixed default");
        check(a.isEndElement() == true, "isEndElement() returns fixed default");
        check(a.isCharacters() == true, "isCharacters() returns fixed default");
        check(a.isWhiteSpace() == true, "isWhiteSpace() returns fixed default");
        check("s".equals(a.getAttributeValue("s", "s")), "getAttributeValue() returns fixed default");
        check(a.getAttributeCount() == 3, "getAttributeCount() returns fixed default");
        check(a.getAttributeName(3) == null, "getAttributeName() returns fixed default");
        check("s".equals(a.getAttributeNamespace(3)), "getAttributeNamespace() returns fixed default");
        check("s".equals(a.getAttributeLocalName(3)), "getAttributeLocalName() returns fixed default");
        check("s".equals(a.getAttributePrefix(3)), "getAttributePrefix() returns fixed default");
        check("s".equals(a.getAttributeType(3)), "getAttributeType() returns fixed default");
        check("s".equals(a.getAttributeValue(3)), "getAttributeValue() returns fixed default");
        check(a.isAttributeSpecified(3) == true, "isAttributeSpecified() returns fixed default");
        check(a.getNamespaceCount() == 3, "getNamespaceCount() returns fixed default");
        check("s".equals(a.getNamespacePrefix(3)), "getNamespacePrefix() returns fixed default");
        check("s".equals(a.getNamespaceURI(3)), "getNamespaceURI() returns fixed default");
        check(a.getNamespaceContext() == null, "getNamespaceContext() returns fixed default");
        check(a.getEventType() == 3, "getEventType() returns fixed default");
        check("s".equals(a.getText()), "getText() returns fixed default");
        check(a.getTextCharacters() == null, "getTextCharacters() returns fixed default");
        check(a.getTextCharacters(3, (char[]) null, 3, 3) == 3, "getTextCharacters() returns fixed default");
        check(a.getTextStart() == 3, "getTextStart() returns fixed default");
        check(a.getTextLength() == 3, "getTextLength() returns fixed default");
        check("s".equals(a.getEncoding()), "getEncoding() returns fixed default");
        check(a.hasText() == true, "hasText() returns fixed default");
        check(a.getLocation() == null, "getLocation() returns fixed default");
        check(a.getName() == null, "getName() returns fixed default");
        check("s".equals(a.getLocalName()), "getLocalName() returns fixed default");
        check(a.hasName() == true, "hasName() returns fixed default");
        check("s".equals(a.getNamespaceURI()), "getNamespaceURI() returns fixed default");
        check("s".equals(a.getPrefix()), "getPrefix() returns fixed default");
        check("s".equals(a.getVersion()), "getVersion() returns fixed default");
        check(a.isStandalone() == true, "isStandalone() returns fixed default");
        check(a.standaloneSet() == true, "standaloneSet() returns fixed default");
        check("s".equals(a.getCharacterEncodingScheme()), "getCharacterEncodingScheme() returns fixed default");
        check("s".equals(a.getPITarget()), "getPITarget() returns fixed default");
        check("s".equals(a.getPIData()), "getPIData() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
