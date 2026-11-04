/*
 * @test
 * @summary javax.naming.AuthenticationNotSupportedException conformance: message and hierarchy.
 * @run main AuthenticationNotSupportedExceptionTest
 */


public class AuthenticationNotSupportedExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.AuthenticationNotSupportedException e = new javax.naming.AuthenticationNotSupportedException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingSecurityException, "is NamingSecurityException");
        javax.naming.AuthenticationNotSupportedException e0 = new javax.naming.AuthenticationNotSupportedException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
