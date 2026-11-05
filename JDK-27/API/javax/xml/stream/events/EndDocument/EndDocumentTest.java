/*
 * @test
 * @summary javax.xml.stream.events.EndDocument conformance: white-box structure + black-box behavior.
 * @run main EndDocumentTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class EndDocumentTest {
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

        Class<?> c = Class.forName("javax.xml.stream.events.EndDocument");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.stream.events.XMLEvent"), "interface javax.xml.stream.events.XMLEvent");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.stream.events.EndDocument a = new javax.xml.stream.events.EndDocument(){
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
        check(a != null, "anon impl of EndDocument");
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

    // ---- Part 2: contract deepening (parsed final event + factory form) ----
    static void testDeep() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();

        // 1) parsed EndDocument is the last event: code 8, family, dummy location -1/-1/-1
        javax.xml.stream.XMLEventReader r = ifac.createXMLEventReader(
                new java.io.StringReader("<r>a</r>"));
        javax.xml.stream.events.XMLEvent last = null;
        while (r.hasNext()) last = r.nextEvent();
        check(last != null && last.getEventType() == 8 && last.isEndDocument(), "last event code 8");
        check(!last.isStartDocument() && !last.isStartElement() && !last.isEndElement() && !last.isCharacters()
                && !last.isProcessingInstruction() && !last.isEntityReference() && !last.isAttribute()
                && !last.isNamespace(), "ED isXxx family exactly one true");
        check(last.getLocation().getLineNumber() == -1 && last.getLocation().getColumnNumber() == -1
                && last.getLocation().getCharacterOffset() == -1, "ED location all -1");
        check(last.getSchemaType() == null, "ED getSchemaType null");
        r.close();

        // 2) factory createEndDocument: code 8, empty encoded output
        javax.xml.stream.events.EndDocument fed = ef.createEndDocument();
        check(fed.getEventType() == 8 && fed.isEndDocument(), "factory ED code 8");
        java.io.StringWriter w = new java.io.StringWriter();
        fed.writeAsEncodedUnicode(w);
        check("".equals(w.toString()), "factory ED encodes to empty string");
        try { fed.asCharacters(); check(false, "ED.asCharacters must CCE"); }
        catch (ClassCastException cce) { passed++; }
        try { fed.asStartElement(); check(false, "ED.asStartElement must CCE"); }
        catch (ClassCastException cce) { passed++; }
        try { fed.asEndElement(); check(false, "ED.asEndElement must CCE"); }
        catch (ClassCastException cce) { passed++; }

        // 3) writer round-trip: doc with factory ED event is well-formed and reparses
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        javax.xml.stream.XMLEventWriter ew = javax.xml.stream.XMLOutputFactory.newFactory().createXMLEventWriter(bos);
        ew.add(ef.createStartDocument("UTF-8", "1.0"));
        ew.add(ef.createStartElement("", "", "r"));
        ew.add(ef.createCharacters("v"));
        ew.add(ef.createEndElement("", "", "r"));
        ew.add(ef.createEndDocument());
        ew.flush();
        javax.xml.stream.XMLEventReader rr = ifac.createXMLEventReader(
                new java.io.StringReader(new String(bos.toByteArray(), "UTF-8")));
        javax.xml.stream.events.XMLEvent re = null;
        while (rr.hasNext()) re = rr.nextEvent();
        check(re != null && re.getEventType() == 8, "reparse ends with END_DOCUMENT");
        rr.close();
        ew.close();
    }
}
