/*
 * @test
 * @summary java.sql.SQLRecoverableException conformance: constructor and SQLState/errorCode behavior
 * @run main SQLRecoverableExceptionTest
 */

import java.sql.SQLRecoverableException;

public class SQLRecoverableExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        SQLRecoverableException e = new SQLRecoverableException();
        check(e.getMessage() == null, "no-arg message null");
        check(e.getSQLState() == null, "no-arg SQLState null");
        check(e.getErrorCode() == 0, "no-arg errorCode 0");
        SQLRecoverableException e2 = new SQLRecoverableException("msg");
        check("msg".equals(e2.getMessage()), "String ctor message");
        SQLRecoverableException e3 = new SQLRecoverableException("msg", "42S01", 1234);
        check("42S01".equals(e3.getSQLState()), "SQLState");
        check(e3.getErrorCode() == 1234, "errorCode");
        SQLRecoverableException e4 = new SQLRecoverableException("m", "st");
        check("st".equals(e4.getSQLState()) && "m".equals(e4.getMessage()), "String+SQLState ctor");
        RuntimeException cause = new RuntimeException("cause");
        SQLRecoverableException e5 = new SQLRecoverableException("msg", cause);
        check(cause == e5.getCause(), "String+cause");
        SQLRecoverableException e6 = new SQLRecoverableException(cause);
        check(cause == e6.getCause(), "Throwable-only cause");
        SQLRecoverableException e7 = new SQLRecoverableException("msg", "42S01", cause);
        check("42S01".equals(e7.getSQLState()) && cause == e7.getCause(), "String+SQLState+cause");
        SQLRecoverableException e8 = new SQLRecoverableException("msg", "42S01", 7, cause);
        check(e8.getErrorCode() == 7 && cause == e8.getCause(), "full ctor");
        check(e instanceof java.sql.SQLException, "extends java.sql.SQLException");
        check(e instanceof java.sql.SQLException, "is SQLException");        System.out.println("PASS (" + passed + " assertions)");
    }
}
