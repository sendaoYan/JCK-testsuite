/*
 * @test
 * @summary javax.naming.NoPermissionException conformance: message and hierarchy.
 * @run main NoPermissionExceptionTest
 */


public class NoPermissionExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.NoPermissionException e = new javax.naming.NoPermissionException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingSecurityException, "is NamingSecurityException");
        javax.naming.NoPermissionException e0 = new javax.naming.NoPermissionException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
