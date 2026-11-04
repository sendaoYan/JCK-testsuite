/*
 * @test
 * @summary org.xml.sax.ext.Locator2Impl conformance: locator2 impl.
 * @run main Locator2ImplTest
 */


public class Locator2ImplTest {
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
        org.xml.sax.ext.Locator2Impl l = new org.xml.sax.ext.Locator2Impl();
        l.setXMLVersion("1.0");
        check("1.0".equals(l.getXMLVersion()), "xml version");
        l.setEncoding("UTF-8");
        check("UTF-8".equals(l.getEncoding()), "encoding");
        check(l instanceof org.xml.sax.ext.Locator2, "is Locator2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
