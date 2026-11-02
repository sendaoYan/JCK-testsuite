/*
 * @test
 * @summary java.sql.SQLInvalidAuthorizationSpecException conformance: message and hierarchy.
 * @run main SQLInvalidAuthorizationSpecExceptionTest
 */


public class SQLInvalidAuthorizationSpecExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.sql.SQLInvalidAuthorizationSpecException e = new java.sql.SQLInvalidAuthorizationSpecException("boom");
        check("boom".equals(e.getMessage()), "getMessage(String)");
        check(e instanceof java.sql.SQLNonTransientException, "is SQLNonTransientException");
        java.sql.SQLInvalidAuthorizationSpecException e0 = new java.sql.SQLInvalidAuthorizationSpecException();
        check(e0.getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
