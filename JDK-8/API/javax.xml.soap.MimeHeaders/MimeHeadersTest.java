/*
 * @test
 * @summary javax.xml.soap.MimeHeaders conformance: mime headers.
 * @run main MimeHeadersTest
 */


public class MimeHeadersTest {
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
        javax.xml.soap.MimeHeaders h = new javax.xml.soap.MimeHeaders();
        h.addHeader("A", "1");
        h.addHeader("A", "2");
        check(h.getHeader("A").length == 2, "two A headers");
        h.setHeader("B", "3");
        check(h.getHeader("B").length == 1 && "3".equals(h.getHeader("B")[0]), "set header");
        h.removeHeader("A");
        check(h.getHeader("A") == null, "removed A");
        h.removeAllHeaders();
        check(h.getHeader("B") == null, "removed all");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
