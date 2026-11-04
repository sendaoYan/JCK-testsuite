/*
 * @test
 * @summary java.sql.Timestamp API conformance: full contract coverage.
 *          constructors (7-int) nano-boundary IAE + (long) fractional-second
 *          decomposition (incl. negative millis), setTime/getTime/nanos
 *          state invariants, setNanos IAE bounds, valueOf(String) valid
 *          fractions 1..9 digits + invalid matrix (null/no-space/bad-date/
 *          >9-digit fraction/bare period/non-digit), toString trailing-zero
 *          truncation and ".0" minimum, nanos!=0 vs getTime, equals(Timestamp)
 *          and the documented asymmetry with java.util.Date (equals(Object)
 *          false for non-Timestamp), hashCode excludes nanos, before/after,
 *          compareTo(Timestamp/Date/Object) incl. CCE, valueOf(LocalDateTime)/
 *          toLocalDateTime, from(Instant)/toInstant, serialization round-trip
 *          + serialVersionUID. Time-zone independent.
 * @run main TimestampTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.time.Instant;
import java.time.LocalDateTime;

public class TimestampTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws Exception {
        testIntConstructor();
        testLongConstructor();
        testSetTimeGetNanos();
        testSetNanos();
        testValueOfValid();
        testValueOfInvalid();
        testToString();
        testEqualsHashCode();
        testCompareToBeforeAfter();
        testLocalDateTimeConversion();
        testInstantConversion();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }

    @SuppressWarnings("deprecation")
    static void testIntConstructor() {
        java.sql.Timestamp t = new java.sql.Timestamp(120, 0, 2, 3, 4, 5, 123456789);
        check(t.getNanos() == 123456789, "7-int constructor nanos");
        check("2020-01-02 03:04:05.123456789".equals(t.toString()),
                "7-int constructor toString");

        java.sql.Timestamp min = new java.sql.Timestamp(120, 0, 2, 3, 4, 5, 0);
        check(min.getNanos() == 0, "7-int constructor nanos 0");
        java.sql.Timestamp max = new java.sql.Timestamp(120, 0, 2, 3, 4, 5, 999999999);
        check(max.getNanos() == 999999999, "7-int constructor nanos max");

        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { new java.sql.Timestamp(120, 0, 2, 3, 4, 5, -1); }
        }, "7-int constructor nanos -1");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { new java.sql.Timestamp(120, 0, 2, 3, 4, 5, 1000000000); }
        }, "7-int constructor nanos 1_000_000_000");
    }

    static void testLongConstructor() {
        long[] cases = {0L, 1L, -1L, 1000L, 1500L, -1500L, Long.MAX_VALUE, Long.MIN_VALUE};
        for (long m : cases) {
            java.sql.Timestamp t = new java.sql.Timestamp(m);
            check(t.getTime() == m, "Timestamp(long) getTime round-trip " + m);
        }
        check(new java.sql.Timestamp(1500L).getNanos() == 500000000,
                "Timestamp(1500) nanos 500000000");
        check(new java.sql.Timestamp(-1500L).getNanos() == 500000000,
                "Timestamp(-1500) nanos 500000000 (negative normalized)");
        check(new java.sql.Timestamp(-1L).getNanos() == 999000000,
                "Timestamp(-1) nanos 999000000");
        check(new java.sql.Timestamp(1000L).getNanos() == 0,
                "Timestamp(1000) nanos 0");
    }

    static void testSetTimeGetNanos() {
        java.sql.Timestamp t = new java.sql.Timestamp(0L);
        t.setTime(1500L);
        check(t.getTime() == 1500L, "setTime getTime");
        check(t.getNanos() == 500000000, "setTime nanos");
        t.setTime(-1L);
        check(t.getTime() == -1L, "setTime negative getTime");
        check(t.getNanos() == 999000000, "setTime negative nanos");
    }

    static void testSetNanos() {
        final java.sql.Timestamp t = new java.sql.Timestamp(0L);
        t.setNanos(123456789);
        check(t.getNanos() == 123456789, "setNanos valid");
        t.setNanos(0);
        check(t.getNanos() == 0, "setNanos 0");
        t.setNanos(999999999);
        check(t.getNanos() == 999999999, "setNanos max");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { t.setNanos(-1); }
        }, "setNanos(-1)");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { t.setNanos(1000000000); }
        }, "setNanos(1_000_000_000)");
    }

    static void testValueOfValid() {
        check("2020-01-02 03:04:05.0".equals(
                java.sql.Timestamp.valueOf("2020-01-02 03:04:05").toString()),
                "valueOf no fraction -> .0");
        check("2020-01-02 03:04:05.123456789".equals(
                java.sql.Timestamp.valueOf("2020-01-02 03:04:05.123456789").toString()),
                "valueOf 9-digit fraction");
        check(123456789 == java.sql.Timestamp.valueOf("2020-01-02 03:04:05.123456789").getNanos(),
                "valueOf 9-digit nanos");
        check("2020-01-02 03:04:05.1".equals(
                java.sql.Timestamp.valueOf("2020-01-02 03:04:05.1").toString()),
                "valueOf 1-digit fraction truncates");
        check("2020-01-02 03:04:05.12".equals(
                java.sql.Timestamp.valueOf("2020-01-02 03:04:05.120").toString()),
                "valueOf trailing zero truncated");
        check("2020-01-02 03:04:05.0".equals(
                java.sql.Timestamp.valueOf("2020-1-2 3:4:5").toString()),
                "valueOf single-digit date/time normalized");
        check("2020-01-02 03:04:05.0".equals(
                java.sql.Timestamp.valueOf("  2020-01-02 03:04:05  ").toString()),
                "valueOf trims whitespace");
    }

    static void testValueOfInvalid() {
        String[] bad = {
            null, "", " ", "2020-01-02", "2020-01-02 03:04",
            "2020-01-02 03:04:05.1234567890", "2020-13-01 03:04:05",
            "2020-00-01 03:04:05", "2020-01-00 03:04:05",
            "2020-01-32 03:04:05", "20-01-02 03:04:05",
            "2020-01-02 03:04:05.", "2020-01-02 03:04:05.x",
            "2020-01-02 03:04:05.x1", "2020-01-02 03:04", "2020-01-02 :04:05"
        };
        for (final String s : bad) {
            checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
                public void run() { java.sql.Timestamp.valueOf(s); }
            }, "valueOf(" + s + ")");
        }
    }

    static void testToString() {
        check("1970-01-01 00:00:00.0".equals(
                java.sql.Timestamp.valueOf("1970-01-01 00:00:00").toString()),
                "toString nanos 0 -> .0");
        check("0999-12-31 23:59:59.0".equals(
                java.sql.Timestamp.valueOf("0999-12-31 23:59:59").toString()),
                "toString year <1000 padded");
        check("0001-01-01 00:00:00.0".equals(
                java.sql.Timestamp.valueOf("0001-01-01 00:00:00").toString()),
                "toString year 1 padded");
        check("2020-01-02 03:04:05.123".equals(
                java.sql.Timestamp.valueOf("2020-01-02 03:04:05.123").toString()),
                "toString trailing zeros truncated");
    }

    static void testEqualsHashCode() {
        java.sql.Timestamp a = java.sql.Timestamp.valueOf("2020-01-02 03:04:05.123456789");
        java.sql.Timestamp b = java.sql.Timestamp.valueOf("2020-01-02 03:04:05.123456789");
        java.sql.Timestamp c = java.sql.Timestamp.valueOf("2020-01-02 03:04:05.123456788");
        java.util.Date ud = new java.util.Date(a.getTime());

        check(a.equals(b), "equals(Timestamp) same millis+nanos");
        check(a.equals((Object) b), "equals(Object) Timestamp");
        check(!a.equals(c), "equals(Timestamp) different nanos");
        check(!a.equals(null), "equals(null)");
        check(!a.equals(ud), "equals(Object) java.util.Date -> false (asymmetry)");
        check(ud.equals(a), "java.util.Date.equals(Timestamp) -> true (asymmetry)");
        check(!a.equals("x"), "equals(non-Date)");

        // hashCode excludes nanos: same integral second, different nanos.
        java.sql.Timestamp n1 = java.sql.Timestamp.valueOf("2020-01-02 03:04:05.000000001");
        java.sql.Timestamp n2 = java.sql.Timestamp.valueOf("2020-01-02 03:04:05.000000002");
        check(n1.hashCode() == n2.hashCode(), "hashCode excludes nanos");
        check(!n1.equals(n2), "nanos difference breaks equals");
    }

    static void testCompareToBeforeAfter() {
        java.sql.Timestamp a = java.sql.Timestamp.valueOf("2020-01-01 00:00:00");
        java.sql.Timestamp b = java.sql.Timestamp.valueOf("2020-01-01 00:00:00.000000001");
        java.sql.Timestamp c = java.sql.Timestamp.valueOf("2020-01-01 00:00:01");
        java.sql.Timestamp a2 = java.sql.Timestamp.valueOf("2020-01-01 00:00:00");
        java.util.Date ud = new java.util.Date(a.getTime());

        check(a.compareTo(b) < 0, "compareTo(Timestamp) nanos ordering");
        check(b.compareTo(a) > 0, "compareTo(Timestamp) nanos ordering reverse");
        check(a.compareTo(a2) == 0, "compareTo(Timestamp) equal");
        check(b.compareTo(c) < 0, "compareTo(Timestamp) second vs nanos");
        check(a.compareTo(ud) == 0, "compareTo(java.util.Date) equal");
        check(a.compareTo((java.util.Date) a2) == 0, "compareTo(Date) Timestamp arg");

        check(a.before(b), "before nanos");
        check(b.after(a), "after nanos");
        check(!a.before(a), "!before self");
        check(!a.after(a), "!after self");

        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { a.compareTo((java.sql.Timestamp) null); }
        }, "compareTo(null Timestamp)");
        checkThrows(ClassCastException.class, new ThrowingRunnable() {
            public void run() { ((Comparable) a).compareTo("not a date"); }
        }, "compareTo(Object non-Date)");
    }

    static void testLocalDateTimeConversion() {
        LocalDateTime[] dts = {
            LocalDateTime.of(2020, 1, 2, 3, 4, 5, 123456789),
            LocalDateTime.of(1970, 1, 1, 0, 0, 0, 0),
            LocalDateTime.of(9999, 12, 31, 23, 59, 59, 999999999),
            LocalDateTime.of(1, 1, 1, 0, 0, 0, 1)
        };
        for (LocalDateTime ldt : dts) {
            java.sql.Timestamp t = java.sql.Timestamp.valueOf(ldt);
            check(ldt.equals(t.toLocalDateTime()), "valueOf(LocalDateTime)/toLocalDateTime round-trip " + ldt);
        }
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { java.sql.Timestamp.valueOf((LocalDateTime) null); }
        }, "valueOf(null LocalDateTime)");
    }

    static void testInstantConversion() {
        Instant[] instants = {
            Instant.ofEpochSecond(0, 123456789),
            Instant.ofEpochSecond(1, 1),
            Instant.ofEpochSecond(-1, 500000000),
            Instant.ofEpochSecond(1580000000L, 999999999),
            Instant.ofEpochSecond(-1580000000L, 1)
        };
        for (Instant inst : instants) {
            java.sql.Timestamp t = java.sql.Timestamp.from(inst);
            check(inst.equals(t.toInstant()), "from(Instant)/toInstant round-trip " + inst);
        }
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { java.sql.Timestamp.from((Instant) null); }
        }, "from(null Instant)");
    }

    static void testSerialization() throws Exception {
        check(ObjectStreamClass.lookup(java.sql.Timestamp.class).getSerialVersionUID() == 2745179027874758501L,
                "serialVersionUID");
        java.sql.Timestamp[] ts = {
            java.sql.Timestamp.valueOf("2020-01-02 03:04:05.123456789"),
            java.sql.Timestamp.valueOf("2020-01-02 03:04:05"),
            new java.sql.Timestamp(-1500L),
            new java.sql.Timestamp(Long.MAX_VALUE)
        };
        for (java.sql.Timestamp t : ts) {
            java.sql.Timestamp back = roundTrip(t);
            check(back.getTime() == t.getTime(), "serialization round-trip getTime");
            check(back.getNanos() == t.getNanos(), "serialization round-trip nanos");
            check(back.equals(t), "serialization round-trip equals");
        }
    }

    static java.sql.Timestamp roundTrip(java.sql.Timestamp t) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(t);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        java.sql.Timestamp back = (java.sql.Timestamp) ois.readObject();
        ois.close();
        return back;
    }
}
