/*
 * @test
 * @summary java.sql.SQLDataException conformance: constructor and SQLState/errorCode behavior
 * @run main SQLDataExceptionTest
 */

import java.sql.SQLDataException;

public class SQLDataExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        SQLDataException e = new SQLDataException();
        check(e.getMessage() == null, "no-arg message null");
        check(e.getSQLState() == null, "no-arg SQLState null");
        check(e.getErrorCode() == 0, "no-arg errorCode 0");
        SQLDataException e2 = new SQLDataException("msg");
        check("msg".equals(e2.getMessage()), "String ctor message");
        SQLDataException e3 = new SQLDataException("msg", "42S01", 1234);
        check("42S01".equals(e3.getSQLState()), "SQLState");
        check(e3.getErrorCode() == 1234, "errorCode");
        SQLDataException e4 = new SQLDataException("m", "st");
        check("st".equals(e4.getSQLState()) && "m".equals(e4.getMessage()), "String+SQLState ctor");
        RuntimeException cause = new RuntimeException("cause");
        SQLDataException e5 = new SQLDataException("msg", cause);
        check(cause == e5.getCause(), "String+cause");
        SQLDataException e6 = new SQLDataException(cause);
        check(cause == e6.getCause(), "Throwable-only cause");
        SQLDataException e7 = new SQLDataException("msg", "42S01", cause);
        check("42S01".equals(e7.getSQLState()) && cause == e7.getCause(), "String+SQLState+cause");
        SQLDataException e8 = new SQLDataException("msg", "42S01", 7, cause);
        check(e8.getErrorCode() == 7 && cause == e8.getCause(), "full ctor");
        check(e instanceof java.sql.SQLNonTransientException, "extends java.sql.SQLNonTransientException");
        check(e instanceof java.sql.SQLException, "is SQLException");        System.out.println("PASS (" + passed + " assertions)");
    }
}
