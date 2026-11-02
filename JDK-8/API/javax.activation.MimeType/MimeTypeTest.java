/*
 * @test
 * @summary javax.activation.MimeType conformance: mime type parsing.
 * @run main MimeTypeTest
 */


public class MimeTypeTest {
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
        javax.activation.MimeType mt = new javax.activation.MimeType("text/plain");
        check("text".equals(mt.getPrimaryType()), "primary type");
        check("plain".equals(mt.getSubType()), "subtype");
        check(mt.match("text/plain"), "match");
        mt.setParameter("charset", "us-ascii");
        check("us-ascii".equals(mt.getParameter("charset")), "parameter");
        check("text/plain".equals(mt.getBaseType()), "base type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
