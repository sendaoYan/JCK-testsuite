/*
 * @test
 * @summary javax.xml.stream.events.Characters conformance: white-box structure + black-box behavior.
 * @run main CharactersTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class CharactersTest {
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

        Class<?> c = Class.forName("javax.xml.stream.events.Characters");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.stream.events.XMLEvent"), "interface javax.xml.stream.events.XMLEvent");
        check(hasMethod(c, "getData"), "method getData");
        check(hasMethod(c, "isCData"), "method isCData");
        check(hasMethod(c, "isIgnorableWhiteSpace"), "method isIgnorableWhiteSpace");
        check(hasMethod(c, "isWhiteSpace"), "method isWhiteSpace");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.stream.events.Characters a = new javax.xml.stream.events.Characters(){
            public java.lang.String getData() { return "s"; }
            public boolean isWhiteSpace() { return true; }
            public boolean isCData() { return true; }
            public boolean isIgnorableWhiteSpace() { return true; }
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
        check(a != null, "anon impl of Characters");
        check("s".equals(a.getData()), "getData() returns fixed default");
        check(a.isWhiteSpace() == true, "isWhiteSpace() returns fixed default");
        check(a.isCData() == true, "isCData() returns fixed default");
        check(a.isIgnorableWhiteSpace() == true, "isIgnorableWhiteSpace() returns fixed default");
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

    // ---- Part 2: contract deepening (factory matrix + parsed three-state matrix) ----
    static void testDeep() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();

        // 1) factory three-state matrix: plain / cdata / whitespace / ignorable / empty
        javax.xml.stream.events.Characters plain = ef.createCharacters("abc");
        check("abc".equals(plain.getData()) && !plain.isCData() && !plain.isWhiteSpace() && !plain.isIgnorableWhiteSpace(),
                "plain: F/F/F");
        check(plain.getEventType() == 4 && plain.isCharacters(), "plain code 4");
        javax.xml.stream.events.Characters cd = ef.createCData("x");
        check(cd.isCData() && !cd.isWhiteSpace() && !cd.isIgnorableWhiteSpace(), "cdata: T/F/F");
        javax.xml.stream.events.Characters sp = ef.createSpace("   ");
        check(!sp.isCData() && sp.isWhiteSpace() && !sp.isIgnorableWhiteSpace(), "space: F/T/F");
        check(sp.getEventType() == 4, "createSpace still CHARACTERS code 4");
        javax.xml.stream.events.Characters ig = ef.createIgnorableSpace("\t\n");
        check(!ig.isCData() && ig.isWhiteSpace() && ig.isIgnorableWhiteSpace(), "ignorable: F/T/T");
        javax.xml.stream.events.Characters empty = ef.createCharacters("");
        check("".equals(empty.getData()) && !empty.isWhiteSpace(), "empty string not whitespace");
        javax.xml.stream.events.Characters wsChars = ef.createCharacters("   ");
        check(wsChars.isWhiteSpace(), "createCharacters('   ') isWhiteSpace true");
        // CDATA content with markup chars kept verbatim, encoded wraps in CDATA section
        javax.xml.stream.events.Characters cdv = ef.createCData("<&]]>");
        java.io.StringWriter w = new java.io.StringWriter();
        cdv.writeAsEncodedUnicode(w);
        check("<![CDATA[<&]]>]]>".equals(w.toString()), "cdata encoded exact");
        // createCharacters(null) tolerated
        javax.xml.stream.events.Characters nul = ef.createCharacters(null);
        check(nul.getData() == null, "createCharacters(null) getData null");
        // self cast + wrong casts
        check(plain.asCharacters() == plain, "asCharacters self");
        try { plain.asEndElement(); check(false, "CH.asEndElement must CCE"); }
        catch (ClassCastException cce) { passed++; }
        try { plain.asStartElement(); check(false, "CH.asStartElement must CCE"); }
        catch (ClassCastException cce) { passed++; }

        // 2) parsed plain text: F/F/F
        javax.xml.stream.XMLEventReader r = ifac.createXMLEventReader(new java.io.StringReader("<r>plain text</r>"));
        r.nextEvent(); r.nextEvent();
        javax.xml.stream.events.Characters pc = (javax.xml.stream.events.Characters) r.nextEvent();
        check("plain text".equals(pc.getData()) && !pc.isCData() && !pc.isWhiteSpace() && !pc.isIgnorableWhiteSpace(),
                "parsed plain: F/F/F");
        check(pc.getEventType() == 4, "parsed plain code 4");
        r.close();

        // 3) parsed whitespace between child elements: CHARACTERS(4) with ws=T, ig=F (no DTD -> not ignorable)
        javax.xml.stream.XMLEventReader r2 = ifac.createXMLEventReader(new java.io.StringReader("<r>  <c/>  </r>"));
        r2.nextEvent(); r2.nextEvent();
        javax.xml.stream.events.Characters ws1 = (javax.xml.stream.events.Characters) r2.nextEvent();
        check("  ".equals(ws1.getData()) && ws1.isWhiteSpace() && !ws1.isIgnorableWhiteSpace() && ws1.getEventType() == 4,
                "inter-element whitespace: ws=T ig=F code 4 (SPACE not used)");
        r2.close();

        // 4) parsed CDATA section: impl reports CHARACTERS with isCData()=false (quirk, both sides)
        javax.xml.stream.XMLEventReader r3 = ifac.createXMLEventReader(
                new java.io.StringReader("<r><![CDATA[<cdata&>]]>t1</r>"));
        r3.nextEvent(); r3.nextEvent();
        javax.xml.stream.events.Characters c1 = (javax.xml.stream.events.Characters) r3.nextEvent();
        check("<cdata&>".equals(c1.getData()) && !c1.isCData(), "parsed CDATA keeps text, isCData=false (impl quirk)");
        javax.xml.stream.events.Characters c2 = (javax.xml.stream.events.Characters) r3.nextEvent();
        check("t1".equals(c2.getData()) && !c2.isCData(), "text after CDATA separate event");
        r3.close();

        // 5) default (replacing=true) splits text at predefined entities and char refs
        javax.xml.stream.XMLEventReader r4 = ifac.createXMLEventReader(new java.io.StringReader("<r>t1&amp;t2&#65;</r>"));
        r4.nextEvent(); r4.nextEvent();
        String[] chunks = {"t1", "&", "t2", "A"};
        int i = 0;
        while (i < 4) {
            javax.xml.stream.events.XMLEvent e = r4.nextEvent();
            check(e.isCharacters() && chunks[i].equals(e.asCharacters().getData()), "chunk[" + i + "]=" + chunks[i]);
            i++;
        }
        r4.close();

        // 6) IS_COALESCING=true merges entities + CDATA + text into one event (isCData still false)
        javax.xml.stream.XMLInputFactory cf = javax.xml.stream.XMLInputFactory.newFactory();
        cf.setProperty(javax.xml.stream.XMLInputFactory.IS_COALESCING, true);
        javax.xml.stream.XMLEventReader r5 = cf.createXMLEventReader(
                new java.io.StringReader("<r>t1&amp;t2<![CDATA[<c>]]>z</r>"));
        r5.nextEvent(); r5.nextEvent();
        javax.xml.stream.events.Characters co = (javax.xml.stream.events.Characters) r5.nextEvent();
        check("t1&t2<c>z".equals(co.getData()) && !co.isCData(), "coalesced single event");
        r5.close();
    }
}
