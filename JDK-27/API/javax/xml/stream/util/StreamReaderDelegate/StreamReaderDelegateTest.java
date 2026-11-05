/*
 * @test
 * @summary javax.xml.stream.util.StreamReaderDelegate conformance: white-box structure + black-box behavior.
 * @run main StreamReaderDelegateTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class StreamReaderDelegateTest {
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

        Class<?> c = Class.forName("javax.xml.stream.util.StreamReaderDelegate");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is concrete class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasInterface(c, "javax.xml.stream.XMLStreamReader"), "interface javax.xml.stream.XMLStreamReader");
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
        check(hasMethod(c, "getParent"), "method getParent");
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
        check(hasMethod(c, "setParent"), "method setParent");
        check(hasMethod(c, "standaloneSet"), "method standaloneSet");

        javax.xml.stream.util.StreamReaderDelegate d = new javax.xml.stream.util.StreamReaderDelegate();
        check(d.getParent() == null, "parent null");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.stream.XMLStreamReader base = javax.xml.stream.XMLInputFactory.newFactory().createXMLStreamReader(new java.io.StringReader("<r/>"));
        javax.xml.stream.util.StreamReaderDelegate d = new javax.xml.stream.util.StreamReaderDelegate(base);
        check(d.getParent() == base, "parent round-trip");
        d.close();
        d.setParent(null); check(d.getParent() == null, "setParent null");
        javax.xml.stream.util.StreamReaderDelegate d2 = new javax.xml.stream.util.StreamReaderDelegate();
        check(d2.getParent() == null, "no-arg empty");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract deepening (override vs passthrough matrix) ----
    static void testDeep() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        String xml = "<?xml version='1.0'?><r a='1' b='2'><c>txt</c><?p d?> tail</r>";

        // 1) override wins: getLocalName overridden while everything else passes through
        javax.xml.stream.XMLStreamReader parent = ifac.createXMLStreamReader(new java.io.StringReader(xml));
        javax.xml.stream.util.StreamReaderDelegate d = new javax.xml.stream.util.StreamReaderDelegate(parent) {
            public String getLocalName() { return "OVR"; }
        };
        check(d.getParent() == parent, "getParent identity");
        d.next(); // START r
        check(d.getEventType() == 1, "passthrough getEventType 1");
        check("OVR".equals(d.getLocalName()), "overridden getLocalName");
        check(d.getName().getLocalPart().equals("r"), "passthrough getName r (not overridden)");
        check(d.getAttributeCount() == 2, "passthrough getAttributeCount 2");
        check("1".equals(d.getAttributeValue(0)) && "2".equals(d.getAttributeValue(1)), "passthrough getAttributeValue(i)");
        check("1".equals(d.getAttributeValue(null, "a")), "passthrough getAttributeValue(ns,l)");
        check("a".equals(d.getAttributeLocalName(0)) && "b".equals(d.getAttributeLocalName(1)), "passthrough getAttributeLocalName(i)");
        check("".equals(d.getAttributePrefix(0)), "passthrough getAttributePrefix empty");
        check("CDATA".equals(d.getAttributeType(0)), "passthrough getAttributeType CDATA");
        check(d.getAttributeNamespace(0) == null, "passthrough getAttributeNamespace null");
        check(d.isAttributeSpecified(0), "passthrough isAttributeSpecified true");
        check(d.getNamespaceURI() == null && "".equals(d.getPrefix()), "passthrough ns null / prefix empty for no-ns element");
        check(d.hasName() && !d.hasText(), "passthrough hasName/hasText");
        check(d.isStartElement() && !d.isEndElement(), "passthrough isStartElement/isEndElement");
        check(d.getLocation().getLineNumber() == 1, "passthrough getLocation line 1");
        check(Boolean.TRUE.equals(d.getProperty("javax.xml.stream.isReplacingEntityReferences")),
                "passthrough getProperty known");
        check(d.getProperty("zzz") == null, "passthrough getProperty unknown null");
        check("1.0".equals(d.getVersion()), "passthrough getVersion 1.0");
        check(d.getEncoding() == null && d.getCharacterEncodingScheme() == null, "passthrough encodings null (Reader)");
        check(!d.isStandalone(), "passthrough isStandalone false (no standalone decl)");
        d.next(); // START c
        d.next(); // CHARACTERS txt
        check("txt".equals(d.getText()), "passthrough getText");
        check(d.getTextLength() == 3, "passthrough getTextLength 3");
        check(new String(d.getTextCharacters(), d.getTextStart(), d.getTextLength()).equals("txt"),
                "passthrough getTextCharacters");
        check(d.isCharacters() && !d.isWhiteSpace(), "passthrough isCharacters/isWhiteSpace");
        d.next(); // END c
        check(d.isEndElement() && "c".equals(d.getName().getLocalPart()), "passthrough end element state");
        d.next(); // PI
        check("p".equals(d.getPITarget()) && "d".equals(d.getPIData()), "passthrough getPITarget/getPIData");
        d.next(); // CHARACTERS tail
        check(!d.isWhiteSpace(), "passthrough non-ws text");
        d.next(); // END r
        d.require(javax.xml.stream.XMLStreamConstants.END_ELEMENT, null, "r");
        passed++; // require(END_ELEMENT, null, "r") passed without exception
        d.next(); // END_DOCUMENT
        check(d.getEventType() == 8 && !d.hasNext(), "passthrough to END_DOCUMENT");
        d.close();

        // 2) constructors: no-arg then setParent vs reader-arg
        javax.xml.stream.util.StreamReaderDelegate d2 = new javax.xml.stream.util.StreamReaderDelegate();
        check(d2.getParent() == null, "no-arg parent null");
        try { d2.hasNext(); check(false, "no-parent hasNext must NPE"); }
        catch (NullPointerException npe) { passed++; }
        try { d2.getEventType(); check(false, "no-parent getEventType must NPE"); }
        catch (NullPointerException npe) { passed++; }
        javax.xml.stream.XMLStreamReader p2 = ifac.createXMLStreamReader(new java.io.StringReader("<r/>"));
        d2.setParent(p2);
        check(d2.getParent() == p2 && d2.hasNext(), "setParent wires reader");
        d2.close();
        javax.xml.stream.util.StreamReaderDelegate d3 =
                new javax.xml.stream.util.StreamReaderDelegate(ifac.createXMLStreamReader(new java.io.StringReader("<r/>")));
        check(d3.getParent() != null, "reader-arg constructor parent");
        d3.close();

        // 3) getElementText passthrough: text content + ends on END_ELEMENT
        javax.xml.stream.util.StreamReaderDelegate d4 = new javax.xml.stream.util.StreamReaderDelegate(
                ifac.createXMLStreamReader(new java.io.StringReader("<c>AB</c>")));
        d4.next();
        check("AB".equals(d4.getElementText()), "getElementText passthrough AB");
        check(d4.getEventType() == 2, "getElementText ends on END_ELEMENT");

        // 4) negative: getElementText in wrong state -> XMLStreamException
        try {
            javax.xml.stream.util.StreamReaderDelegate d5 = new javax.xml.stream.util.StreamReaderDelegate(
                    ifac.createXMLStreamReader(new java.io.StringReader("<r><c/></r>")));
            d5.getElementText();
            check(false, "getElementText wrong state must throw");
        } catch (javax.xml.stream.XMLStreamException xse) {
            check(String.valueOf(xse.getMessage()).contains("START_ELEMENT"), "wrong-state message mentions START_ELEMENT");
        }

        // 5) negative: nextTag over non-whitespace text -> XMLStreamException
        try {
            javax.xml.stream.util.StreamReaderDelegate d6 = new javax.xml.stream.util.StreamReaderDelegate(
                    ifac.createXMLStreamReader(new java.io.StringReader("<r>abc</r>")));
            d6.next();
            d6.nextTag();
            check(false, "nextTag on text must throw");
        } catch (javax.xml.stream.XMLStreamException xse) {
            check(String.valueOf(xse.getMessage()).contains("CHARACTERS"), "nextTag message mentions CHARACTERS");
        }

        // 6) nextTag skips inter-element whitespace
        javax.xml.stream.util.StreamReaderDelegate d7 = new javax.xml.stream.util.StreamReaderDelegate(
                ifac.createXMLStreamReader(new java.io.StringReader("<r>  <c/>  </r>")));
        check(d7.nextTag() == 1 && "r".equals(d7.getLocalName()), "nextTag lands on START r");
        check(d7.nextTag() == 1 && "c".equals(d7.getLocalName()), "nextTag skips whitespace to START c");
        check(d7.nextTag() == 2, "nextTag reaches END c");
        d7.close();
    }
}
