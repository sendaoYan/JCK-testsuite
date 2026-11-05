/*
 * @test
 * @summary javax.xml.stream.events.ProcessingInstruction conformance: white-box structure + black-box behavior.
 * @run main ProcessingInstructionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class ProcessingInstructionTest {
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

        Class<?> c = Class.forName("javax.xml.stream.events.ProcessingInstruction");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.stream.events.XMLEvent"), "interface javax.xml.stream.events.XMLEvent");
        check(hasMethod(c, "getData"), "method getData");
        check(hasMethod(c, "getTarget"), "method getTarget");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.stream.events.ProcessingInstruction a = new javax.xml.stream.events.ProcessingInstruction(){
            public java.lang.String getTarget() { return "s"; }
            public java.lang.String getData() { return "s"; }
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
        check(a != null, "anon impl of ProcessingInstruction");
        check("s".equals(a.getTarget()), "getTarget() returns fixed default");
        check("s".equals(a.getData()), "getData() returns fixed default");
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

    // ---- Part 2: contract deepening (factory + parsed round-trips) ----
    static void testDeep() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();

        // 1) factory createProcessingInstruction: target/data equality, code, family, encoded
        javax.xml.stream.events.ProcessingInstruction pi = ef.createProcessingInstruction("t", "d");
        check("t".equals(pi.getTarget()) && "d".equals(pi.getData()), "factory PI target/data");
        check(pi.getEventType() == 3, "PI code 3");
        check(pi.isProcessingInstruction() && !pi.isCharacters() && !pi.isStartElement() && !pi.isEndElement(),
                "PI isXxx family");
        java.io.StringWriter w = new java.io.StringWriter();
        pi.writeAsEncodedUnicode(w);
        check("<?t d?>".equals(w.toString()), "PI encoded <?t d?>");
        // null data tolerated; encoded drops the data part
        javax.xml.stream.events.ProcessingInstruction pin = ef.createProcessingInstruction("t", null);
        check(pin.getData() == null && "t".equals(pin.getTarget()), "factory PI null data");
        java.io.StringWriter wn = new java.io.StringWriter();
        pin.writeAsEncodedUnicode(wn);
        check("<?t?>".equals(wn.toString()), "PI null-data encoded <?t?>");
        // null target tolerated by factory
        javax.xml.stream.events.ProcessingInstruction pit = ef.createProcessingInstruction(null, "d");
        check(pit.getTarget() == null, "factory PI null target");
        try { pi.asCharacters(); check(false, "PI.asCharacters must CCE"); }
        catch (ClassCastException cce) { passed++; }

        // 2) parsed PI in prolog and inside element: target/data exact
        javax.xml.stream.XMLEventReader r = ifac.createXMLEventReader(
                new java.io.StringReader("<?pi1 data1?><r><?in mid?></r>"));
        r.nextEvent(); // START_DOCUMENT always first
        javax.xml.stream.events.ProcessingInstruction p1 = (javax.xml.stream.events.ProcessingInstruction) r.nextEvent();
        check("pi1".equals(p1.getTarget()) && "data1".equals(p1.getData()), "prolog PI target/data");
        check(p1.isProcessingInstruction(), "prolog PI isProcessingInstruction");
        r.nextEvent();
        javax.xml.stream.events.ProcessingInstruction p2 = (javax.xml.stream.events.ProcessingInstruction) r.nextEvent();
        check("in".equals(p2.getTarget()) && "mid".equals(p2.getData()), "content PI target/data");
        r.close();

        // 3) parsed PI without data: getData() is empty string (factory null differs)
        javax.xml.stream.XMLEventReader r2 = ifac.createXMLEventReader(
                new java.io.StringReader("<r><?tgt?></r>"));
        r2.nextEvent(); r2.nextEvent();
        javax.xml.stream.events.ProcessingInstruction p3 = (javax.xml.stream.events.ProcessingInstruction) r2.nextEvent();
        check("tgt".equals(p3.getTarget()) && "".equals(p3.getData()), "no-data PI getData empty string");
        r2.close();

        // 4) round-trip: write factory PI into writer, reparse via event reader, values identical
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        javax.xml.stream.XMLEventWriter ew = javax.xml.stream.XMLOutputFactory.newFactory().createXMLEventWriter(bos);
        ew.add(ef.createStartDocument("UTF-8", "1.0"));
        ew.add(ef.createStartElement("", "", "r"));
        ew.add(ef.createProcessingInstruction("xsl", "transform"));
        ew.add(ef.createEndElement("", "", "r"));
        ew.add(ef.createEndDocument());
        ew.flush();
        javax.xml.stream.XMLEventReader rr = ifac.createXMLEventReader(
                new java.io.StringReader(new String(bos.toByteArray(), "UTF-8")));
        boolean found = false;
        while (rr.hasNext()) {
            javax.xml.stream.events.XMLEvent e = rr.nextEvent();
            if (e.getEventType() == 3) {
                javax.xml.stream.events.ProcessingInstruction px = (javax.xml.stream.events.ProcessingInstruction) e;
                check("xsl".equals(px.getTarget()) && "transform".equals(px.getData()), "round-trip PI values");
                found = true;
            }
        }
        check(found, "round-trip PI observed");
        rr.close();
        ew.close();
    }
}
