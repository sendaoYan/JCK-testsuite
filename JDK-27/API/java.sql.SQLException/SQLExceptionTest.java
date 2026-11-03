/*
 * @test
 * @summary java.sql.SQLException conformance: white-box structure + black-box behavior.
 * @run main SQLExceptionTest
 */
public class SQLExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface Throwing { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> t, String label, Throwing r) {
        try { r.run(); } catch (Throwable x) { check(t.isInstance(x), label + " throws " + t.getSimpleName()); return; }
        throw new AssertionError(label + ": expected " + t.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.sql.SQLException");
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "superclass");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("java.lang.Iterable"), "superinterface 0");
        check(c.getDeclaredMethods().length == 5, "declared method count 5");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
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
        java.sql.SQLException e3 = new java.sql.SQLException(cause);
        check(e3.getCause() == cause, "cause-only constructor");
        java.sql.SQLException e4 = new java.sql.SQLException("m", cause);
        check("m".equals(e4.getMessage()) && e4.getCause() == cause, "message+cause");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
