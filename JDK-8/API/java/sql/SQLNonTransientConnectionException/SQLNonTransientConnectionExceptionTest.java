/*
 * @test
 * @summary java.sql.SQLNonTransientConnectionException conformance: message and hierarchy.
 * @run main SQLNonTransientConnectionExceptionTest
 */


public class SQLNonTransientConnectionExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.sql.SQLNonTransientConnectionException e = new java.sql.SQLNonTransientConnectionException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.sql.SQLNonTransientException, "is SQLNonTransientException");
        java.sql.SQLNonTransientConnectionException e0 = new java.sql.SQLNonTransientConnectionException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
