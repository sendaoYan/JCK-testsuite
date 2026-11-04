/*
 * @test
 * @summary java.sql.SQLTimeoutException API conformance: hierarchy
 *          (SQLTransientException -> SQLException) and all 8 constructors with
 *          message/SQLState/vendorCode/cause defaults.
 * @run main SQLTimeoutExceptionTest
 */

public class SQLTimeoutExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testHierarchy();
        testConstructors();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testHierarchy() {
        check(java.sql.SQLTransientException.class.isAssignableFrom(
                java.sql.SQLTimeoutException.class), "extends SQLTransientException");
        check(java.sql.SQLException.class.isAssignableFrom(
                java.sql.SQLTimeoutException.class), "extends SQLException");
    }

    static void testConstructors() {
        java.sql.SQLTimeoutException e0 = new java.sql.SQLTimeoutException();
        check(e0.getMessage() == null && e0.getSQLState() == null && e0.getErrorCode() == 0,
                "no-arg defaults");

        java.sql.SQLTimeoutException e1 = new java.sql.SQLTimeoutException("m");
        check("m".equals(e1.getMessage()) && e1.getSQLState() == null && e1.getErrorCode() == 0,
                "(String)");

        java.sql.SQLTimeoutException e2 = new java.sql.SQLTimeoutException("m", "S");
        check("S".equals(e2.getSQLState()) && e2.getErrorCode() == 0, "(String,String)");

        java.sql.SQLTimeoutException e3 = new java.sql.SQLTimeoutException("m", "S", 5);
        check("S".equals(e3.getSQLState()) && e3.getErrorCode() == 5, "(String,String,int)");

        RuntimeException cause = new RuntimeException("c");
        java.sql.SQLTimeoutException e4 = new java.sql.SQLTimeoutException(cause);
        check(e4.getCause() == cause && cause.toString().equals(e4.getMessage()), "(Throwable)");

        java.sql.SQLTimeoutException e5 = new java.sql.SQLTimeoutException("m", cause);
        check("m".equals(e5.getMessage()) && e5.getCause() == cause, "(String,Throwable)");

        java.sql.SQLTimeoutException e6 = new java.sql.SQLTimeoutException("m", "S", cause);
        check("S".equals(e6.getSQLState()) && e6.getCause() == cause && e6.getErrorCode() == 0,
                "(String,String,Throwable)");

        java.sql.SQLTimeoutException e7 = new java.sql.SQLTimeoutException("m", "S", 5, cause);
        check("S".equals(e7.getSQLState()) && e7.getErrorCode() == 5 && e7.getCause() == cause,
                "(String,String,int,Throwable)");
    }
}
