/*
 * @test
 * @summary javax.naming.directory.InvalidSearchControlsException conformance: message and hierarchy.
 * @run main InvalidSearchControlsExceptionTest
 */


public class InvalidSearchControlsExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.directory.InvalidSearchControlsException e = new javax.naming.directory.InvalidSearchControlsException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        javax.naming.directory.InvalidSearchControlsException e0 = new javax.naming.directory.InvalidSearchControlsException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
