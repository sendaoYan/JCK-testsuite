/*
 * @test
 * @summary java.sql.BatchUpdateException conformance: white-box structure + black-box behavior.
 * @run main BatchUpdateExceptionTest
 */
public class BatchUpdateExceptionTest {
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
        Class<?> c = Class.forName("java.sql.BatchUpdateException");
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.sql.SQLException"), "superclass");
        check(c.getInterfaces().length == 0, "no direct superinterface");
        check(c.getDeclaredMethods().length == 6, "declared method count 6");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        int[] counts = {1, 2, 3};
        java.sql.BatchUpdateException b = new java.sql.BatchUpdateException("batch", "22S01", 9, counts, null);
        check(java.util.Arrays.equals(b.getUpdateCounts(), counts), "getUpdateCounts int[]");
        long[] large = {10L, 20L};
        java.sql.BatchUpdateException b2 = new java.sql.BatchUpdateException("batch2", "22S02", 8, large, null);
        check(java.util.Arrays.equals(b2.getLargeUpdateCounts(), large), "getLargeUpdateCounts long[]");
        java.sql.BatchUpdateException b3 = new java.sql.BatchUpdateException();
        check(b3.getUpdateCounts() == null, "no-arg update counts null");
        java.sql.BatchUpdateException b4 = new java.sql.BatchUpdateException(new int[]{5});
        check(b4.getUpdateCounts().length == 1 && b4.getUpdateCounts()[0] == 5, "int[] ctor");
        check(b instanceof java.sql.SQLException, "is SQLException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
