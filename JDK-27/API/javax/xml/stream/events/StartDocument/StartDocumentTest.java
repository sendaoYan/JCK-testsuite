/*
 * @test
 * @summary javax.xml.stream.events.StartDocument conformance: white-box structure + black-box behavior.
 * @run main StartDocumentTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class StartDocumentTest {
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

        Class<?> c = Class.forName("javax.xml.stream.events.StartDocument");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.stream.events.XMLEvent"), "interface javax.xml.stream.events.XMLEvent");
        check(hasMethod(c, "encodingSet"), "method encodingSet");
        check(hasMethod(c, "getCharacterEncodingScheme"), "method getCharacterEncodingScheme");
        check(hasMethod(c, "getSystemId"), "method getSystemId");
        check(hasMethod(c, "getVersion"), "method getVersion");
        check(hasMethod(c, "isStandalone"), "method isStandalone");
        check(hasMethod(c, "standaloneSet"), "method standaloneSet");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.stream.events.StartDocument a = new javax.xml.stream.events.StartDocument(){
            public java.lang.String getSystemId() { return "s"; }
            public java.lang.String getCharacterEncodingScheme() { return "s"; }
            public boolean encodingSet() { return true; }
            public boolean isStandalone() { return true; }
            public boolean standaloneSet() { return true; }
            public java.lang.String getVersion() { return "s"; }
            public int getEventType() { return 3; }
            public javax.xml.stream.Location getLocation() { return null; }
            public boolean isStartElement() { return true; }
            public boolean isAttribute() { return true; }
            public boolean isNamespace() { return true; }
            public boolean isEndElement() { return true; }
            public boolean isEntityReference() { return true; }
            public boolean isProcessingInstruction() { return true; }
            public boolean isCharacters() { return true; }
            public boolean isStartDocument() { return true; }
            public boolean isEndDocument() { return true; }
            public javax.xml.stream.events.StartElement asStartElement() { return null; }
            public javax.xml.stream.events.EndElement asEndElement() { return null; }
            public javax.xml.stream.events.Characters asCharacters() { return null; }
            public javax.xml.namespace.QName getSchemaType() { return null; }
            public void writeAsEncodedUnicode(java.io.Writer p0) { }
        };
        check(a != null, "anon impl of StartDocument");
        check("s".equals(a.getSystemId()), "getSystemId() returns fixed default");
        check("s".equals(a.getCharacterEncodingScheme()), "getCharacterEncodingScheme() returns fixed default");
        check(a.encodingSet() == true, "encodingSet() returns fixed default");
        check(a.isStandalone() == true, "isStandalone() returns fixed default");
        check(a.standaloneSet() == true, "standaloneSet() returns fixed default");
        check("s".equals(a.getVersion()), "getVersion() returns fixed default");
        check(a.getEventType() == 3, "getEventType() returns fixed default");
        check(a.getLocation() == null, "getLocation() returns fixed default");
        check(a.isStartElement() == true, "isStartElement() returns fixed default");
        check(a.isAttribute() == true, "isAttribute() returns fixed default");
        check(a.isNamespace() == true, "isNamespace() returns fixed default");
        check(a.isEndElement() == true, "isEndElement() returns fixed default");
        check(a.isEntityReference() == true, "isEntityReference() returns fixed default");
        check(a.isProcessingInstruction() == true, "isProcessingInstruction() returns fixed default");
        check(a.isCharacters() == true, "isCharacters() returns fixed default");
        check(a.isStartDocument() == true, "isStartDocument() returns fixed default");
        check(a.isEndDocument() == true, "isEndDocument() returns fixed default");
        check(a.asStartElement() == null, "asStartElement() returns fixed default");
        check(a.asEndElement() == null, "asEndElement() returns fixed default");
        check(a.asCharacters() == null, "asCharacters() returns fixed default");
        check(a.getSchemaType() == null, "getSchemaType() returns fixed default");
        a.writeAsEncodedUnicode((java.io.Writer) null);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract deepening (XMLDecl forms matrix + factory forms) ----
    // JDK-8 impl reports standaloneSet()==true even when standalone is absent from the decl;
    // JDK-27 reports the spec-correct false. Sides differ only in this constant.
    static final boolean SA_SET_WHEN_ABSENT = false;

    static void testDeep() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();

        // 1) parsed XMLDecl forms matrix (Reader input: character encoding scheme reported null)
        javax.xml.stream.events.StartDocument d1 = (javax.xml.stream.events.StartDocument)
                ifac.createXMLEventReader(new java.io.StringReader(
                        "<?xml version='1.0' encoding='UTF-8' standalone='yes'?><r/>")).nextEvent();
        check(d1.getEventType() == 7 && d1.isStartDocument(), "SD code 7");
        check("1.0".equals(d1.getVersion()) && d1.isStandalone() && d1.standaloneSet() && d1.encodingSet(),
                "decl: version/enc/sa=yes/saSet/encSet");
        check(d1.getCharacterEncodingScheme() == null, "Reader input encoding scheme null (impl)");
        check(d1.getSystemId() == null, "Reader input systemId null");
        javax.xml.stream.events.StartDocument d2 = (javax.xml.stream.events.StartDocument)
                ifac.createXMLEventReader(new java.io.StringReader(
                        "<?xml version='1.0' encoding='UTF-8' standalone='no'?><r/>")).nextEvent();
        check(!d2.isStandalone() && d2.standaloneSet(), "standalone=no: isStandalone false, saSet true");
        javax.xml.stream.events.StartDocument d3 = (javax.xml.stream.events.StartDocument)
                ifac.createXMLEventReader(new java.io.StringReader("<?xml version='1.0'?><r/>")).nextEvent();
        check("1.0".equals(d3.getVersion()) && !d3.isStandalone() && !d3.encodingSet(), "no encoding: encSet false");
        check(d3.standaloneSet() == SA_SET_WHEN_ABSENT, "standalone absent: saSet locked per side");
        javax.xml.stream.events.StartDocument d4 = (javax.xml.stream.events.StartDocument)
                ifac.createXMLEventReader(new java.io.StringReader(
                        "<?xml version='1.0' encoding='UTF-8'?><r/>")).nextEvent();
        check(d4.encodingSet() && !d4.isStandalone(), "encoding without standalone");
        javax.xml.stream.events.StartDocument d5 = (javax.xml.stream.events.StartDocument)
                ifac.createXMLEventReader(new java.io.StringReader("<r/>")).nextEvent();
        check(d5.getVersion() == null && !d5.encodingSet() && !d5.isStandalone(),
                "no decl at all: version null");

        // 2) InputStream input reports the actual encoding
        javax.xml.stream.events.StartDocument d6 = (javax.xml.stream.events.StartDocument)
                ifac.createXMLEventReader(new java.io.ByteArrayInputStream(
                        "<?xml version='1.0' encoding='UTF-8' standalone='yes'?><r/>".getBytes("UTF-8"))).nextEvent();
        check("UTF-8".equals(d6.getCharacterEncodingScheme()) && d6.isStandalone(), "InputStream input encoding UTF-8");

        // 3) Source with systemId propagates into getSystemId
        javax.xml.stream.events.StartDocument d7 = (javax.xml.stream.events.StartDocument)
                ifac.createXMLEventReader(new javax.xml.transform.stream.StreamSource(
                        new java.io.StringReader("<?xml version='1.0'?><r/>"), "http://x/y.xml")).nextEvent();
        check("http://x/y.xml".equals(d7.getSystemId()), "Source systemId propagated");

        // 4) factory forms matrix
        javax.xml.stream.events.StartDocument f1 = ef.createStartDocument();
        check("UTF-8".equals(f1.getCharacterEncodingScheme()) && "1.0".equals(f1.getVersion()),
                "factory no-arg defaults UTF-8/1.0");
        check(f1.isStandalone() && !f1.standaloneSet() && f1.encodingSet(), "factory no-arg standalone flags");
        check("".equals(f1.getSystemId()), "factory no-arg systemId empty string");
        javax.xml.stream.events.StartDocument f2 = ef.createStartDocument("UTF-8", "1.1", true);
        check("1.1".equals(f2.getVersion()) && f2.isStandalone() && f2.standaloneSet() && f2.encodingSet(),
                "factory (enc,ver,true) flags");
        javax.xml.stream.events.StartDocument f3 = ef.createStartDocument("UTF-8", "1.0", false);
        check(!f3.isStandalone() && f3.standaloneSet(), "factory (enc,ver,false) flags");
        javax.xml.stream.events.StartDocument f4 = ef.createStartDocument("ISO-8859-1");
        check("ISO-8859-1".equals(f4.getCharacterEncodingScheme()) && "1.0".equals(f4.getVersion())
                && f4.encodingSet(), "factory (enc) form");
        javax.xml.stream.events.StartDocument f5 = ef.createStartDocument("UTF-16", "1.1");
        check("UTF-16".equals(f5.getCharacterEncodingScheme()) && "1.1".equals(f5.getVersion())
                && !f5.standaloneSet(), "factory (enc,ver) standalone unset");

        // 5) encoded form exact
        java.io.StringWriter w = new java.io.StringWriter();
        f5.writeAsEncodedUnicode(w);
        check("<?xml version=\"1.1\" encoding='UTF-16'?>".equals(w.toString()), "factory SD encoded exact");
        try { f1.asEndElement(); check(false, "SD.asEndElement must CCE"); }
        catch (ClassCastException cce) { passed++; }
    }
}
