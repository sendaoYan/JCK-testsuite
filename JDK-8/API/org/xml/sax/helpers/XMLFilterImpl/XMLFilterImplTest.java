/*
 * @test
 * @summary org.xml.sax.helpers.XMLFilterImpl conformance: XML filter impl.
 * @run main XMLFilterImplTest
 */


public class XMLFilterImplTest {
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
        org.xml.sax.helpers.XMLFilterImpl f = new org.xml.sax.helpers.XMLFilterImpl();
        check(f.getParent() == null, "parent null");
        check(f instanceof org.xml.sax.XMLFilter, "is XMLFilter");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
