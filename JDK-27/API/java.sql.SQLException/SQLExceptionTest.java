/*
 * @test
 * @summary java.sql.SQLException conformance: constructors, chaining and iterator
 * @run main SQLExceptionTest
 */

public class SQLExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        java.sql.SQLException e = new java.sql.SQLException();
        check(e.getMessage() == null && e.getSQLState() == null && e.getErrorCode() == 0, "no-arg");
        java.sql.SQLException e1 = new java.sql.SQLException("m", "42S01", 99);
        check("m".equals(e1.getMessage()) && "42S01".equals(e1.getSQLState()) && e1.getErrorCode() == 99, "3-arg");
        java.sql.SQLException next = new java.sql.SQLException("next", "S", 1);
        e1.setNextException(next);
        check(e1.getNextException() == next, "setNextException/getNextException");
        check(next.getNextException() == null, "next has no next");
        java.util.Iterator<java.lang.Throwable> it = e1.iterator();
        check(it.hasNext() && it.next() == e1, "iterator first is self");
        check(it.hasNext() && it.next() == next, "iterator second is next");
        check(!it.hasNext(), "iterator exhausted");
        RuntimeException cause = new RuntimeException("c");
        java.sql.SQLException e2 = new java.sql.SQLException("m", "s", 1, cause);
        check(e2.getCause() == cause, "cause");
        check(e2 instanceof java.lang.Iterable, "implements Iterable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
