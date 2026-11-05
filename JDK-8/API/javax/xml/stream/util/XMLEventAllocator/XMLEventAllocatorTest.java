/*
 * @test
 * @summary javax.xml.stream.util.XMLEventAllocator conformance: interface hierarchy and methods.
 * @run main XMLEventAllocatorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XMLEventAllocatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.stream.util.XMLEventAllocator");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("newInstance"), "has method newInstance");
        check(names.contains("allocate"), "has method allocate");
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
