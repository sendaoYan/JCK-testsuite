/*
 * @test
 * @summary java.sql.SQLTimeoutException conformance: message and hierarchy.
 * @run main SQLTimeoutExceptionTest
 */


public class SQLTimeoutExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.sql.SQLTimeoutException e = new java.sql.SQLTimeoutException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.sql.SQLTransientException, "is SQLTransientException");
        java.sql.SQLTimeoutException e0 = new java.sql.SQLTimeoutException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
