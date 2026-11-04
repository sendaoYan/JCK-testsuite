/*
 * @test
 * @summary javax.naming.directory.NoSuchAttributeException conformance: message and hierarchy.
 * @run main NoSuchAttributeExceptionTest
 */


public class NoSuchAttributeExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.directory.NoSuchAttributeException e = new javax.naming.directory.NoSuchAttributeException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        javax.naming.directory.NoSuchAttributeException e0 = new javax.naming.directory.NoSuchAttributeException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
