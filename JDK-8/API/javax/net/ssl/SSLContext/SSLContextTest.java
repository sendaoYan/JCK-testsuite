/*
 * @test
 * @summary javax.net.ssl.SSLContext conformance: getInstance and getters.
 * @run main SSLContextTest
 */

public class SSLContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.net.ssl.SSLContext ctx = javax.net.ssl.SSLContext.getInstance("TLS");
        check("TLS".equals(ctx.getProtocol()), "getProtocol");
        check(ctx.getProvider() != null, "getProvider");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
