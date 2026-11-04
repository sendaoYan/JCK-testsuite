/*
 * @test
 * @summary javax.security.sasl.SaslException conformance: constructors, message, cause and superclass.
 * @run main SaslExceptionTest
 */

public class SaslExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.sasl.SaslException e = new javax.security.sasl.SaslException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.io.IOException, "extends java.io.IOException");
        Throwable cau = new Exception("cause");
        javax.security.sasl.SaslException e2 = new javax.security.sasl.SaslException("m", cau);
        check(e2.getCause() == cau, "(String,Throwable) ctor cause");
        javax.security.sasl.SaslException e4 = new javax.security.sasl.SaslException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
