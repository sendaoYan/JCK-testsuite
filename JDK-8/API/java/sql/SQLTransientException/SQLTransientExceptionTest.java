/*
 * @test
 * @summary java.sql.SQLTransientException conformance: message and hierarchy.
 * @run main SQLTransientExceptionTest
 */


public class SQLTransientExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.sql.SQLTransientException e = new java.sql.SQLTransientException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.sql.SQLException, "is SQLException");
        java.sql.SQLTransientException e0 = new java.sql.SQLTransientException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
