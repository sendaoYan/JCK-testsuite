/*
 * @test
 * @summary javax.security.sasl.AuthenticationException conformance: constructors, message, cause and superclass.
 * @run main AuthenticationExceptionTest
 */

public class AuthenticationExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.sasl.AuthenticationException e = new javax.security.sasl.AuthenticationException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof javax.security.sasl.SaslException, "extends javax.security.sasl.SaslException");
        Throwable cau = new Exception("cause");
        javax.security.sasl.AuthenticationException e2 = new javax.security.sasl.AuthenticationException("m", cau);
        check(e2.getCause() == cau, "(String,Throwable) ctor cause");
        javax.security.sasl.AuthenticationException e4 = new javax.security.sasl.AuthenticationException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
