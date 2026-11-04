/*
 * @test
 * @summary org.xml.sax.helpers.XMLReaderAdapter conformance: XML reader adapter.
 * @run main XMLReaderAdapterTest
 */


public class XMLReaderAdapterTest {
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
        org.xml.sax.helpers.XMLReaderAdapter ra = new org.xml.sax.helpers.XMLReaderAdapter();
        check(ra != null, "constructed");
        check(ra instanceof org.xml.sax.Parser, "is Parser");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
