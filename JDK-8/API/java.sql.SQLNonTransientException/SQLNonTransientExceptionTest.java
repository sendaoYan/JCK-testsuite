/*
 * @test
 * @summary java.sql.SQLNonTransientException conformance: message and hierarchy.
 * @run main SQLNonTransientExceptionTest
 */


public class SQLNonTransientExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.sql.SQLNonTransientException e = new java.sql.SQLNonTransientException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.sql.SQLException, "is SQLException");
        java.sql.SQLNonTransientException e0 = new java.sql.SQLNonTransientException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
