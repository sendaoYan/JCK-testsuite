/*
 * @test
 * @summary java.sql.SQLSyntaxErrorException conformance: message and hierarchy.
 * @run main SQLSyntaxErrorExceptionTest
 */


public class SQLSyntaxErrorExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.sql.SQLSyntaxErrorException e = new java.sql.SQLSyntaxErrorException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.sql.SQLNonTransientException, "is SQLNonTransientException");
        java.sql.SQLSyntaxErrorException e0 = new java.sql.SQLSyntaxErrorException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
