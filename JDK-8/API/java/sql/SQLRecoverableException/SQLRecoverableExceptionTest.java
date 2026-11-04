/*
 * @test
 * @summary java.sql.SQLRecoverableException conformance: message and hierarchy.
 * @run main SQLRecoverableExceptionTest
 */


public class SQLRecoverableExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.sql.SQLRecoverableException e = new java.sql.SQLRecoverableException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.sql.SQLException, "is SQLException");
        java.sql.SQLRecoverableException e0 = new java.sql.SQLRecoverableException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
