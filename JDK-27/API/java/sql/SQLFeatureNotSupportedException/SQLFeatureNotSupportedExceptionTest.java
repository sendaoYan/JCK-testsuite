/*
 * @test
 * @summary java.sql.SQLFeatureNotSupportedException API conformance: hierarchy
 *          (SQLNonTransientException -> SQLException) and all 8 constructors with
 *          message/SQLState/vendorCode/cause defaults.
 * @run main SQLFeatureNotSupportedExceptionTest
 */

public class SQLFeatureNotSupportedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.sql.SQLFeatureNotSupportedException");
        check(c != null, "loads");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "class kind");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == java.sql.SQLNonTransientException.class, "superclass");
        check(c.getInterfaces().length == 0, "no direct interface");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testHierarchy();
        testConstructors();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testHierarchy() {
        check(java.sql.SQLNonTransientException.class.isAssignableFrom(
                java.sql.SQLFeatureNotSupportedException.class), "extends SQLNonTransientException");
        check(java.sql.SQLException.class.isAssignableFrom(
                java.sql.SQLFeatureNotSupportedException.class), "extends SQLException");
    }

    static void testConstructors() {
        java.sql.SQLFeatureNotSupportedException e0 = new java.sql.SQLFeatureNotSupportedException();
        check(e0.getMessage() == null && e0.getSQLState() == null && e0.getErrorCode() == 0,
                "no-arg defaults");

        java.sql.SQLFeatureNotSupportedException e1 = new java.sql.SQLFeatureNotSupportedException("m");
        check("m".equals(e1.getMessage()) && e1.getSQLState() == null && e1.getErrorCode() == 0,
                "(String)");

        java.sql.SQLFeatureNotSupportedException e2 = new java.sql.SQLFeatureNotSupportedException("m", "S");
        check("S".equals(e2.getSQLState()) && e2.getErrorCode() == 0, "(String,String)");

        java.sql.SQLFeatureNotSupportedException e3 = new java.sql.SQLFeatureNotSupportedException("m", "S", 5);
        check("S".equals(e3.getSQLState()) && e3.getErrorCode() == 5, "(String,String,int)");

        RuntimeException cause = new RuntimeException("c");
        java.sql.SQLFeatureNotSupportedException e4 = new java.sql.SQLFeatureNotSupportedException(cause);
        check(e4.getCause() == cause && cause.toString().equals(e4.getMessage()), "(Throwable)");

        java.sql.SQLFeatureNotSupportedException e5 = new java.sql.SQLFeatureNotSupportedException("m", cause);
        check("m".equals(e5.getMessage()) && e5.getCause() == cause, "(String,Throwable)");

        java.sql.SQLFeatureNotSupportedException e6 = new java.sql.SQLFeatureNotSupportedException("m", "S", cause);
        check("S".equals(e6.getSQLState()) && e6.getCause() == cause && e6.getErrorCode() == 0,
                "(String,String,Throwable)");

        java.sql.SQLFeatureNotSupportedException e7 = new java.sql.SQLFeatureNotSupportedException("m", "S", 5, cause);
        check("S".equals(e7.getSQLState()) && e7.getErrorCode() == 5 && e7.getCause() == cause,
                "(String,String,int,Throwable)");
    }
}
