/*
 * @test
 * @summary java.sql.SQLTransactionRollbackException conformance: constructor and SQLState/errorCode behavior
 * @run main SQLTransactionRollbackExceptionTest
 */

import java.sql.SQLTransactionRollbackException;

public class SQLTransactionRollbackExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        SQLTransactionRollbackException e = new SQLTransactionRollbackException();
        check(e.getMessage() == null, "no-arg message null");
        check(e.getSQLState() == null, "no-arg SQLState null");
        check(e.getErrorCode() == 0, "no-arg errorCode 0");
        SQLTransactionRollbackException e2 = new SQLTransactionRollbackException("msg");
        check("msg".equals(e2.getMessage()), "String ctor message");
        SQLTransactionRollbackException e3 = new SQLTransactionRollbackException("msg", "42S01", 1234);
        check("42S01".equals(e3.getSQLState()), "SQLState");
        check(e3.getErrorCode() == 1234, "errorCode");
        SQLTransactionRollbackException e4 = new SQLTransactionRollbackException("m", "st");
        check("st".equals(e4.getSQLState()) && "m".equals(e4.getMessage()), "String+SQLState ctor");
        RuntimeException cause = new RuntimeException("cause");
        SQLTransactionRollbackException e5 = new SQLTransactionRollbackException("msg", cause);
        check(cause == e5.getCause(), "String+cause");
        SQLTransactionRollbackException e6 = new SQLTransactionRollbackException(cause);
        check(cause == e6.getCause(), "Throwable-only cause");
        SQLTransactionRollbackException e7 = new SQLTransactionRollbackException("msg", "42S01", cause);
        check("42S01".equals(e7.getSQLState()) && cause == e7.getCause(), "String+SQLState+cause");
        SQLTransactionRollbackException e8 = new SQLTransactionRollbackException("msg", "42S01", 7, cause);
        check(e8.getErrorCode() == 7 && cause == e8.getCause(), "full ctor");
        check(e instanceof java.sql.SQLTransientException, "extends java.sql.SQLTransientException");
        check(e instanceof java.sql.SQLException, "is SQLException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
