/*
 * @test
 * @summary javax.xml.stream.XMLInputFactory conformance: white-box structure + black-box behavior.
 * @run main XMLInputFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLInputFactoryTest {
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

        Class<?> c = Class.forName("javax.xml.stream.XMLInputFactory");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "superclass Object");
        check(hasMethod(c, "createFilteredReader"), "method createFilteredReader");
        check(hasMethod(c, "createXMLEventReader"), "method createXMLEventReader");
        check(hasMethod(c, "createXMLStreamReader"), "method createXMLStreamReader");
        check(hasMethod(c, "getEventAllocator"), "method getEventAllocator");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "getXMLReporter"), "method getXMLReporter");
        check(hasMethod(c, "getXMLResolver"), "method getXMLResolver");
        check(hasMethod(c, "isPropertySupported"), "method isPropertySupported");
        check(hasMethod(c, "newDefaultFactory"), "method newDefaultFactory");
        check(hasMethod(c, "newFactory"), "method newFactory");
        check(hasMethod(c, "newInstance"), "method newInstance");
        check(hasMethod(c, "setEventAllocator"), "method setEventAllocator");
        check(hasMethod(c, "setProperty"), "method setProperty");
        check(hasMethod(c, "setXMLReporter"), "method setXMLReporter");
        check(hasMethod(c, "setXMLResolver"), "method setXMLResolver");
        check(hasField(c, "ALLOCATOR"), "field ALLOCATOR");
        check(hasField(c, "IS_COALESCING"), "field IS_COALESCING");
        check(hasField(c, "IS_NAMESPACE_AWARE"), "field IS_NAMESPACE_AWARE");
        check(hasField(c, "IS_REPLACING_ENTITY_REFERENCES"), "field IS_REPLACING_ENTITY_REFERENCES");
        check(hasField(c, "IS_SUPPORTING_EXTERNAL_ENTITIES"), "field IS_SUPPORTING_EXTERNAL_ENTITIES");
        check(hasField(c, "IS_VALIDATING"), "field IS_VALIDATING");
        check(hasField(c, "REPORTER"), "field REPORTER");
        check(hasField(c, "RESOLVER"), "field RESOLVER");
        check(hasField(c, "SUPPORT_DTD"), "field SUPPORT_DTD");

        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLStreamReader r = f.createXMLStreamReader(new java.io.StringReader("<a/>"));
        check(r.hasNext(), "hasNext");
        check(r.next() == javax.xml.stream.XMLStreamConstants.START_ELEMENT, "START_ELEMENT");
        check("a".equals(r.getLocalName()), "localName");
        r.close();
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        check(f != null, "newFactory");
        javax.xml.stream.XMLStreamReader r = f.createXMLStreamReader(new java.io.StringReader("<root><item id='a'>one</item><item id='b'>two</item></root>"));
        StringBuilder events = new StringBuilder();
        while (r.hasNext()) {
            int ev = r.next();
            if (ev == javax.xml.stream.XMLStreamConstants.START_ELEMENT) events.append(r.getLocalName()).append(";");
        }
        r.close();
        check("root;item;item;".equals(events.toString()), "stream reader start elements, got " + events);
        // XMLEventReader
        javax.xml.stream.XMLEventReader er = f.createXMLEventReader(new java.io.StringReader("<root/>"));
        check(er.hasNext() && er.nextEvent().isStartDocument(), "event reader first event start document");
        check(er.nextEvent().isStartElement(), "event reader second event start element");
        er.close();
        // property
        check(f.isPropertySupported(javax.xml.stream.XMLInputFactory.IS_NAMESPACE_AWARE), "IS_NAMESPACE_AWARE supported");
        check(!f.isPropertySupported("http://example.invalid/nope"), "unknown property unsupported");
        boolean ok = false;
        try { f.getProperty("http://example.invalid/nope"); }
        catch (IllegalArgumentException e) { ok = true; }
        check(ok, "getProperty(unknown) -> IllegalArgumentException");
        check(javax.xml.stream.XMLInputFactory.newInstance() != null, "newInstance");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
