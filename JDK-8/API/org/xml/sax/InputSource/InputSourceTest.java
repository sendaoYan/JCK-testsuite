/*
 * @test
 * @summary org.xml.sax.InputSource conformance: SAX input source.
 * @run main InputSourceTest
 */


public class InputSourceTest {
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
        org.xml.sax.InputSource s = new org.xml.sax.InputSource();
        s.setPublicId("pub");
        check("pub".equals(s.getPublicId()), "publicId");
        s.setSystemId("sys");
        check("sys".equals(s.getSystemId()), "systemId");
        s.setEncoding("UTF-8");
        check("UTF-8".equals(s.getEncoding()), "encoding");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
