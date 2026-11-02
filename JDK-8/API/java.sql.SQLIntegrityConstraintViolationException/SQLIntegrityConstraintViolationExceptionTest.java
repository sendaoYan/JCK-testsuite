/*
 * @test
 * @summary java.sql.SQLIntegrityConstraintViolationException conformance: message and hierarchy.
 * @run main SQLIntegrityConstraintViolationExceptionTest
 */


public class SQLIntegrityConstraintViolationExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.sql.SQLIntegrityConstraintViolationException e = new java.sql.SQLIntegrityConstraintViolationException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.sql.SQLNonTransientException, "is SQLNonTransientException");
        java.sql.SQLIntegrityConstraintViolationException e0 = new java.sql.SQLIntegrityConstraintViolationException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
