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
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral Locator2Impl contract ----
    static void deepContract() throws Exception {
        org.xml.sax.ext.Locator2Impl l = new org.xml.sax.ext.Locator2Impl();
        check(l.getXMLVersion() == null && l.getEncoding() == null, "fresh Locator2Impl: version/encoding null");
        check(l.getLineNumber() == 0 && l.getColumnNumber() == 0, "fresh Locator2Impl: position 0/0 (inherited impl default)");
        l.setXMLVersion("1.1");
        l.setEncoding("UTF-8");
        check("1.1".equals(l.getXMLVersion()) && "UTF-8".equals(l.getEncoding()), "setXMLVersion/setEncoding round-trip");
        l.setPublicId("pub"); l.setSystemId("sys"); l.setLineNumber(4); l.setColumnNumber(6);
        check("pub".equals(l.getPublicId()) && "sys".equals(l.getSystemId()), "inherited id setters round-trip");
        check(l.getLineNumber() == 4 && l.getColumnNumber() == 6, "inherited position setters round-trip");
        // copy constructor snapshots another locator incl. version/encoding
        org.xml.sax.ext.Locator2Impl copy = new org.xml.sax.ext.Locator2Impl(l);
        check(copy.getXMLVersion().equals("1.1") && copy.getEncoding().equals("UTF-8"), "copy constructor carries version/encoding");
        check(copy.getLineNumber() == 4 && copy.getPublicId().equals("pub"), "copy constructor carries position/ids");
        check(org.xml.sax.helpers.LocatorImpl.class.isAssignableFrom(org.xml.sax.ext.Locator2Impl.class), "Locator2Impl extends LocatorImpl");
    }
}
