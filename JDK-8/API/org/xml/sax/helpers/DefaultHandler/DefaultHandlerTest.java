/*
 * @test
 * @summary org.xml.sax.helpers.DefaultHandler conformance: default handler.
 * @run main DefaultHandlerTest
 */


public class DefaultHandlerTest {
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
        org.xml.sax.helpers.DefaultHandler h = new org.xml.sax.helpers.DefaultHandler();
        check(h instanceof org.xml.sax.ContentHandler, "is ContentHandler");
        check(h instanceof org.xml.sax.EntityResolver, "is EntityResolver");
        check(h instanceof org.xml.sax.DTDHandler, "is DTDHandler");
        check(h instanceof org.xml.sax.ErrorHandler, "is ErrorHandler");
        h.startDocument();
        h.endDocument();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
