/*
 * @test
 * @summary java.sql.SQLTransactionRollbackException conformance: message and hierarchy.
 * @run main SQLTransactionRollbackExceptionTest
 */


public class SQLTransactionRollbackExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.sql.SQLTransactionRollbackException e = new java.sql.SQLTransactionRollbackException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.sql.SQLTransientException, "is SQLTransientException");
        java.sql.SQLTransactionRollbackException e0 = new java.sql.SQLTransactionRollbackException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
