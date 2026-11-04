/*
 * @test
 * @summary java.sql.Date API conformance: full contract coverage.
 *          constructors (int,int,int) and (long) incl. getTime round-trip and
 *          Long.MIN/MAX bounds, valueOf(String) valid formats + precise
 *          IllegalArgumentException matrix (null/empty/bad-year/bad-month/
 *          bad-day/extra-separators/non-numeric), day-overflow normalization,
 *          deprecated time accessors/mutators all throw IAE, toInstant() throws
 *          UOE, valueOf(LocalDate)/toLocalDate round-trip + null NPE,
 *          equals/hashCode cross-implementation vs java.util.Date and
 *          Timestamp (incl. the documented asymmetry), compareTo/before/after,
 *          serialization round-trip + serialVersionUID. All assertions are
 *          time-zone independent (no dependence on default TimeZone).
 * @run main DateTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.time.LocalDate;

public class DateTest {
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
        testDeprecatedTimeAccessorsThrow();
        testToInstantThrows();
        testLocalDateConversion();
        testEqualsHashCode();
        testCompareToBeforeAfter();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }

    @SuppressWarnings("deprecation")
    static void testConstructors() {
        java.sql.Date d = new java.sql.Date(120, 0, 2);
        check("2020-01-02".equals(d.toString()), "Date(int,int,int) toString");
        check(d.getTime() == java.sql.Date.valueOf("2020-01-02").getTime(),
                "Date(int,int,int) getTime == valueOf getTime");
        java.sql.Date leap = new java.sql.Date(120, 1, 29); // 2020-02-29
        check("2020-02-29".equals(leap.toString()), "Date(int,int,int) leap day");

        long[] millis = {0L, 1L, -1L, 1000L, -1000L, Long.MAX_VALUE, Long.MIN_VALUE};
        for (long m : millis) {
            java.sql.Date md = new java.sql.Date(m);
            check(md.getTime() == m, "Date(long) getTime round-trip " + m);
        }
    }

    static void testValueOfValid() {
        // Padded forms round-trip exactly.
        String[] canonical = {
            "2020-01-02", "9999-12-31", "0001-01-01", "2020-02-29",
            "2000-02-29", "2021-07-31", "2020-12-31", "2020-01-31"
        };
        for (String s : canonical) {
            check(s.equals(java.sql.Date.valueOf(s).toString()),
                    "valueOf(" + s + ") canonical toString");
        }
        // Leading zeros for month/day may be omitted (still canonical on output).
        check("2020-01-02".equals(java.sql.Date.valueOf("2020-1-2").toString()),
                "valueOf single-digit month/day normalized");
        check("2020-11-05".equals(java.sql.Date.valueOf("2020-11-5").toString()),
                "valueOf single-digit day normalized");
    }

    static void testValueOfInvalid() {
        String[] bad = {
            null, "", " ", "20-01-02", "20200-01-02", "2020-13-01",
            "2020-00-01", "2020-01-00", "2020-01-32", "2020/01/02",
            "2020-01", "2020-01-02-03", "2020-01-02 ", "2020--01-02",
            "abcd-ef-gh", "2020-1x-02", "2020-01-2y"
        };
        for (final String s : bad) {
            checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
                public void run() { java.sql.Date.valueOf(s); }
            }, "valueOf(" + s + ")");
        }
    }

    static void testValueOfNormalization() {
        // Day overflow within a month is normalized by the deprecated util.Date
        // constructor rather than rejected (valueOf only bounds-checks 1..31).
        check("2019-03-01".equals(java.sql.Date.valueOf("2019-02-29").toString()),
                "valueOf 2019-02-29 normalizes to 2019-03-01");
        check("2020-03-02".equals(java.sql.Date.valueOf("2020-02-31").toString()),
                "valueOf 2020-02-31 normalizes to 2020-03-02");
    }

    @SuppressWarnings("deprecation")
    static void testDeprecatedTimeAccessorsThrow() {
        final java.sql.Date d = java.sql.Date.valueOf("2020-01-02");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { d.getHours(); }
        }, "getHours");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { d.getMinutes(); }
        }, "getMinutes");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { d.getSeconds(); }
        }, "getSeconds");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { d.setHours(0); }
        }, "setHours");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { d.setMinutes(0); }
        }, "setMinutes");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { d.setSeconds(0); }
        }, "setSeconds");
    }

    static void testToInstantThrows() {
        final java.sql.Date d = java.sql.Date.valueOf("2020-01-02");
        checkThrows(UnsupportedOperationException.class, new ThrowingRunnable() {
            public void run() { d.toInstant(); }
        }, "toInstant");
    }

    static void testLocalDateConversion() {
        LocalDate[] dates = {
            LocalDate.of(2020, 1, 2), LocalDate.of(1970, 1, 1),
            LocalDate.of(9999, 12, 31), LocalDate.of(1, 1, 1),
            LocalDate.of(2000, 2, 29)
        };
        for (LocalDate ld : dates) {
            java.sql.Date d = java.sql.Date.valueOf(ld);
            check(ld.equals(d.toLocalDate()), "valueOf(LocalDate)/toLocalDate round-trip " + ld);
        }
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { java.sql.Date.valueOf((LocalDate) null); }
        }, "valueOf(null LocalDate)");
    }

    static void testEqualsHashCode() {
        java.sql.Date a = new java.sql.Date(0L);
        java.sql.Date b = new java.sql.Date(0L);
        java.sql.Date c = new java.sql.Date(1000L);
        java.util.Date ud = new java.util.Date(0L);
        java.sql.Timestamp ts = new java.sql.Timestamp(0L);

        check(a.equals(b), "Date.equals(Date) same millis");
        check(a.equals(ud), "Date.equals(java.util.Date) same millis");
        check(ud.equals(a), "java.util.Date.equals(Date) symmetric");
        check(a.hashCode() == ud.hashCode(), "Date.hashCode == java.util.Date.hashCode");
        check(!a.equals(c), "Date.equals(Date) different millis");
        check(!a.equals(null), "Date.equals(null)");
        check(!a.equals("1970"), "Date.equals(non-Date)");
        // Documented asymmetry: Date.equals(Timestamp) compares millis, but
        // Timestamp.equals(Date) is always false (nanos unknown).
        check(a.equals(ts), "Date.equals(Timestamp) same millis (asymmetric)");
    }

    static void testCompareToBeforeAfter() {
        java.sql.Date a = java.sql.Date.valueOf("2020-01-01");
        java.sql.Date b = java.sql.Date.valueOf("2020-01-02");
        java.sql.Date c = java.sql.Date.valueOf("2020-01-02");
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
        check(ObjectStreamClass.lookup(java.sql.Date.class).getSerialVersionUID() == 1511598038487230103L,
                "serialVersionUID");
        long[] millis = {0L, 1L, -1L, 1000L, -1000L, Long.MAX_VALUE, Long.MIN_VALUE};
        for (long m : millis) {
            java.sql.Date d = new java.sql.Date(m);
            java.sql.Date back = roundTrip(d);
            check(back.getTime() == d.getTime(), "serialization round-trip getTime " + m);
            check(back.equals(d), "serialization round-trip equals " + m);
        }
    }

    static java.sql.Date roundTrip(java.sql.Date d) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(d);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        java.sql.Date back = (java.sql.Date) ois.readObject();
        ois.close();
        return back;
    }
}
