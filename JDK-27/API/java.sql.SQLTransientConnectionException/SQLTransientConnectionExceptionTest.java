/*
 * @test
 * @summary java.sql.SQLTransientConnectionException conformance: constructor and SQLState/errorCode behavior
 * @run main SQLTransientConnectionExceptionTest
 */

import java.sql.SQLTransientConnectionException;

public class SQLTransientConnectionExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        SQLTransientConnectionException e = new SQLTransientConnectionException();
        check(e.getMessage() == null, "no-arg message null");
        check(e.getSQLState() == null, "no-arg SQLState null");
        check(e.getErrorCode() == 0, "no-arg errorCode 0");
        SQLTransientConnectionException e2 = new SQLTransientConnectionException("msg");
        check("msg".equals(e2.getMessage()), "String ctor message");
        SQLTransientConnectionException e3 = new SQLTransientConnectionException("msg", "42S01", 1234);
        check("42S01".equals(e3.getSQLState()), "SQLState");
        check(e3.getErrorCode() == 1234, "errorCode");
        SQLTransientConnectionException e4 = new SQLTransientConnectionException("m", "st");
        check("st".equals(e4.getSQLState()) && "m".equals(e4.getMessage()), "String+SQLState ctor");
        RuntimeException cause = new RuntimeException("cause");
        SQLTransientConnectionException e5 = new SQLTransientConnectionException("msg", cause);
        check(cause == e5.getCause(), "String+cause");
        SQLTransientConnectionException e6 = new SQLTransientConnectionException(cause);
        check(cause == e6.getCause(), "Throwable-only cause");
        SQLTransientConnectionException e7 = new SQLTransientConnectionException("msg", "42S01", cause);
        check("42S01".equals(e7.getSQLState()) && cause == e7.getCause(), "String+SQLState+cause");
        SQLTransientConnectionException e8 = new SQLTransientConnectionException("msg", "42S01", 7, cause);
        check(e8.getErrorCode() == 7 && cause == e8.getCause(), "full ctor");
        check(e instanceof java.sql.SQLTransientException, "extends java.sql.SQLTransientException");
        check(e instanceof java.sql.SQLException, "is SQLException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
