/*
 * @test
 * @summary javax.xml.stream.StreamFilter conformance: interface hierarchy and methods.
 * @run main StreamFilterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class StreamFilterTest {
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
        Class<?> c = Class.forName("javax.xml.stream.StreamFilter");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("accept"), "has method accept");
        testFilterMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Deepened 1: accept contract + createFilteredReader integration ----
    static void testFilterMatrix() throws Exception {
        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLStreamReader r = f.createXMLStreamReader(new java.io.StringReader(
            "<a>1<b>2</b>3<!--c--></a>"));
        r.next(); // START a
        javax.xml.stream.StreamFilter elementsOnly = new javax.xml.stream.StreamFilter() {
            public boolean accept(javax.xml.stream.XMLStreamReader rr) {
                return rr.isStartElement() || rr.isEndElement();
            }
        };
        javax.xml.stream.StreamFilter none = new javax.xml.stream.StreamFilter() {
            public boolean accept(javax.xml.stream.XMLStreamReader rr) { return false; }
        };
        check(elementsOnly.accept(r), "element filter accepts current START");
        r.next(); // CHARACTERS
        check(!elementsOnly.accept(r), "element filter rejects CHARACTERS");
        r.close();
        // integration: text and comments dropped, element events kept (END_DOCUMENT delivered)
        javax.xml.stream.XMLStreamReader src = f.createXMLStreamReader(new java.io.StringReader(
            "<a>1<b>2</b>3<!--c--></a>"));
        javax.xml.stream.XMLStreamReader fr = f.createFilteredReader(src, elementsOnly);
        java.util.List<String> seen = new java.util.ArrayList<String>();
        while (fr.hasNext()) {
            int ev = fr.next();
            if (ev == 1) seen.add("S" + fr.getLocalName());
            else if (ev == 2) seen.add("E" + fr.getLocalName());
            else seen.add("EV" + ev);
        }
        fr.close();
        // JDK impl: the filter primes on the reader's initial START_DOCUMENT/root, so the root's own
        // START is consumed during priming; text/comments dropped; END_DOCUMENT delivered
        check(seen.equals(java.util.Arrays.asList("Sb", "Eb", "Ea", "EV8")),
            "filtered sequence drops text/comments but ends with END_DOCUMENT, got " + seen);
        // reject-all: zero events
        javax.xml.stream.XMLStreamReader src2 = f.createXMLStreamReader(new java.io.StringReader("<a><b/></a>"));
        javax.xml.stream.XMLStreamReader noneR = f.createFilteredReader(src2, none);
        int n = 0;
        while (noneR.hasNext()) { noneR.next(); n++; }
        noneR.close();
        check(n == 0, "reject-all StreamFilter yields zero events");
        // filtered reader still exposes full attribute/namespace state on accepted events
        javax.xml.stream.XMLStreamReader src3 = f.createXMLStreamReader(new java.io.StringReader(
            "<r x='1'><!--c--><k y='2'/></r>"));
        javax.xml.stream.XMLStreamReader fr3 = f.createFilteredReader(src3, elementsOnly);
        fr3.next(); // first accepted event is k (root START consumed during priming)
        check("k".equals(fr3.getLocalName()) && "2".equals(fr3.getAttributeValue(0)), "child attributes intact on filtered reader");
        fr3.close();
    }
}
