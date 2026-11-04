/*
 * @test
 * @summary java.sql.SQLException conformance: message and hierarchy.
 * @run main SQLExceptionTest
 */


public class SQLExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.sql.SQLException e = new java.sql.SQLException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.lang.Exception, "is Exception");
        check(e.getSQLState() == null, "SQLState null");
        check(e.getErrorCode() == 0, "errorCode 0");
        check(e.getNextException() == null, "no next exception");
        java.sql.SQLException e0 = new java.sql.SQLException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
