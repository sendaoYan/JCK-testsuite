/*
 * @test
 * @summary java.sql.Date conformance: white-box structure + black-box behavior.
 * @run main DateTest
 */
public class DateTest {
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
        Class<?> c = Class.forName("java.sql.Date");
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.util.Date"), "superclass");
        check(c.getInterfaces().length == 0, "no direct superinterface");
        check(c.getDeclaredMethods().length == 13, "declared method count 13");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.sql.Date d = java.sql.Date.valueOf("2026-01-01");
        check("2026-01-01".equals(d.toString()), "valueOf(String).toString roundtrip");
        check(d.toLocalDate().equals(java.time.LocalDate.of(2026, 1, 1)), "toLocalDate");
        check(d.toLocalDate().equals(java.sql.Date.valueOf(java.time.LocalDate.of(2026, 1, 1)).toLocalDate()), "valueOf(LocalDate) roundtrip");
        check(java.sql.Date.valueOf(java.time.LocalDate.of(2026, 3, 15)).toLocalDate().equals(java.time.LocalDate.of(2026, 3, 15)), "valueOf(LocalDate) value");
        checkThrows(IllegalArgumentException.class, "valueOf bad separator", () -> java.sql.Date.valueOf("2026/01/01"));
        checkThrows(IllegalArgumentException.class, "valueOf non-date", () -> java.sql.Date.valueOf("not-a-date"));
        checkThrows(UnsupportedOperationException.class, "toInstant throws", () -> d.toInstant());
        java.sql.Date same = java.sql.Date.valueOf("2026-01-01");
        check(d.equals(same), "equals");
        check(d.hashCode() == same.hashCode(), "hashCode");
        java.sql.Date later = java.sql.Date.valueOf("2026-01-02");
        check(d.compareTo(later) < 0, "compareTo less");
        check(d.before(later), "before");
        check(later.after(d), "after");
        check(d.compareTo(d) == 0, "compareTo equal");
        java.sql.Date fromEpoch = new java.sql.Date(0L);
        fromEpoch.setTime(5000L);
        check(fromEpoch.getTime() == 5000L, "setTime/getTime");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
