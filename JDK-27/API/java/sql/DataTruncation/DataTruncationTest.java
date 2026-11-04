/*
 * @test
 * @summary java.sql.DataTruncation conformance: white-box structure + black-box behavior.
 * @run main DataTruncationTest
 */
public class DataTruncationTest {
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
        Class<?> c = Class.forName("java.sql.DataTruncation");
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.sql.SQLWarning"), "superclass");
        check(c.getInterfaces().length == 0, "no direct superinterface");
        check(c.getDeclaredMethods().length == 5, "declared method count 5");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.sql.DataTruncation d = new java.sql.DataTruncation(3, true, false, 10, 4);
        check(d.getIndex() == 3, "getIndex");
        check(d.getParameter(), "getParameter");
        check(!d.getRead(), "getRead");
        check(d.getDataSize() == 10, "getDataSize");
        check(d.getTransferSize() == 4, "getTransferSize");
        RuntimeException cause = new RuntimeException("c");
        java.sql.DataTruncation d2 = new java.sql.DataTruncation(1, false, true, 8, 8, cause);
        check(d2.getCause() == cause, "cause");
        check(d instanceof java.sql.SQLWarning && d instanceof java.sql.SQLException, "is SQLWarning/SQLException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
