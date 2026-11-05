/*
 * @test
 * @summary javax.xml.stream.EventFilter conformance: white-box structure + black-box behavior.
 * @run main EventFilterTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class EventFilterTest {
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

        Class<?> c = Class.forName("javax.xml.stream.EventFilter");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "accept"), "method accept");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.stream.EventFilter a = new javax.xml.stream.EventFilter(){
            public boolean accept(javax.xml.stream.events.XMLEvent p0) { return true; }
        };
        check(a != null, "anon impl of EventFilter");
        check(a.accept((javax.xml.stream.events.XMLEvent) null) == true, "accept() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFilterMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Deepened 1: accept matrix + integration with createFilteredReader ----
    static void testFilterMatrix() throws Exception {
        javax.xml.stream.XMLEventFactory ef = javax.xml.stream.XMLEventFactory.newFactory();
        javax.xml.stream.events.XMLEvent start = ef.createStartElement("", "", "a");
        javax.xml.stream.events.XMLEvent end = ef.createEndElement("", "", "a");
        javax.xml.stream.events.XMLEvent ch = ef.createCharacters("t");
        javax.xml.stream.events.XMLEvent cm = ef.createComment("c");
        javax.xml.stream.EventFilter all = new javax.xml.stream.EventFilter() {
            public boolean accept(javax.xml.stream.events.XMLEvent e) { return true; }
        };
        javax.xml.stream.EventFilter none = new javax.xml.stream.EventFilter() {
            public boolean accept(javax.xml.stream.events.XMLEvent e) { return false; }
        };
        javax.xml.stream.EventFilter elementsOnly = new javax.xml.stream.EventFilter() {
            public boolean accept(javax.xml.stream.events.XMLEvent e) { return e.isStartElement() || e.isEndElement(); }
        };
        check(all.accept(start) && all.accept(ch), "accept-all passes everything");
        check(!none.accept(start) && !none.accept(ch), "accept-none rejects everything");
        check(elementsOnly.accept(start) && elementsOnly.accept(end)
            && !elementsOnly.accept(ch) && !elementsOnly.accept(cm), "element-only filter discriminates event kinds");
        // integration: only start elements survive
        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventReader src = f.createXMLEventReader(new java.io.StringReader(
            "<r>1<a/>2<b/>3</r>"));
        javax.xml.stream.XMLEventReader fr = f.createFilteredReader(src, elementsOnly);
        java.util.List<String> seen = new java.util.ArrayList<String>();
        while (fr.hasNext()) {
            javax.xml.stream.events.XMLEvent e = fr.nextEvent();
            seen.add(e.isStartElement() ? "S" + e.asStartElement().getName().getLocalPart()
                : "E" + e.asEndElement().getName().getLocalPart());
        }
        fr.close();
        check(seen.equals(java.util.Arrays.asList("Sr", "Sa", "Ea", "Sb", "Eb", "Er")),
            "filtered stream keeps element events only, got " + seen);
        // accept-none: hasNext immediately false
        javax.xml.stream.XMLEventReader src2 = f.createXMLEventReader(new java.io.StringReader("<a><b/></a>"));
        javax.xml.stream.XMLEventReader none2 = f.createFilteredReader(src2, none);
        int n = 0;
        while (none2.hasNext()) { none2.nextEvent(); n++; }
        none2.close();
        check(n == 0, "reject-all filter yields zero events");
        // peek on filtered reader respects the filter
        javax.xml.stream.XMLEventReader src3 = f.createXMLEventReader(new java.io.StringReader("<r>x<a/></r>"));
        javax.xml.stream.XMLEventReader fr3 = f.createFilteredReader(src3, elementsOnly);
        javax.xml.stream.events.XMLEvent pk = fr3.peek();
        check(pk != null && pk.isStartElement() && "r".equals(pk.asStartElement().getName().getLocalPart()),
            "peek on filtered reader skips rejected events");
        fr3.close();
    }
}
