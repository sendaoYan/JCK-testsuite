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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
