/*
 * @test
 * @summary javax.xml.transform.sax.SAXSource conformance: SAX source.
 * @run main SAXSourceTest
 */


public class SAXSourceTest {
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
        javax.xml.transform.sax.SAXSource s = new javax.xml.transform.sax.SAXSource();
        check(s.getXMLReader() == null, "reader null");
        check(s.getInputSource() == null, "input source null");
        s.setSystemId("sysid");
        check("sysid".equals(s.getSystemId()), "systemId");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
