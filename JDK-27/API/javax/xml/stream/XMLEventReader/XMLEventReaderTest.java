/*
 * @test
 * @summary javax.xml.stream.XMLEventReader conformance: white-box structure + black-box behavior.
 * @run main XMLEventReaderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLEventReaderTest {
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

        Class<?> c = Class.forName("javax.xml.stream.XMLEventReader");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasInterface(c, "java.util.Iterator"), "interface java.util.Iterator");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "getElementText"), "method getElementText");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "hasNext"), "method hasNext");
        check(hasMethod(c, "nextEvent"), "method nextEvent");
        check(hasMethod(c, "nextTag"), "method nextTag");
        check(hasMethod(c, "peek"), "method peek");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventReader r = f.createXMLEventReader(new java.io.StringReader("<root><item id='a'>one</item><item id='b'>two</item></root>"));
        check(r.hasNext(), "hasNext");
        javax.xml.stream.events.StartDocument sd = (javax.xml.stream.events.StartDocument) r.nextEvent();
        check(sd.isStartDocument(), "first event start document");
        javax.xml.stream.events.StartElement se = (javax.xml.stream.events.StartElement) r.nextEvent();
        check("root".equals(se.getName().getLocalPart()), "root element");
        javax.xml.stream.events.XMLEvent peeked = r.peek();
        check(peeked.isStartElement() && "item".equals(peeked.asStartElement().getName().getLocalPart()), "peek without consuming");
        javax.xml.stream.events.XMLEvent nxt = r.nextEvent();
        check(nxt.isStartElement() && "item".equals(nxt.asStartElement().getName().getLocalPart()), "next after peek same event");
        // iterate via Iterator interface
        java.util.Iterator it = r;
        int remaining = 0;
        while (it.hasNext()) { javax.xml.stream.events.XMLEvent ev = (javax.xml.stream.events.XMLEvent) it.next(); remaining++; }
        check(remaining > 0, "Iterator iteration reaches remaining events");
        // nextTag convenience on a fresh reader
        javax.xml.stream.XMLEventReader r2 = f.createXMLEventReader(new java.io.StringReader("<a><b/></a>"));
        javax.xml.stream.events.XMLEvent t = r2.nextTag();
        check(t.isStartElement() && "a".equals(t.asStartElement().getName().getLocalPart()), "nextTag start a");
        r2.close(); r.close();
        boolean ok = false;
        try { r2.getElementText(); }
        catch (Exception e) { ok = true; }
        check(ok, "closed reader operations throw");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
