/*
 * @test
 * @summary javax.xml.transform.stream.StreamResult conformance: stream result.
 * @run main StreamResultTest
 */


public class StreamResultTest {
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
        javax.xml.transform.stream.StreamResult r = new javax.xml.transform.stream.StreamResult();
        check(r.getOutputStream() == null, "output stream null");
        check(r.getWriter() == null, "writer null");
        r.setSystemId("sysid");
        check("sysid".equals(r.getSystemId()), "systemId");
        java.io.StringWriter sw = new java.io.StringWriter();
        javax.xml.transform.stream.StreamResult r2 = new javax.xml.transform.stream.StreamResult(sw);
        check(r2.getWriter() == sw, "writer set");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
