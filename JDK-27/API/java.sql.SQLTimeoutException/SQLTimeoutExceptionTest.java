/*
 * @test
 * @summary java.sql.SQLTimeoutException conformance: constructor and SQLState/errorCode behavior
 * @run main SQLTimeoutExceptionTest
 */

import java.sql.SQLTimeoutException;

public class SQLTimeoutExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        SQLTimeoutException e = new SQLTimeoutException();
        check(e.getMessage() == null, "no-arg message null");
        check(e.getSQLState() == null, "no-arg SQLState null");
        check(e.getErrorCode() == 0, "no-arg errorCode 0");
        SQLTimeoutException e2 = new SQLTimeoutException("msg");
        check("msg".equals(e2.getMessage()), "String ctor message");
        SQLTimeoutException e3 = new SQLTimeoutException("msg", "42S01", 1234);
        check("42S01".equals(e3.getSQLState()), "SQLState");
        check(e3.getErrorCode() == 1234, "errorCode");
        SQLTimeoutException e4 = new SQLTimeoutException("m", "st");
        check("st".equals(e4.getSQLState()) && "m".equals(e4.getMessage()), "String+SQLState ctor");
        RuntimeException cause = new RuntimeException("cause");
        SQLTimeoutException e5 = new SQLTimeoutException("msg", cause);
        check(cause == e5.getCause(), "String+cause");
        SQLTimeoutException e6 = new SQLTimeoutException(cause);
        check(cause == e6.getCause(), "Throwable-only cause");
        SQLTimeoutException e7 = new SQLTimeoutException("msg", "42S01", cause);
        check("42S01".equals(e7.getSQLState()) && cause == e7.getCause(), "String+SQLState+cause");
        SQLTimeoutException e8 = new SQLTimeoutException("msg", "42S01", 7, cause);
        check(e8.getErrorCode() == 7 && cause == e8.getCause(), "full ctor");
        check(e instanceof java.sql.SQLTransientException, "extends java.sql.SQLTransientException");
        check(e instanceof java.sql.SQLException, "is SQLException");        System.out.println("PASS (" + passed + " assertions)");
    }
}
