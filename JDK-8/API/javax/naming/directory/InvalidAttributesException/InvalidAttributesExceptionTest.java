/*
 * @test
 * @summary javax.naming.directory.InvalidAttributesException conformance: message and hierarchy.
 * @run main InvalidAttributesExceptionTest
 */


public class InvalidAttributesExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.directory.InvalidAttributesException e = new javax.naming.directory.InvalidAttributesException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        javax.naming.directory.InvalidAttributesException e0 = new javax.naming.directory.InvalidAttributesException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
