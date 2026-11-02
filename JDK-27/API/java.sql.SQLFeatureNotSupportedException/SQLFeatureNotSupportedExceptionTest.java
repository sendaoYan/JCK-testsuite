/*
 * @test
 * @summary java.sql.SQLFeatureNotSupportedException conformance: constructor and SQLState/errorCode behavior
 * @run main SQLFeatureNotSupportedExceptionTest
 */

import java.sql.SQLFeatureNotSupportedException;

public class SQLFeatureNotSupportedExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        SQLFeatureNotSupportedException e = new SQLFeatureNotSupportedException();
        check(e.getMessage() == null, "no-arg message null");
        check(e.getSQLState() == null, "no-arg SQLState null");
        check(e.getErrorCode() == 0, "no-arg errorCode 0");
        SQLFeatureNotSupportedException e2 = new SQLFeatureNotSupportedException("msg");
        check("msg".equals(e2.getMessage()), "String ctor message");
        SQLFeatureNotSupportedException e3 = new SQLFeatureNotSupportedException("msg", "42S01", 1234);
        check("42S01".equals(e3.getSQLState()), "SQLState");
        check(e3.getErrorCode() == 1234, "errorCode");
        SQLFeatureNotSupportedException e4 = new SQLFeatureNotSupportedException("m", "st");
        check("st".equals(e4.getSQLState()) && "m".equals(e4.getMessage()), "String+SQLState ctor");
        RuntimeException cause = new RuntimeException("cause");
        SQLFeatureNotSupportedException e5 = new SQLFeatureNotSupportedException("msg", cause);
        check(cause == e5.getCause(), "String+cause");
        SQLFeatureNotSupportedException e6 = new SQLFeatureNotSupportedException(cause);
        check(cause == e6.getCause(), "Throwable-only cause");
        SQLFeatureNotSupportedException e7 = new SQLFeatureNotSupportedException("msg", "42S01", cause);
        check("42S01".equals(e7.getSQLState()) && cause == e7.getCause(), "String+SQLState+cause");
        SQLFeatureNotSupportedException e8 = new SQLFeatureNotSupportedException("msg", "42S01", 7, cause);
        check(e8.getErrorCode() == 7 && cause == e8.getCause(), "full ctor");
        check(e instanceof java.sql.SQLNonTransientException, "extends java.sql.SQLNonTransientException");
        check(e instanceof java.sql.SQLException, "is SQLException");        System.out.println("PASS (" + passed + " assertions)");
    }
}
