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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
