/*
 * @test
 * @summary java.sql.Time conformance: white-box structure + black-box behavior.
 * @run main TimeTest
 */
public class TimeTest {
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
        Class<?> c = Class.forName("java.sql.Time");
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
        java.sql.Time t = java.sql.Time.valueOf("12:34:56");
        check("12:34:56".equals(t.toString()), "valueOf(String).toString roundtrip");
        check(t.toLocalTime().equals(java.time.LocalTime.of(12, 34, 56)), "toLocalTime");
        check(java.sql.Time.valueOf(java.time.LocalTime.of(23, 59, 59)).toLocalTime().equals(java.time.LocalTime.of(23, 59, 59)), "valueOf(LocalTime) value");
        checkThrows(IllegalArgumentException.class, "valueOf bad format", () -> java.sql.Time.valueOf("12:34"));
        checkThrows(UnsupportedOperationException.class, "toInstant throws", () -> t.toInstant());
        java.sql.Time same = java.sql.Time.valueOf("12:34:56");
        check(t.equals(same), "equals");
        check(t.hashCode() == same.hashCode(), "hashCode");
        java.sql.Time later = java.sql.Time.valueOf("12:34:57");
        check(t.compareTo(later) < 0, "compareTo less");
        check(t.before(later), "before");
        check(later.after(t), "after");
        java.sql.Time fromEpoch = new java.sql.Time(0L);
        fromEpoch.setTime(5000L);
        check(fromEpoch.getTime() == 5000L, "setTime/getTime");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
