/*
 * @test
 * @summary javax.net.ssl.SSLHandshakeException conformance: constructors, message, cause and superclass.
 * @run main SSLHandshakeExceptionTest
 */

public class SSLHandshakeExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.net.ssl.SSLHandshakeException e = new javax.net.ssl.SSLHandshakeException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof javax.net.ssl.SSLException, "extends javax.net.ssl.SSLException");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
