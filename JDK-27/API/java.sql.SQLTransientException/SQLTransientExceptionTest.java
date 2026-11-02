/*
 * @test
 * @summary java.sql.SQLTransientException conformance: constructor and SQLState/errorCode behavior
 * @run main SQLTransientExceptionTest
 */

import java.sql.SQLTransientException;

public class SQLTransientExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        SQLTransientException e = new SQLTransientException();
        check(e.getMessage() == null, "no-arg message null");
        check(e.getSQLState() == null, "no-arg SQLState null");
        check(e.getErrorCode() == 0, "no-arg errorCode 0");
        SQLTransientException e2 = new SQLTransientException("msg");
        check("msg".equals(e2.getMessage()), "String ctor message");
        SQLTransientException e3 = new SQLTransientException("msg", "42S01", 1234);
        check("42S01".equals(e3.getSQLState()), "SQLState");
        check(e3.getErrorCode() == 1234, "errorCode");
        SQLTransientException e4 = new SQLTransientException("m", "st");
        check("st".equals(e4.getSQLState()) && "m".equals(e4.getMessage()), "String+SQLState ctor");
        RuntimeException cause = new RuntimeException("cause");
        SQLTransientException e5 = new SQLTransientException("msg", cause);
        check(cause == e5.getCause(), "String+cause");
        SQLTransientException e6 = new SQLTransientException(cause);
        check(cause == e6.getCause(), "Throwable-only cause");
        SQLTransientException e7 = new SQLTransientException("msg", "42S01", cause);
        check("42S01".equals(e7.getSQLState()) && cause == e7.getCause(), "String+SQLState+cause");
        SQLTransientException e8 = new SQLTransientException("msg", "42S01", 7, cause);
        check(e8.getErrorCode() == 7 && cause == e8.getCause(), "full ctor");
        check(e instanceof java.sql.SQLException, "extends java.sql.SQLException");
        check(e instanceof java.sql.SQLException, "is SQLException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
