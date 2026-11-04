/*
 * @test
 * @summary java.sql.RowId conformance: white-box structure + black-box behavior.
 * @run main RowIdTest
 */
public class RowIdTest {
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
        Class<?> c = Class.forName("java.sql.RowId");
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no direct superinterface");
        check(c.getDeclaredMethods().length == 4, "declared method count 4");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final byte[] bytes = {1, 2, 3};
        java.sql.RowId r = new java.sql.RowId() {
            public byte[] getBytes() { return bytes; }
            public String toString() { return "ROWID"; }
            public int hashCode() { return java.util.Arrays.hashCode(bytes); }
            public boolean equals(Object o) { return o instanceof java.sql.RowId && java.util.Arrays.equals(bytes, ((java.sql.RowId) o).getBytes()); }
        };
        check(java.util.Arrays.equals(r.getBytes(), bytes), "getBytes");
        check("ROWID".equals(r.toString()), "toString");
        check(r.equals(r), "equals self");
        check(r.hashCode() == java.util.Arrays.hashCode(bytes), "hashCode");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
