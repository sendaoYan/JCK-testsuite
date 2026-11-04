/*
 * @test
 * @summary java.sql.SQLFeatureNotSupportedException conformance: message and hierarchy.
 * @run main SQLFeatureNotSupportedExceptionTest
 */


public class SQLFeatureNotSupportedExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.sql.SQLFeatureNotSupportedException e = new java.sql.SQLFeatureNotSupportedException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.sql.SQLNonTransientException, "is SQLNonTransientException");
        java.sql.SQLFeatureNotSupportedException e0 = new java.sql.SQLFeatureNotSupportedException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
