/*
 * @test
 * @summary java.sql.SQLDataException conformance: message and hierarchy.
 * @run main SQLDataExceptionTest
 */


public class SQLDataExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.sql.SQLDataException e = new java.sql.SQLDataException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.sql.SQLNonTransientException, "is SQLNonTransientException");
        java.sql.SQLDataException e0 = new java.sql.SQLDataException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
