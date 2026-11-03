/*
 * @test
 * @summary java.sql.SQLWarning conformance: white-box structure + black-box behavior.
 * @run main SQLWarningTest
 */
public class SQLWarningTest {
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
        Class<?> c = Class.forName("java.sql.SQLWarning");
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.sql.SQLException"), "superclass");
        check(c.getInterfaces().length == 0, "no direct superinterface");
        check(c.getDeclaredMethods().length == 2, "declared method count 2");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.sql.SQLWarning w = new java.sql.SQLWarning();
        check(w.getMessage() == null && w.getSQLState() == null && w.getErrorCode() == 0, "no-arg");
        java.sql.SQLWarning w1 = new java.sql.SQLWarning("m", "42S01", 99);
        check("m".equals(w1.getMessage()) && "42S01".equals(w1.getSQLState()) && w1.getErrorCode() == 99, "3-arg");
        java.sql.SQLWarning w2 = new java.sql.SQLWarning("next");
        w1.setNextWarning(w2);
        check(w1.getNextWarning() == w2, "setNextWarning/getNextWarning");
        check(w2.getNextWarning() == null, "next warning none");
        RuntimeException cause = new RuntimeException("c");
        java.sql.SQLWarning w3 = new java.sql.SQLWarning("m", "s", 1, cause);
        check(w3.getCause() == cause, "cause");
        check(w1 instanceof java.sql.SQLException, "is SQLException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
