/*
 * @test
 * @summary javax.net.ssl.SSLException conformance: constructors, message, cause and superclass.
 * @run main SSLExceptionTest
 */

public class SSLExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.net.ssl.SSLException e = new javax.net.ssl.SSLException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.io.IOException, "extends java.io.IOException");
        Throwable cau = new Exception("cause");
        javax.net.ssl.SSLException e2 = new javax.net.ssl.SSLException("m", cau);
        check(e2.getCause() == cau, "(String,Throwable) ctor cause");
        Throwable cau2 = new Exception("cause2");
        javax.net.ssl.SSLException e3 = new javax.net.ssl.SSLException(cau2);
        check(e3.getCause() == cau2, "(Throwable) ctor cause");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
