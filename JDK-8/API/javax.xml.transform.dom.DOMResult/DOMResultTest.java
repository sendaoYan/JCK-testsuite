/*
 * @test
 * @summary javax.xml.transform.dom.DOMResult conformance: DOM result.
 * @run main DOMResultTest
 */


public class DOMResultTest {
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
        javax.xml.transform.dom.DOMResult r = new javax.xml.transform.dom.DOMResult();
        check(r.getNode() == null, "node null");
        check(r.getNextSibling() == null, "next sibling null");
        r.setSystemId("sysid");
        check("sysid".equals(r.getSystemId()), "systemId");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
