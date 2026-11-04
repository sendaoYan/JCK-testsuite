/*
 * @test
 * @summary javax.xml.transform.stream.StreamSource conformance: stream source.
 * @run main StreamSourceTest
 */


public class StreamSourceTest {
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
        javax.xml.transform.stream.StreamSource s = new javax.xml.transform.stream.StreamSource();
        check(s.getInputStream() == null, "input stream null");
        check(s.getReader() == null, "reader null");
        s.setSystemId("sysid");
        check("sysid".equals(s.getSystemId()), "systemId");
        s.setPublicId("pub");
        check("pub".equals(s.getPublicId()), "publicId");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
