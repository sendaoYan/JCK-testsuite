/*
 * @test
 * @summary javax.xml.transform.dom.DOMSource conformance: DOM source.
 * @run main DOMSourceTest
 */


public class DOMSourceTest {
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
        javax.xml.transform.dom.DOMSource s = new javax.xml.transform.dom.DOMSource();
        check(s.getNode() == null, "node null");
        s.setSystemId("sysid");
        check("sysid".equals(s.getSystemId()), "systemId");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
