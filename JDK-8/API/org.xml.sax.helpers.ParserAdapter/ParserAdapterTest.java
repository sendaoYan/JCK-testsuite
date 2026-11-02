/*
 * @test
 * @summary org.xml.sax.helpers.ParserAdapter conformance: parser adapter (no SAX1 driver available).
 * @run main ParserAdapterTest
 */


public class ParserAdapterTest {
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
        check(org.xml.sax.XMLReader.class.isAssignableFrom(org.xml.sax.helpers.ParserAdapter.class), "is XMLReader");
        check(org.xml.sax.DocumentHandler.class.isAssignableFrom(org.xml.sax.helpers.ParserAdapter.class), "is DocumentHandler");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.xml.sax.helpers.ParserAdapter.class.getMethods()) names.add(m.getName());
        check(names.contains("parse"), "has parse");
        check(names.contains("setContentHandler"), "has setContentHandler");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
