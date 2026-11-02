/*
 * @test
 * @summary java.sql.SQLWarning conformance: constructors and warning chaining
 * @run main SQLWarningTest
 */

public class SQLWarningTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        java.sql.SQLWarning w = new java.sql.SQLWarning();
        check(w.getMessage() == null && w.getSQLState() == null && w.getErrorCode() == 0, "no-arg");
        java.sql.SQLWarning w2 = new java.sql.SQLWarning("w", "st", 7);
        check("w".equals(w2.getMessage()) && "st".equals(w2.getSQLState()) && w2.getErrorCode() == 7, "3-arg");
        java.sql.SQLWarning next = new java.sql.SQLWarning("n");
        w2.setNextWarning(next);
        check(w2.getNextWarning() == next, "setNextWarning/getNextWarning");
        check(next.getNextWarning() == null, "next has no next");
        RuntimeException cause = new RuntimeException("c");
        check(new java.sql.SQLWarning("m", "s", 1, cause).getCause() == cause, "full ctor cause");
        check(w2 instanceof java.sql.SQLException, "extends SQLException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
