/*
 * @test
 * @summary javax.naming.AuthenticationException conformance: message and hierarchy.
 * @run main AuthenticationExceptionTest
 */


public class AuthenticationExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.AuthenticationException e = new javax.naming.AuthenticationException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingSecurityException, "is NamingSecurityException");
        javax.naming.AuthenticationException e0 = new javax.naming.AuthenticationException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
