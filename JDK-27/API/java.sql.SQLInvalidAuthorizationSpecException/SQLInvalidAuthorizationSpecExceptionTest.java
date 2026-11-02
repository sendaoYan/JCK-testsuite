/*
 * @test
 * @summary java.sql.SQLInvalidAuthorizationSpecException conformance: constructor and SQLState/errorCode behavior
 * @run main SQLInvalidAuthorizationSpecExceptionTest
 */

import java.sql.SQLInvalidAuthorizationSpecException;

public class SQLInvalidAuthorizationSpecExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        SQLInvalidAuthorizationSpecException e = new SQLInvalidAuthorizationSpecException();
        check(e.getMessage() == null, "no-arg message null");
        check(e.getSQLState() == null, "no-arg SQLState null");
        check(e.getErrorCode() == 0, "no-arg errorCode 0");
        SQLInvalidAuthorizationSpecException e2 = new SQLInvalidAuthorizationSpecException("msg");
        check("msg".equals(e2.getMessage()), "String ctor message");
        SQLInvalidAuthorizationSpecException e3 = new SQLInvalidAuthorizationSpecException("msg", "42S01", 1234);
        check("42S01".equals(e3.getSQLState()), "SQLState");
        check(e3.getErrorCode() == 1234, "errorCode");
        SQLInvalidAuthorizationSpecException e4 = new SQLInvalidAuthorizationSpecException("m", "st");
        check("st".equals(e4.getSQLState()) && "m".equals(e4.getMessage()), "String+SQLState ctor");
        RuntimeException cause = new RuntimeException("cause");
        SQLInvalidAuthorizationSpecException e5 = new SQLInvalidAuthorizationSpecException("msg", cause);
        check(cause == e5.getCause(), "String+cause");
        SQLInvalidAuthorizationSpecException e6 = new SQLInvalidAuthorizationSpecException(cause);
        check(cause == e6.getCause(), "Throwable-only cause");
        SQLInvalidAuthorizationSpecException e7 = new SQLInvalidAuthorizationSpecException("msg", "42S01", cause);
        check("42S01".equals(e7.getSQLState()) && cause == e7.getCause(), "String+SQLState+cause");
        SQLInvalidAuthorizationSpecException e8 = new SQLInvalidAuthorizationSpecException("msg", "42S01", 7, cause);
        check(e8.getErrorCode() == 7 && cause == e8.getCause(), "full ctor");
        check(e instanceof java.sql.SQLNonTransientException, "extends java.sql.SQLNonTransientException");
        check(e instanceof java.sql.SQLException, "is SQLException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
