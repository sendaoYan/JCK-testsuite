/*
 * @test
 * @summary javax.xml.soap.MimeHeader conformance: mime header.
 * @run main MimeHeaderTest
 */


public class MimeHeaderTest {
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
        javax.xml.soap.MimeHeader h = new javax.xml.soap.MimeHeader("Content-Type", "text/plain");
        check("Content-Type".equals(h.getName()), "name");
        check("text/plain".equals(h.getValue()), "value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
