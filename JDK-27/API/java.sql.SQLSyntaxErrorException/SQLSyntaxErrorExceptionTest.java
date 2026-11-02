/*
 * @test
 * @summary java.sql.SQLSyntaxErrorException conformance: constructor and SQLState/errorCode behavior
 * @run main SQLSyntaxErrorExceptionTest
 */

import java.sql.SQLSyntaxErrorException;

public class SQLSyntaxErrorExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        SQLSyntaxErrorException e = new SQLSyntaxErrorException();
        check(e.getMessage() == null, "no-arg message null");
        check(e.getSQLState() == null, "no-arg SQLState null");
        check(e.getErrorCode() == 0, "no-arg errorCode 0");
        SQLSyntaxErrorException e2 = new SQLSyntaxErrorException("msg");
        check("msg".equals(e2.getMessage()), "String ctor message");
        SQLSyntaxErrorException e3 = new SQLSyntaxErrorException("msg", "42S01", 1234);
        check("42S01".equals(e3.getSQLState()), "SQLState");
        check(e3.getErrorCode() == 1234, "errorCode");
        SQLSyntaxErrorException e4 = new SQLSyntaxErrorException("m", "st");
        check("st".equals(e4.getSQLState()) && "m".equals(e4.getMessage()), "String+SQLState ctor");
        RuntimeException cause = new RuntimeException("cause");
        SQLSyntaxErrorException e5 = new SQLSyntaxErrorException("msg", cause);
        check(cause == e5.getCause(), "String+cause");
        SQLSyntaxErrorException e6 = new SQLSyntaxErrorException(cause);
        check(cause == e6.getCause(), "Throwable-only cause");
        SQLSyntaxErrorException e7 = new SQLSyntaxErrorException("msg", "42S01", cause);
        check("42S01".equals(e7.getSQLState()) && cause == e7.getCause(), "String+SQLState+cause");
        SQLSyntaxErrorException e8 = new SQLSyntaxErrorException("msg", "42S01", 7, cause);
        check(e8.getErrorCode() == 7 && cause == e8.getCause(), "full ctor");
        check(e instanceof java.sql.SQLNonTransientException, "extends java.sql.SQLNonTransientException");
        check(e instanceof java.sql.SQLException, "is SQLException");        System.out.println("PASS (" + passed + " assertions)");
    }
}
