/*
 * @test
 * @summary javax.xml.stream.util.EventReaderDelegate conformance: event reader delegate.
 * @run main EventReaderDelegateTest
 */


public class EventReaderDelegateTest {
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
        javax.xml.stream.XMLInputFactory f = javax.xml.stream.XMLInputFactory.newFactory();
        javax.xml.stream.XMLEventReader rdr = f.createXMLEventReader(new java.io.StringReader("<a/>"));
        javax.xml.stream.util.EventReaderDelegate d = new javax.xml.stream.util.EventReaderDelegate();
        check(d.getParent() == null, "parent null");
        d.setParent(rdr);
        check(d.getParent() == rdr, "parent set");
        check(d.hasNext(), "hasNext");
        d.close();
        testDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract deepening (override vs passthrough matrix) ----
    static void testDeep() throws Exception {
        javax.xml.stream.XMLInputFactory ifac = javax.xml.stream.XMLInputFactory.newFactory();
        final javax.xml.stream.events.XMLEvent marker =
                javax.xml.stream.XMLEventFactory.newFactory().createCharacters("MARKER");

        // 1) override wins: peek() overridden to return marker while nextEvent etc. pass through
        javax.xml.stream.XMLEventReader parent = ifac.createXMLEventReader(
                new java.io.StringReader("<r><c>x</c></r>"));
        javax.xml.stream.util.EventReaderDelegate d = new javax.xml.stream.util.EventReaderDelegate(parent) {
            public javax.xml.stream.events.XMLEvent peek() { return marker; }
        };
        check(d.getParent() == parent, "getParent identity");
        check(d.peek() == marker, "overridden peek returns marker");
        check(d.nextEvent().getEventType() == 7, "passthrough nextEvent START_DOCUMENT");
        check(d.hasNext(), "passthrough hasNext");
        javax.xml.stream.events.XMLEvent n = (javax.xml.stream.events.XMLEvent) d.next();
        check(n != null && n.getEventType() == 1, "passthrough Iterator.next START_ELEMENT");
        check(d.peek() == marker, "peek still overridden mid-stream");
        // getElementText passthrough: position on <c> then read text
        javax.xml.stream.util.EventReaderDelegate d2 = new javax.xml.stream.util.EventReaderDelegate(
                ifac.createXMLEventReader(new java.io.StringReader("<c>AB</c>")));
        d2.nextEvent(); // START_DOCUMENT
        d2.nextEvent(); // START c
        check("AB".equals(d2.getElementText()), "passthrough getElementText AB");
        check(d2.nextEvent().getEventType() == 8, "getElementText consumed to END then END_DOCUMENT");
        d2.close();
        // getProperty passthrough
        javax.xml.stream.util.EventReaderDelegate d3 = new javax.xml.stream.util.EventReaderDelegate(
                ifac.createXMLEventReader(new java.io.StringReader("<c/>")));
        check(Boolean.FALSE.equals(d3.getProperty("javax.xml.stream.isValidating")),
                "passthrough getProperty isValidating false");
        check(d3.getProperty("zzz") == null, "passthrough getProperty unknown null");
        // nextTag passthrough skips whitespace
        javax.xml.stream.util.EventReaderDelegate d4 = new javax.xml.stream.util.EventReaderDelegate(
                ifac.createXMLEventReader(new java.io.StringReader("<r>  <c/>  </r>")));
        check(d4.nextTag().getEventType() == 1, "passthrough nextTag START r");
        check(d4.nextTag().getEventType() == 1, "passthrough nextTag START c");
        // remove unsupported
        try { d4.remove(); check(false, "remove must throw"); }
        catch (UnsupportedOperationException uoe) { passed++; }
        d4.close();
        d.close();

        // 2) constructors and parent lifecycle
        javax.xml.stream.util.EventReaderDelegate d5 = new javax.xml.stream.util.EventReaderDelegate();
        check(d5.getParent() == null, "no-arg parent null");
        try { d5.hasNext(); check(false, "no-parent hasNext must NPE"); }
        catch (NullPointerException npe) { passed++; }
        try { d5.nextEvent(); check(false, "no-parent nextEvent must NPE"); }
        catch (NullPointerException npe) { passed++; }
        try { d5.peek(); check(false, "no-parent peek must NPE"); }
        catch (NullPointerException npe) { passed++; }
        try { d5.getProperty("x"); check(false, "no-parent getProperty must NPE"); }
        catch (NullPointerException npe) { passed++; }
        javax.xml.stream.XMLEventReader p6 = ifac.createXMLEventReader(new java.io.StringReader("<r/>"));
        d5.setParent(p6);
        check(d5.getParent() == p6 && d5.hasNext(), "setParent wires reader");
        d5.close();
        d5.setParent(null);
        check(d5.getParent() == null, "setParent(null) clears");

        // 3) full-flow passthrough: event sequence equals the raw parent sequence
        javax.xml.stream.XMLEventReader raw = ifac.createXMLEventReader(
                new java.io.StringReader("<r a='1'>t</r>"));
        javax.xml.stream.util.EventReaderDelegate d7 = new javax.xml.stream.util.EventReaderDelegate(
                ifac.createXMLEventReader(new java.io.StringReader("<r a='1'>t</r>")));
        while (raw.hasNext()) {
            check(raw.nextEvent().getEventType() == d7.nextEvent().getEventType(), "delegate mirrors raw sequence");
        }
        check(!d7.hasNext(), "delegate exhausted with parent");
        d7.close();
        raw.close();

        // 4) negative: getElementText before first event -> NPE; after START_DOCUMENT -> XMLStreamException
        javax.xml.stream.util.EventReaderDelegate d8 = new javax.xml.stream.util.EventReaderDelegate(
                ifac.createXMLEventReader(new java.io.StringReader("<c>a</c>")));
        try { d8.getElementText(); check(false, "getElementText before first event must throw"); }
        catch (NullPointerException npe) { passed++; }
        javax.xml.stream.util.EventReaderDelegate d9 = new javax.xml.stream.util.EventReaderDelegate(
                ifac.createXMLEventReader(new java.io.StringReader("<c>a</c>")));
        d9.nextEvent(); // START_DOCUMENT
        try { d9.getElementText(); check(false, "getElementText on START_DOCUMENT must throw"); }
        catch (javax.xml.stream.XMLStreamException xse) { passed++; }
        d9.close();
    }
}
