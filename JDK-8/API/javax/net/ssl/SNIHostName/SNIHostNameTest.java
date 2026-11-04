/*
 * @test
 * @summary javax.net.ssl.SNIHostName conformance: constructors and getters.
 * @run main SNIHostNameTest
 */

public class SNIHostNameTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.net.ssl.SNIHostName h = new javax.net.ssl.SNIHostName("example.com");
        check("example.com".equals(h.getAsciiName()), "getAsciiName");
        check(h.getType() == javax.net.ssl.StandardConstants.SNI_HOST_NAME, "getType");
        check(h.getEncoded() != null, "getEncoded");
        javax.net.ssl.SNIHostName h2 = new javax.net.ssl.SNIHostName(h.getEncoded());
        check(h.equals(h2), "byte[] ctor equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
