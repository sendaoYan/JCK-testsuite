/*
 * @test
 * @summary java.sql.SQLNonTransientConnectionException conformance: constructor and SQLState/errorCode behavior
 * @run main SQLNonTransientConnectionExceptionTest
 */

import java.sql.SQLNonTransientConnectionException;

public class SQLNonTransientConnectionExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        SQLNonTransientConnectionException e = new SQLNonTransientConnectionException();
        check(e.getMessage() == null, "no-arg message null");
        check(e.getSQLState() == null, "no-arg SQLState null");
        check(e.getErrorCode() == 0, "no-arg errorCode 0");
        SQLNonTransientConnectionException e2 = new SQLNonTransientConnectionException("msg");
        check("msg".equals(e2.getMessage()), "String ctor message");
        SQLNonTransientConnectionException e3 = new SQLNonTransientConnectionException("msg", "42S01", 1234);
        check("42S01".equals(e3.getSQLState()), "SQLState");
        check(e3.getErrorCode() == 1234, "errorCode");
        SQLNonTransientConnectionException e4 = new SQLNonTransientConnectionException("m", "st");
        check("st".equals(e4.getSQLState()) && "m".equals(e4.getMessage()), "String+SQLState ctor");
        RuntimeException cause = new RuntimeException("cause");
        SQLNonTransientConnectionException e5 = new SQLNonTransientConnectionException("msg", cause);
        check(cause == e5.getCause(), "String+cause");
        SQLNonTransientConnectionException e6 = new SQLNonTransientConnectionException(cause);
        check(cause == e6.getCause(), "Throwable-only cause");
        SQLNonTransientConnectionException e7 = new SQLNonTransientConnectionException("msg", "42S01", cause);
        check("42S01".equals(e7.getSQLState()) && cause == e7.getCause(), "String+SQLState+cause");
        SQLNonTransientConnectionException e8 = new SQLNonTransientConnectionException("msg", "42S01", 7, cause);
        check(e8.getErrorCode() == 7 && cause == e8.getCause(), "full ctor");
        check(e instanceof java.sql.SQLNonTransientException, "extends java.sql.SQLNonTransientException");
        check(e instanceof java.sql.SQLException, "is SQLException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
