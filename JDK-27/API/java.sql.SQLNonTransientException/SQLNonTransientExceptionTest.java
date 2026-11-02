/*
 * @test
 * @summary java.sql.SQLNonTransientException conformance: constructor and SQLState/errorCode behavior
 * @run main SQLNonTransientExceptionTest
 */

import java.sql.SQLNonTransientException;

public class SQLNonTransientExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        SQLNonTransientException e = new SQLNonTransientException();
        check(e.getMessage() == null, "no-arg message null");
        check(e.getSQLState() == null, "no-arg SQLState null");
        check(e.getErrorCode() == 0, "no-arg errorCode 0");
        SQLNonTransientException e2 = new SQLNonTransientException("msg");
        check("msg".equals(e2.getMessage()), "String ctor message");
        SQLNonTransientException e3 = new SQLNonTransientException("msg", "42S01", 1234);
        check("42S01".equals(e3.getSQLState()), "SQLState");
        check(e3.getErrorCode() == 1234, "errorCode");
        SQLNonTransientException e4 = new SQLNonTransientException("m", "st");
        check("st".equals(e4.getSQLState()) && "m".equals(e4.getMessage()), "String+SQLState ctor");
        RuntimeException cause = new RuntimeException("cause");
        SQLNonTransientException e5 = new SQLNonTransientException("msg", cause);
        check(cause == e5.getCause(), "String+cause");
        SQLNonTransientException e6 = new SQLNonTransientException(cause);
        check(cause == e6.getCause(), "Throwable-only cause");
        SQLNonTransientException e7 = new SQLNonTransientException("msg", "42S01", cause);
        check("42S01".equals(e7.getSQLState()) && cause == e7.getCause(), "String+SQLState+cause");
        SQLNonTransientException e8 = new SQLNonTransientException("msg", "42S01", 7, cause);
        check(e8.getErrorCode() == 7 && cause == e8.getCause(), "full ctor");
        check(e instanceof java.sql.SQLException, "extends java.sql.SQLException");
        check(e instanceof java.sql.SQLException, "is SQLException");        System.out.println("PASS (" + passed + " assertions)");
    }
}
