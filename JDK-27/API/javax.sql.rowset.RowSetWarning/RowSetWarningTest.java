/*
 * @test
 * @summary javax.sql.rowset.RowSetWarning conformance: white-box structure + black-box behavior.
 * @run main RowSetWarningTest
 */

import javax.sql.rowset.RowSetWarning;

public class RowSetWarningTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.RowSetWarning");
        check(c != null, "loads");
        check(!c.isInterface(), "is concrete class");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == java.sql.SQLException.class, "extends SQLException");
        check(c.getDeclaredConstructors().length == 4, "4 declared constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RowSetWarning w = new RowSetWarning();
        check(w.getMessage() == null, "no-arg message null");
        check(w.getSQLState() == null, "no-arg SQLState null");
        check(w.getErrorCode() == 0, "no-arg errorCode 0");
        check(w.getNextWarning() == null, "no-arg next warning null");
        check(w instanceof java.sql.SQLException, "instanceof SQLException");

        RowSetWarning w1 = new RowSetWarning("m");
        check("m".equals(w1.getMessage()), "1-arg message");
        check(w1.getSQLState() == null, "1-arg SQLState null");
        check(w1.getErrorCode() == 0, "1-arg errorCode 0");

        RowSetWarning w2 = new RowSetWarning("m", "st");
        check("m".equals(w2.getMessage()) && "st".equals(w2.getSQLState()) && w2.getErrorCode() == 0,
                "2-arg message/SQLState/errorCode");

        RowSetWarning w3 = new RowSetWarning("m", "st", 7);
        check("m".equals(w3.getMessage()) && "st".equals(w3.getSQLState()) && w3.getErrorCode() == 7,
                "3-arg message/SQLState/errorCode");

        // chaining
        RowSetWarning next = new RowSetWarning("n");
        w3.setNextWarning(next);
        check(w3.getNextWarning() == next, "setNextWarning/getNextWarning");
        check(next.getNextWarning() == null, "next has no next");
        RowSetWarning third = new RowSetWarning("t");
        next.setNextWarning(third);
        check(w3.getNextWarning().getNextWarning() == third, "3-warning chain");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
