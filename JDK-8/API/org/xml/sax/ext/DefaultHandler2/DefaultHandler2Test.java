/*
 * @test
 * @summary org.xml.sax.ext.DefaultHandler2 conformance: default handler2.
 * @run main DefaultHandler2Test
 */


public class DefaultHandler2Test {
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
        org.xml.sax.ext.DefaultHandler2 h = new org.xml.sax.ext.DefaultHandler2();
        check(h instanceof org.xml.sax.ext.LexicalHandler, "is LexicalHandler");
        check(h instanceof org.xml.sax.ext.DeclHandler, "is DeclHandler");
        check(h instanceof org.xml.sax.ext.EntityResolver2, "is EntityResolver2");
        h.startCDATA();
        h.endCDATA();
        h.comment(new char[]{'-', '-'}, 0, 2);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
