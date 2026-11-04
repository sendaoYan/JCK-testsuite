/*
 * @test
 * @summary java.sql.Timestamp conformance: white-box structure + black-box behavior.
 * @run main TimestampTest
 */
public class TimestampTest {
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
        Class<?> c = Class.forName("java.sql.Timestamp");
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.util.Date"), "superclass");
        check(c.getInterfaces().length == 0, "no direct superinterface");
        check(c.getDeclaredMethods().length == 18, "declared method count 18");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.sql.Timestamp ts = java.sql.Timestamp.valueOf("2026-01-01 12:34:56.123456789");
        check("2026-01-01 12:34:56.123456789".equals(ts.toString()), "valueOf(String).toString roundtrip");
        check(ts.getNanos() == 123456789, "getNanos");
        check(ts.toLocalDateTime().equals(java.time.LocalDateTime.of(2026, 1, 1, 12, 34, 56, 123456789)), "toLocalDateTime");
        check(java.sql.Timestamp.valueOf(ts.toLocalDateTime()).equals(ts), "valueOf(LocalDateTime) roundtrip");
        java.time.Instant ins = java.time.Instant.parse("2026-01-01T12:34:56.123456789Z");
        java.sql.Timestamp fromIns = java.sql.Timestamp.from(ins);
        check(fromIns.toInstant().equals(ins), "from/toInstant roundtrip");
        check(fromIns.getNanos() == 123456789, "from nanos");
        java.sql.Timestamp t0 = new java.sql.Timestamp(0L);
        t0.setNanos(999999999);
        check(t0.getNanos() == 999999999, "setNanos max");
        checkThrows(IllegalArgumentException.class, "setNanos negative", () -> { java.sql.Timestamp x = new java.sql.Timestamp(0L); x.setNanos(-1); });
        checkThrows(IllegalArgumentException.class, "setNanos too big", () -> { java.sql.Timestamp x = new java.sql.Timestamp(0L); x.setNanos(1000000000); });
        checkThrows(IllegalArgumentException.class, "valueOf bad format", () -> java.sql.Timestamp.valueOf("2026-01-01"));
        java.sql.Timestamp e1 = java.sql.Timestamp.valueOf("2026-01-01 12:34:56.123");
        java.sql.Timestamp e2 = java.sql.Timestamp.valueOf("2026-01-01 12:34:56.123");
        check(e1.equals(e2) && e2.equals(e1), "equals symmetric");
        check(e1.hashCode() == e2.hashCode(), "hashCode");
        java.sql.Timestamp c1 = java.sql.Timestamp.valueOf("2026-01-01 12:34:56.000000001");
        java.sql.Timestamp c2 = java.sql.Timestamp.valueOf("2026-01-01 12:34:56.000000002");
        check(c1.compareTo(c2) < 0, "compareTo nanos less");
        check(c1.before(c2), "before nanos");
        check(c2.after(c1), "after nanos");
        java.sql.Timestamp st = new java.sql.Timestamp(0L);
        st.setTime(5000L);
        check(st.getTime() == 5000L, "setTime/getTime");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
