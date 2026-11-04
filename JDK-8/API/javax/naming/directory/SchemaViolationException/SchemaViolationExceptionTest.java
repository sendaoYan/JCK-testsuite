/*
 * @test
 * @summary javax.naming.directory.SchemaViolationException conformance: message and hierarchy.
 * @run main SchemaViolationExceptionTest
 */


public class SchemaViolationExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.directory.SchemaViolationException e = new javax.naming.directory.SchemaViolationException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof javax.naming.NamingException, "is NamingException");
        javax.naming.directory.SchemaViolationException e0 = new javax.naming.directory.SchemaViolationException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
