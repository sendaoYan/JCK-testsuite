/*
 * @test
 * @summary java.sql.Time API conformance: full contract coverage.
 *          constructors (int,int,int) and (long) incl. getTime round-trip,
 *          valueOf(String) valid formats + precise IllegalArgumentException
 *          matrix (null/empty/missing-colon/extra-colon/non-numeric) and
 *          out-of-range hour normalization, deprecated date accessors/mutators
 *          all throw IAE, toInstant() throws UOE, valueOf(LocalTime)/
 *          toLocalTime round-trip + null NPE, equals/hashCode cross-impl vs
 *          java.util.Date, compareTo/before/after, serialization round-trip +
 *          serialVersionUID. All assertions are time-zone independent.
 * @run main TimeTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.time.LocalTime;

public class TimeTest {
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
        testConstructors();
        testValueOfValid();
        testValueOfInvalid();
        testValueOfNormalization();
        testDeprecatedDateAccessorsThrow();
        testToInstantThrows();
        testLocalTimeConversion();
        testEqualsHashCode();
        testCompareToBeforeAfter();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }

    @SuppressWarnings("deprecation")
    static void testConstructors() {
        java.sql.Time t = new java.sql.Time(1, 2, 3);
        check("01:02:03".equals(t.toString()), "Time(int,int,int) toString");
        java.sql.Time t2 = new java.sql.Time(23, 59, 59);
        check("23:59:59".equals(t2.toString()), "Time(int,int,int) max");
        java.sql.Time t3 = new java.sql.Time(0, 0, 0);
        check("00:00:00".equals(t3.toString()), "Time(int,int,int) min");

        long[] millis = {0L, 1L, -1L, 1000L, -1000L, 86399999L, Long.MAX_VALUE, Long.MIN_VALUE};
        for (long m : millis) {
            java.sql.Time mt = new java.sql.Time(m);
            check(mt.getTime() == m, "Time(long) getTime round-trip " + m);
        }
    }

    static void testValueOfValid() {
        String[] canonical = {
            "00:00:00", "01:02:03", "23:59:59", "12:34:56", "09:08:07"
        };
        for (String s : canonical) {
            check(s.equals(java.sql.Time.valueOf(s).toString()),
                    "valueOf(" + s + ") canonical toString");
        }
        check("01:02:03".equals(java.sql.Time.valueOf("1:2:3").toString()),
                "valueOf single-digit h/m/s normalized");
    }

    static void testValueOfInvalid() {
        String[] bad = {
            null, "", " ", "010203", "1", "01:02", "01:02:03:", ":01:02:03",
            "::", "01::03", "01:02:", "01:02:0x", "0x:02:03"
        };
        for (final String s : bad) {
            checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
                public void run() { java.sql.Time.valueOf(s); }
            }, "valueOf(" + s + ")");
        }
    }

    static void testValueOfNormalization() {
        // hour/minute/second are not range-checked; the deprecated util.Date
        // constructor normalizes them.
        check("01:00:00".equals(java.sql.Time.valueOf("25:00:00").toString()),
                "valueOf hour 25 normalizes to 01:00:00");
        check("00:00:00".equals(java.sql.Time.valueOf("24:00:00").toString()),
                "valueOf hour 24 normalizes to 00:00:00");
        check("01:01:00".equals(java.sql.Time.valueOf("00:61:00").toString()),
                "valueOf minute 61 normalizes to 01:01:00");
        check("00:01:01".equals(java.sql.Time.valueOf("00:00:61").toString()),
                "valueOf second 61 normalizes to 00:01:01");
    }

    @SuppressWarnings("deprecation")
    static void testDeprecatedDateAccessorsThrow() {
        final java.sql.Time t = java.sql.Time.valueOf("01:02:03");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { t.getYear(); }
        }, "getYear");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { t.getMonth(); }
        }, "getMonth");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { t.getDay(); }
        }, "getDay");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { t.getDate(); }
        }, "getDate");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { t.setYear(2020); }
        }, "setYear");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { t.setMonth(1); }
        }, "setMonth");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { t.setDate(2); }
        }, "setDate");
    }

    static void testToInstantThrows() {
        final java.sql.Time t = java.sql.Time.valueOf("01:02:03");
        checkThrows(UnsupportedOperationException.class, new ThrowingRunnable() {
            public void run() { t.toInstant(); }
        }, "toInstant");
    }

    static void testLocalTimeConversion() {
        LocalTime[] times = {
            LocalTime.of(1, 2, 3), LocalTime.of(0, 0, 0),
            LocalTime.of(23, 59, 59), LocalTime.of(12, 34, 56)
        };
        for (LocalTime lt : times) {
            java.sql.Time t = java.sql.Time.valueOf(lt);
            check(lt.equals(t.toLocalTime()), "valueOf(LocalTime)/toLocalTime round-trip " + lt);
        }
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { java.sql.Time.valueOf((LocalTime) null); }
        }, "valueOf(null LocalTime)");
    }

    static void testEqualsHashCode() {
        java.sql.Time a = new java.sql.Time(0L);
        java.sql.Time b = new java.sql.Time(0L);
        java.sql.Time c = new java.sql.Time(1000L);
        java.util.Date ud = new java.util.Date(0L);

        check(a.equals(b), "Time.equals(Time) same millis");
        check(a.equals(ud), "Time.equals(java.util.Date) same millis");
        check(ud.equals(a), "java.util.Date.equals(Time) symmetric");
        check(a.hashCode() == ud.hashCode(), "Time.hashCode == java.util.Date.hashCode");
        check(!a.equals(c), "Time.equals(Time) different millis");
        check(!a.equals(null), "Time.equals(null)");
        check(!a.equals("time"), "Time.equals(non-Date)");
    }

    static void testCompareToBeforeAfter() {
        java.sql.Time a = java.sql.Time.valueOf("01:00:00");
        java.sql.Time b = java.sql.Time.valueOf("02:00:00");
        java.sql.Time c = java.sql.Time.valueOf("02:00:00");
        java.util.Date ua = new java.util.Date(a.getTime());

        check(a.compareTo(b) < 0, "compareTo earlier < 0");
        check(b.compareTo(a) > 0, "compareTo later > 0");
        check(b.compareTo(c) == 0, "compareTo equal == 0");
        check(a.compareTo(ua) == 0, "compareTo(java.util.Date) equal");
        check(a.before(b), "before");
        check(b.after(a), "after");
        check(!a.before(a), "!before self");
        check(!a.after(a), "!after self");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { a.compareTo(null); }
        }, "compareTo(null)");
    }

    static void testSerialization() throws Exception {
        check(ObjectStreamClass.lookup(java.sql.Time.class).getSerialVersionUID() == 8397324403548013681L,
                "serialVersionUID");
        long[] millis = {0L, 1L, -1L, 1000L, -1000L, 86399999L};
        for (long m : millis) {
            java.sql.Time t = new java.sql.Time(m);
            java.sql.Time back = roundTrip(t);
            check(back.getTime() == t.getTime(), "serialization round-trip getTime " + m);
            check(back.equals(t), "serialization round-trip equals " + m);
        }
    }

    static java.sql.Time roundTrip(java.sql.Time t) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(t);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        java.sql.Time back = (java.sql.Time) ois.readObject();
        ois.close();
        return back;
    }
}
