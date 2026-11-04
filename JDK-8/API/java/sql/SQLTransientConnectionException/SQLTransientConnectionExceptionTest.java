/*
 * @test
 * @summary java.sql.SQLTransientConnectionException conformance: message and hierarchy.
 * @run main SQLTransientConnectionExceptionTest
 */


public class SQLTransientConnectionExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.sql.SQLTransientConnectionException e = new java.sql.SQLTransientConnectionException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.sql.SQLTransientException, "is SQLTransientException");
        java.sql.SQLTransientConnectionException e0 = new java.sql.SQLTransientConnectionException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
