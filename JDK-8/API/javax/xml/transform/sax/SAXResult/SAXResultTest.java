/*
 * @test
 * @summary javax.xml.transform.sax.SAXResult conformance: SAX result.
 * @run main SAXResultTest
 */


public class SAXResultTest {
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
        javax.xml.transform.sax.SAXResult r = new javax.xml.transform.sax.SAXResult();
        check(r.getHandler() == null, "handler null");
        check(r.getLexicalHandler() == null, "lexical handler null");
        r.setSystemId("sysid");
        check("sysid".equals(r.getSystemId()), "systemId");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
