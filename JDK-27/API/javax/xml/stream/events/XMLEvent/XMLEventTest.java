/*
 * @test
 * @summary javax.xml.stream.events.XMLEvent conformance: white-box structure + black-box behavior.
 * @run main XMLEventTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLEventTest {
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

        Class<?> c = Class.forName("javax.xml.stream.events.XMLEvent");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "javax.xml.stream.XMLStreamConstants"), "interface javax.xml.stream.XMLStreamConstants");
        check(hasMethod(c, "asCharacters"), "method asCharacters");
        check(hasMethod(c, "asEndElement"), "method asEndElement");
        check(hasMethod(c, "asStartElement"), "method asStartElement");
        check(hasMethod(c, "getEventType"), "method getEventType");
        check(hasMethod(c, "getLocation"), "method getLocation");
        check(hasMethod(c, "getSchemaType"), "method getSchemaType");
        check(hasMethod(c, "isAttribute"), "method isAttribute");
        check(hasMethod(c, "isCharacters"), "method isCharacters");
        check(hasMethod(c, "isEndDocument"), "method isEndDocument");
        check(hasMethod(c, "isEndElement"), "method isEndElement");
        check(hasMethod(c, "isEntityReference"), "method isEntityReference");
        check(hasMethod(c, "isNamespace"), "method isNamespace");
        check(hasMethod(c, "isProcessingInstruction"), "method isProcessingInstruction");
        check(hasMethod(c, "isStartDocument"), "method isStartDocument");
        check(hasMethod(c, "isStartElement"), "method isStartElement");
        check(hasMethod(c, "writeAsEncodedUnicode"), "method writeAsEncodedUnicode");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.stream.events.XMLEvent a = new javax.xml.stream.events.XMLEvent(){
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
        check(a != null, "anon impl of XMLEvent");
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

    // ---- Part 2: contract deepening (real events via XMLEventFactory + XMLEventReader) ----
    static void testDeep() throws Exception {
        // 1) event-code family: XMLStreamConstants exact values
        check(javax.xml.stream.XMLStreamConstants.START_ELEMENT == 1, "START_ELEMENT=1");
        check(javax.xml.stream.XMLStreamConstants.END_ELEMENT == 2, "END_ELEMENT=2");
        check(javax.xml.stream.XMLStreamConstants.PROCESSING_INSTRUCTION == 3, "PROCESSING_INSTRUCTION=3");
        check(javax.xml.stream.XMLStreamConstants.CHARACTERS == 4, "CHARACTERS=4");
        check(javax.xml.stream.XMLStreamConstants.COMMENT == 5, "COMMENT=5");
        check(javax.xml.stream.XMLStreamConstants.SPACE == 6, "SPACE=6");
        check(javax.xml.stream.XMLStreamConstants.START_DOCUMENT == 7, "START_DOCUMENT=7");
        check(javax.xml.stream.XMLStreamConstants.END_DOCUMENT == 8, "END_DOCUMENT=8");
        check(javax.xml.stream.XMLStreamConstants.ENTITY_REFERENCE == 9, "ENTITY_REFERENCE=9");
        check(javax.xml.stream.XMLStreamConstants.ATTRIBUTE == 10, "ATTRIBUTE=10");
        check(javax.xml.stream.XMLStreamConstants.DTD == 11, "DTD=11");
        check(javax.xml.stream.XMLStreamConstants.CDATA == 12, "CDATA=12");
        check(javax.xml.stream.XMLStreamConstants.NAMESPACE == 13, "NAMESPACE=13");
        check(javax.xml.stream.XMLStreamConstants.NOTATION_DECLARATION == 14, "NOTATION_DECLARATION=14");
        check(javax.xml.stream.XMLStreamConstants.ENTITY_DECLARATION == 15, "ENTITY_DECLARATION=15");

        // 2) walk a fixed document: per-event type code + isXxx family + asXxx identity
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventReader r = ifac.createXMLEventReader(new java.io.StringReader("<r>ab</r>"));
        javax.xml.stream.events.XMLEvent sd = r.nextEvent();
        check(sd.getEventType() == 7, "SD code 7");
        check(sd.isStartDocument() && !sd.isStartElement() && !sd.isEndElement() && !sd.isCharacters()
                && !sd.isAttribute() && !sd.isNamespace() && !sd.isEntityReference() && !sd.isProcessingInstruction() && !sd.isEndDocument(),
                "SD isXxx family exactly one true");
        check(sd.getLocation().getLineNumber() == 1 && sd.getLocation().getColumnNumber() == 1
                && sd.getLocation().getCharacterOffset() == 0, "SD location (1,1,0)");
        javax.xml.stream.events.XMLEvent se = r.nextEvent();
        check(se.getEventType() == 1 && se.isStartElement() && se.asStartElement() == se, "SE code + self cast");
        javax.xml.stream.events.XMLEvent ch = r.nextEvent();
        check(ch.getEventType() == 4 && ch.isCharacters() && ch.asCharacters() == ch, "CH code + self cast");
        javax.xml.stream.events.XMLEvent ee = r.nextEvent();
        check(ee.getEventType() == 2 && ee.isEndElement() && ee.asEndElement() == ee, "EE code + self cast");
        javax.xml.stream.events.XMLEvent ed = r.nextEvent();
        check(ed.getEventType() == 8 && ed.isEndDocument(), "ED code 8");
        check(ed.getLocation().getLineNumber() == -1 && ed.getLocation().getColumnNumber() == -1
                && ed.getLocation().getCharacterOffset() == -1, "ED location all -1");
        check(!r.hasNext(), "reader exhausted");
        try { r.nextEvent(); check(false, "nextEvent at end must throw"); }
        catch (java.util.NoSuchElementException nsee) { passed++; }
        r.close();

        // 3) ClassCastException matrix on wrong casts
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();
        javax.xml.stream.events.Characters cc = ef.createCharacters("x");
        try { cc.asStartElement(); check(false, "Characters.asStartElement must CCE"); }
        catch (ClassCastException e) { passed++; }
        try { cc.asEndElement(); check(false, "Characters.asEndElement must CCE"); }
        catch (ClassCastException e) { passed++; }
        javax.xml.stream.events.StartElement s2 = ef.createStartElement("p", "http://p", "l");
        try { s2.asCharacters(); check(false, "StartElement.asCharacters must CCE"); }
        catch (ClassCastException e) { passed++; }
        check(s2.asStartElement() == s2, "factory StartElement asStartElement self");
        check(s2.getEventType() == 1 && s2.isStartElement(), "factory StartElement code 1");

        // 4) writeAsEncodedUnicode: escaping of <&>"' (single quote kept raw), null Writer -> NPE
        java.io.StringWriter w = new java.io.StringWriter();
        ef.createCharacters("<&>\"'").writeAsEncodedUnicode(w);
        check("&lt;&amp;&gt;&quot;'".equals(w.toString()), "encoded escaping <&>\"' -> &lt;&amp;&gt;&quot;'");
        try { ef.createCharacters("x").writeAsEncodedUnicode(null); check(false, "null Writer must NPE"); }
        catch (NullPointerException npe) { passed++; }

        // 5) getSchemaType is null for non-schema events
        check(ef.createCharacters("x").getSchemaType() == null, "Characters getSchemaType null");
        check(ef.createStartElement("l", "", "").getSchemaType() == null, "StartElement getSchemaType null");

        // 6) factory default Location is a non-null dummy with -1/-1/-1 and null ids
        javax.xml.stream.Location dl = ef.createCharacters("x").getLocation();
        check(dl != null && dl.getLineNumber() == -1 && dl.getColumnNumber() == -1
                && dl.getCharacterOffset() == -1 && dl.getPublicId() == null && dl.getSystemId() == null,
                "factory default dummy location");

        // 7) setLocation wiring is copied into every created event
        javax.xml.stream.Location fix = new javax.xml.stream.Location() {
            public int getLineNumber() { return 10; }
            public int getColumnNumber() { return 20; }
            public int getCharacterOffset() { return 30; }
            public String getPublicId() { return "pub"; }
            public String getSystemId() { return "sys"; }
        };
        ef.setLocation(fix);
        javax.xml.stream.Location fl = ef.createCharacters("x").getLocation();
        check(fl.getLineNumber() == 10 && fl.getColumnNumber() == 20 && fl.getCharacterOffset() == 30
                && "pub".equals(fl.getPublicId()) && "sys".equals(fl.getSystemId()), "setLocation wired");
        ef.setLocation(null);
        check(ef.createCharacters("y").getLocation() != null, "setLocation(null) still non-null location");

        // 7b) JDK-27+ only: XMLEventFactory.newDefaultFactory() bypasses the lookup
        javax.xml.stream.XMLEventFactory ndf = javax.xml.stream.XMLEventFactory.newDefaultFactory();
        check(ndf != null && "d".equals(ndf.createCharacters("d").getData()), "newDefaultFactory works");

        // 8) peek does not consume: peek twice then next yields the same event type
        javax.xml.stream.XMLEventReader r2 = ifac.createXMLEventReader(new java.io.StringReader("<r/>"));
        check(r2.peek().getEventType() == 7 && r2.peek().getEventType() == 7 && r2.nextEvent().getEventType() == 7,
                "peek twice non-consuming");
        while (r2.hasNext()) r2.nextEvent();
        check(r2.peek() == null, "peek at end null");
        r2.close();

        // 9) DTD and ENTITY_REFERENCE events carry their spec codes (11 and 9)
        javax.xml.stream.XMLInputFactory nf = javax.xml.stream.XMLInputFactory.newFactory();
        nf.setProperty(javax.xml.stream.XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
        javax.xml.stream.XMLEventReader r3 = nf.createXMLEventReader(
                new java.io.StringReader("<!DOCTYPE r [<!ENTITY e 'H'>]><r>&e;</r>"));
        boolean sawDtd = false, sawEr = false;
        while (r3.hasNext()) {
            javax.xml.stream.events.XMLEvent e = r3.nextEvent();
            if (e instanceof javax.xml.stream.events.DTD) {
                sawDtd = true;
                check(e.getEventType() == 11, "DTD code 11");
            }
            if (e.isEntityReference()) {
                sawEr = true;
                check(e.getEventType() == 9, "ENTITY_REFERENCE code 9");
                try { e.asStartElement(); check(false, "ER.asStartElement must CCE"); }
                catch (ClassCastException cce) { passed++; }
            }
        }
        check(sawDtd && sawEr, "DTD and ER events both observed");
        r3.close();
    }
}
