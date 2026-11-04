/*
 * @test
 * @summary javax.naming.NoInitialContextException conformance: message and hierarchy.
 * @run main NoInitialContextExceptionTest
 */


public class NoInitialContextExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.NoInitialContextException e = new javax.naming.NoInitialContextException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        javax.naming.NoInitialContextException e0 = new javax.naming.NoInitialContextException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
