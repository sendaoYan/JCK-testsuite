/*
 * @test
 * @summary javax.xml.stream.util.XMLEventAllocator conformance: white-box structure + black-box behavior.
 * @run main XMLEventAllocatorTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.math.*;
import java.security.*;
import java.security.spec.*;

public class XMLEventAllocatorTest {
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

        Class<?> c = Class.forName("javax.xml.stream.util.XMLEventAllocator");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "allocate"), "method allocate");
        check(hasMethod(c, "newInstance"), "method newInstance");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.xml.stream.util.XMLEventAllocator a = new javax.xml.stream.util.XMLEventAllocator(){
            public javax.xml.stream.util.XMLEventAllocator newInstance() { return null; }
            public javax.xml.stream.events.XMLEvent allocate(javax.xml.stream.XMLStreamReader p0) { return null; }
            public void allocate(javax.xml.stream.XMLStreamReader p0, javax.xml.stream.util.XMLEventConsumer p1) { }
        };
        check(a != null, "anon impl of XMLEventAllocator");
        check(a.newInstance() == null, "newInstance() returns fixed default");
        check(a.allocate((javax.xml.stream.XMLStreamReader) null) == null, "allocate() returns fixed default");
        a.allocate((javax.xml.stream.XMLStreamReader) null, (javax.xml.stream.util.XMLEventConsumer) null);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract deepening (allocator discovery closed loop) ----
    static final javax.xml.stream.events.XMLEvent MARKER =
            javax.xml.stream.XMLEventFactory.newFactory().createCharacters("MARKER");

    static void testDeep() throws Exception {
        // 1) custom allocator installed via the "javax.xml.stream.allocator" property
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        javax.xml.stream.util.XMLEventAllocator alloc = new javax.xml.stream.util.XMLEventAllocator() {
            public javax.xml.stream.events.XMLEvent allocate(javax.xml.stream.XMLStreamReader r) {
                calls.add(String.valueOf(r.getEventType()));
                return MARKER;
            }
            public void allocate(javax.xml.stream.XMLStreamReader r,
                    javax.xml.stream.util.XMLEventConsumer consumer)
                    throws javax.xml.stream.XMLStreamException {
                calls.add("c" + r.getEventType());
                consumer.add(MARKER);
            }
            public javax.xml.stream.util.XMLEventAllocator newInstance() {
                return new javax.xml.stream.util.XMLEventAllocator() {
                    public javax.xml.stream.events.XMLEvent allocate(javax.xml.stream.XMLStreamReader r) { return MARKER; }
                    public void allocate(javax.xml.stream.XMLStreamReader r,
                            javax.xml.stream.util.XMLEventConsumer c2) throws javax.xml.stream.XMLStreamException { c2.add(MARKER); }
                    public javax.xml.stream.util.XMLEventAllocator newInstance() { return this; }
                };
            }
        };
        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        f.setProperty("javax.xml.stream.allocator", alloc);
        check(f.getProperty("javax.xml.stream.allocator") == alloc, "allocator property round-trip");
        // 2) discovery closed loop: the reader hands out our events for every parsed event
        javax.xml.stream.XMLEventReader r = f.createXMLEventReader(
                new java.io.StringReader("<r><c>x</c></r>"));
        int events = 0, markers = 0;
        while (r.hasNext()) {
            javax.xml.stream.events.XMLEvent e = r.nextEvent();
            events++;
            if (e == MARKER) markers++;
        }
        r.close();
        check(events == 7 && markers == 7, "all 7 events come from the custom allocator");
        check(calls.size() == 7, "allocate called once per event");

        // 3) direct allocate(XMLStreamReader) at a positioned state
        javax.xml.stream.XMLInputFactory plain = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLStreamReader sr = plain.createXMLStreamReader(new java.io.StringReader("<r/>"));
        sr.next();
        check(alloc.allocate(sr) == MARKER, "allocate returns custom event");
        sr.close();

        // 4) allocate(reader, consumer): consumer receives the allocated event
        final java.util.List<javax.xml.stream.events.XMLEvent> sink =
                new java.util.ArrayList<javax.xml.stream.events.XMLEvent>();
        javax.xml.stream.util.XMLEventConsumer cons =
                new javax.xml.stream.util.XMLEventConsumer() {
                    public void add(javax.xml.stream.events.XMLEvent e) { sink.add(e); }
                };
        javax.xml.stream.XMLStreamReader sr2 = plain.createXMLStreamReader(new java.io.StringReader("<r/>"));
        sr2.next();
        alloc.allocate(sr2, cons);
        check(sink.size() == 1 && sink.get(0) == MARKER, "allocate(reader,consumer) feeds consumer");
        sr2.close();

        // 5) newInstance returns an independent working copy
        javax.xml.stream.util.XMLEventAllocator copy = alloc.newInstance();
        check(copy != null && copy != alloc, "newInstance returns new instance");
        javax.xml.stream.XMLStreamReader sr3 = plain.createXMLStreamReader(new java.io.StringReader("<r/>"));
        sr3.next();
        check(copy.allocate(sr3) == MARKER, "copy allocates same marker");
        sr3.close();

        // 6) wrong-type property value: setProperty does not throw, the stored value is
        //    cast lazily inside createXMLEventReader -> ClassCastException (impl behavior)
        javax.xml.stream.XMLInputFactory wt = javax.xml.stream.XMLInputFactory.newFactory();
        try {
            wt.setProperty("javax.xml.stream.allocator", "not-an-allocator");
            passed++; // setProperty itself tolerates the wrong type (impl)
        } catch (IllegalArgumentException iae) {
            passed++; // rejecting the value at set time is also contract-legal
        }
        try {
            javax.xml.stream.XMLEventReader wr = wt.createXMLEventReader(new java.io.StringReader("<r/>"));
            check(wr.hasNext() && wr.nextEvent().getEventType() == 7, "reader works after wrong-type set");
            wr.close();
        } catch (ClassCastException cce) {
            check(String.valueOf(cce.getMessage()).contains("XMLEventAllocator"),
                    "lazy cast to XMLEventAllocator inside createXMLEventReader");
        }
    }
}
