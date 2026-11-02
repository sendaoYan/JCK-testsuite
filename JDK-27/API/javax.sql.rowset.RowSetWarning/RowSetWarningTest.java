/*
 * @test
 * @summary javax.sql.rowset.RowSetWarning conformance: constructors and warning chaining
 * @run main RowSetWarningTest
 */

public class RowSetWarningTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        javax.sql.rowset.RowSetWarning w = new javax.sql.rowset.RowSetWarning();
        check(w.getMessage() == null, "no-arg");
        javax.sql.rowset.RowSetWarning w2 = new javax.sql.rowset.RowSetWarning("m", "st", 7);
        check("m".equals(w2.getMessage()) && "st".equals(w2.getSQLState()) && w2.getErrorCode() == 7, "3-arg");
        javax.sql.rowset.RowSetWarning next = new javax.sql.rowset.RowSetWarning("n");
        w2.setNextWarning(next);
        check(w2.getNextWarning() == next, "setNextWarning/getNextWarning");
        check(next.getNextWarning() == null, "next has no next");
        check(w2 instanceof java.sql.SQLException, "extends SQLException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
