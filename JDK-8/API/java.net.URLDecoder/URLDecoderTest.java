/*
 * @test
 * @summary java.net.URLDecoder conformance: decode UTF-8.
 * @run main URLDecoderTest
 */


public class URLDecoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check("a b c".equals(java.net.URLDecoder.decode("a+b%20c", "UTF-8")), "decode + and %20");
        check("a&b".equals(java.net.URLDecoder.decode("a%26b", "UTF-8")), "decode %26");        System.out.println("PASS (" + passed + " assertions)");
    }
}
