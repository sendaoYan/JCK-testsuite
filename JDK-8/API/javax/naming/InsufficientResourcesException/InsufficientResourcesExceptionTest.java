/*
 * @test
 * @summary javax.naming.InsufficientResourcesException conformance: message and hierarchy.
 * @run main InsufficientResourcesExceptionTest
 */


public class InsufficientResourcesExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.InsufficientResourcesException e = new javax.naming.InsufficientResourcesException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        javax.naming.InsufficientResourcesException e0 = new javax.naming.InsufficientResourcesException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
