/*
 * @test
 * @summary org.xml.sax.HandlerBase conformance: SAX handler base.
 * @run main HandlerBaseTest
 */


public class HandlerBaseTest {
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
        org.xml.sax.HandlerBase h = new org.xml.sax.HandlerBase();
        check(h instanceof org.xml.sax.DocumentHandler, "is DocumentHandler");
        check(h instanceof org.xml.sax.ErrorHandler, "is ErrorHandler");
        h.startDocument();
        h.endDocument();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
