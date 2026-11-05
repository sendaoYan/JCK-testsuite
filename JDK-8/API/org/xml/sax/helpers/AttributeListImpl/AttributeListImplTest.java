/*
 * @test
 * @summary org.xml.sax.helpers.AttributeListImpl conformance: attribute list impl.
 * @run main AttributeListImplTest
 */


public class AttributeListImplTest {
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
        org.xml.sax.helpers.AttributeListImpl l = new org.xml.sax.helpers.AttributeListImpl();
        l.addAttribute("id", "CDATA", "x");
        check(l.getLength() == 1, "length");
        check("id".equals(l.getName(0)), "name");
        check("CDATA".equals(l.getType(0)), "type");
        check("x".equals(l.getValue(0)), "value");
        check("x".equals(l.getValue("id")), "value by name");
        l.clear();
        check(l.getLength() == 0, "cleared");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral AttributeListImpl contract (SAX1 list lifecycle) ----
    static void deepContract() throws Exception {
        org.xml.sax.helpers.AttributeListImpl a = new org.xml.sax.helpers.AttributeListImpl();
        check(a.getLength() == 0, "fresh list empty");
        a.addAttribute("x", "CDATA", "1");
        a.addAttribute("y", "NMTOKEN", "n");
        check(a.getLength() == 2, "addAttribute appends");
        check("x".equals(a.getName(0)) && "y".equals(a.getName(1)), "getName by index");
        check("CDATA".equals(a.getType(0)) && "NMTOKEN".equals(a.getType(1)), "getType by index");
        check("1".equals(a.getValue(0)) && "n".equals(a.getValue(1)), "getValue by index");
        check("CDATA".equals(a.getType("x")) && "1".equals(a.getValue("x")), "lookup by name");
        check(a.getType("missing") == null && a.getValue("missing") == null, "missing name -> null type/value");
        check(a.getName(9) == null && a.getType(9) == null && a.getValue(9) == null, "out-of-range getters -> null (no exception)");
        a.removeAttribute("x");
        check(a.getLength() == 1 && "y".equals(a.getName(0)), "removeAttribute shifts remaining entries");
        org.xml.sax.helpers.AttributeListImpl copy = new org.xml.sax.helpers.AttributeListImpl(a);
        check(copy.getLength() == 1 && "n".equals(copy.getValue("y")), "copy constructor snapshots the list");
        a.addAttribute("z", "CDATA", "3");
        check(copy.getValue("z") == null, "copy is independent of later additions");
        a.clear();
        check(a.getLength() == 0, "clear empties the list");
        check(copy.getLength() == 1, "clear does not affect an earlier copy");
    }
}
