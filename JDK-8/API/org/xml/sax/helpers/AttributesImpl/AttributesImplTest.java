/*
 * @test
 * @summary org.xml.sax.helpers.AttributesImpl conformance: attributes impl.
 * @run main AttributesImplTest
 */


public class AttributesImplTest {
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
        org.xml.sax.helpers.AttributesImpl a = new org.xml.sax.helpers.AttributesImpl();
        a.addAttribute("", "id", "id", "ID", "x");
        check(a.getLength() == 1, "length");
        check("id".equals(a.getLocalName(0)), "local name");
        check("id".equals(a.getQName(0)), "qname");
        check("ID".equals(a.getType(0)), "type");
        check("x".equals(a.getValue(0)), "value");
        a.removeAttribute(0);
        check(a.getLength() == 0, "removed");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral AttributesImpl contract (mutable SAX2 list full matrix) ----
    static void deepContract() throws Exception {
        org.xml.sax.helpers.AttributesImpl a = new org.xml.sax.helpers.AttributesImpl();
        check(a.getLength() == 0 && a.getIndex("q") == -1, "fresh list empty, lookup -1");
        a.addAttribute("urn:p", "x", "p:x", "CDATA", "1");
        a.addAttribute("", "y", "y", "ID", "2");
        check(a.getLength() == 2 && a.getIndex("p:x") == 0 && a.getIndex("y") == 1, "getIndex by qName");
        check(a.getIndex("urn:p", "x") == 0 && a.getIndex("", "y") == 1 && a.getIndex("urn:z", "y") == -1, "getIndex by uri+localName");
        check("urn:p".equals(a.getURI(0)) && "x".equals(a.getLocalName(0)) && "p:x".equals(a.getQName(0)), "uri/local/qName accessors");
        check("CDATA".equals(a.getType(0)) && "1".equals(a.getValue(0)), "type/value by index");
        check("1".equals(a.getValue("p:x")) && "2".equals(a.getValue("y")), "value by qName");
        check("1".equals(a.getValue("urn:p", "x")), "value by uri+localName");
        check(a.getValue("nope") == null && a.getType("nope") == null, "missing lookups -> null");
        // duplicate qNames coexist; by-qName lookup returns the first
        a.addAttribute("urn:q", "x", "p:x", "NMTOKEN", "9");
        check(a.getLength() == 3, "duplicate qName appended as a distinct entry");
        check("1".equals(a.getValue("p:x")) && "9".equals(a.getValue(2)), "by-qName lookup returns the FIRST entry");
        // copy constructor
        org.xml.sax.helpers.AttributesImpl copy = new org.xml.sax.helpers.AttributesImpl(a);
        check(copy.getLength() == 3 && "9".equals(copy.getValue(2)), "copy constructor snapshots all entries");
        a.removeAttribute(2);
        check(copy.getLength() == 3, "copy independent of later removal");
        // mutators
        a.setValue(1, "22");
        a.setType(1, "IDREF");
        a.setURI(1, "urn:n");
        a.setLocalName(1, "yy");
        a.setQName(1, "n:yy");
        check("22".equals(a.getValue("n:yy")) && "IDREF".equals(a.getType("n:yy")) && "urn:n".equals(a.getURI(1)), "setValue/setType/setURI/setLocalName/setQName round-trip");
        a.clear();
        check(a.getLength() == 0, "clear empties the list");
        a.addAttribute(null, "l", "l", "CDATA", "v");
        // out-of-range matrix: getters -> null, setters/removers -> ArrayIndexOutOfBoundsException
        check(a.getURI(5) == null && a.getValue(5) == null && a.getType(5) == null, "out-of-range getters -> null");
        boolean oobSet = false, oobRm = false, oobVal = false;
        try { a.setAttribute(5, null, "z", "z", "CDATA", "1"); } catch (ArrayIndexOutOfBoundsException e) { oobSet = true; }
        try { a.removeAttribute(5); } catch (ArrayIndexOutOfBoundsException e) { oobRm = true; }
        try { a.setValue(5, "x"); } catch (ArrayIndexOutOfBoundsException e) { oobVal = true; }
        check(oobSet && oobRm && oobVal, "out-of-range mutators -> ArrayIndexOutOfBoundsException");
        // JDK quirk (both sides): a null-URI entry poisons the 2-arg getIndex lookup with an NPE
        org.xml.sax.helpers.AttributesImpl withNull = new org.xml.sax.helpers.AttributesImpl();
        withNull.addAttribute(null, "n", "n", "CDATA", "v");
        boolean nullNpe = false;
        try { withNull.getIndex("", "n"); } catch (NullPointerException e) { nullNpe = true; }
        check(nullNpe || withNull.getIndex("", "n") == 0, "null-URI entry in getIndex(uri,local) observed (NPE=" + nullNpe + ")");
        check(withNull.getIndex("n") == 0, "null-URI entry still reachable by qName lookup");
        // setAttribute replaces in place
        a.setAttribute(0, "urn:r", "l2", "l2", "ENUM", "e2");
        check(a.getLength() == 1 && "e2".equals(a.getValue("l2")) && a.getValue("l") == null, "setAttribute replaces the slot in place");
    }
}
