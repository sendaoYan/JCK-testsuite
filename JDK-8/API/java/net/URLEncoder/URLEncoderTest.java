/*
 * @test
 * @summary java.net.URLEncoder conformance: encode UTF-8.
 * @run main URLEncoderTest
 */


public class URLEncoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check("a+b%26c".equals(java.net.URLEncoder.encode("a b&c", "UTF-8")), "encode space and &");
        check("%C3%BC".equals(java.net.URLEncoder.encode("\u00fc", "UTF-8")), "encode UTF-8 char");        System.out.println("PASS (" + passed + " assertions)");
    }
}
