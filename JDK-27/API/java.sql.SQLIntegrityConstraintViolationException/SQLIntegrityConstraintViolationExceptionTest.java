/*
 * @test
 * @summary java.sql.SQLIntegrityConstraintViolationException conformance: constructor and SQLState/errorCode behavior
 * @run main SQLIntegrityConstraintViolationExceptionTest
 */

import java.sql.SQLIntegrityConstraintViolationException;

public class SQLIntegrityConstraintViolationExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        SQLIntegrityConstraintViolationException e = new SQLIntegrityConstraintViolationException();
        check(e.getMessage() == null, "no-arg message null");
        check(e.getSQLState() == null, "no-arg SQLState null");
        check(e.getErrorCode() == 0, "no-arg errorCode 0");
        SQLIntegrityConstraintViolationException e2 = new SQLIntegrityConstraintViolationException("msg");
        check("msg".equals(e2.getMessage()), "String ctor message");
        SQLIntegrityConstraintViolationException e3 = new SQLIntegrityConstraintViolationException("msg", "42S01", 1234);
        check("42S01".equals(e3.getSQLState()), "SQLState");
        check(e3.getErrorCode() == 1234, "errorCode");
        SQLIntegrityConstraintViolationException e4 = new SQLIntegrityConstraintViolationException("m", "st");
        check("st".equals(e4.getSQLState()) && "m".equals(e4.getMessage()), "String+SQLState ctor");
        RuntimeException cause = new RuntimeException("cause");
        SQLIntegrityConstraintViolationException e5 = new SQLIntegrityConstraintViolationException("msg", cause);
        check(cause == e5.getCause(), "String+cause");
        SQLIntegrityConstraintViolationException e6 = new SQLIntegrityConstraintViolationException(cause);
        check(cause == e6.getCause(), "Throwable-only cause");
        SQLIntegrityConstraintViolationException e7 = new SQLIntegrityConstraintViolationException("msg", "42S01", cause);
        check("42S01".equals(e7.getSQLState()) && cause == e7.getCause(), "String+SQLState+cause");
        SQLIntegrityConstraintViolationException e8 = new SQLIntegrityConstraintViolationException("msg", "42S01", 7, cause);
        check(e8.getErrorCode() == 7 && cause == e8.getCause(), "full ctor");
        check(e instanceof java.sql.SQLNonTransientException, "extends java.sql.SQLNonTransientException");
        check(e instanceof java.sql.SQLException, "is SQLException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
