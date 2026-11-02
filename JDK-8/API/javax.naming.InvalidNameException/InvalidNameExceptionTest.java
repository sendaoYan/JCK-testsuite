/*
 * @test
 * @summary javax.naming.InvalidNameException conformance: message and hierarchy.
 * @run main InvalidNameExceptionTest
 */


public class InvalidNameExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.InvalidNameException e = new javax.naming.InvalidNameException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        javax.naming.InvalidNameException e0 = new javax.naming.InvalidNameException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
