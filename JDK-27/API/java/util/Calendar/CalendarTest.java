/*
 * @test
 * @summary java.util.Calendar conformance: white-box structure plus black-box behavior of
 *          epoch fields in UTC, set/get/add/roll/getActualMaximum, time-in-millis round-trip,
 *          clone/equals, and before/after.
 * @run main CalendarTest
 */

import java.io.Serializable;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class CalendarTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Calendar utc() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Calendar");
        check(!c.isInterface(), "Calendar is a class, not an interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "Calendar is abstract");
        check(Serializable.class.isAssignableFrom(c), "Calendar implements Serializable");
        check(Cloneable.class.isAssignableFrom(c), "Calendar implements Cloneable");
        check(Comparable.class.isAssignableFrom(c), "Calendar implements Comparable");
        c.getMethod("get", int.class);
        c.getMethod("set", int.class, int.class);
        c.getMethod("add", int.class, int.class);
        c.getMethod("roll", int.class, int.class);
        c.getMethod("getTimeInMillis");
        c.getMethod("setTimeInMillis", long.class);
        c.getMethod("getActualMaximum", int.class);
        c.getMethod("getInstance", TimeZone.class, Locale.class);
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        testEpoch();
        testFieldOps();
        testAddVsRoll();
        testActualMaximum();
        testTimeInMillisRoundTrip();
        testCloneEquals();
        testLenientNullSerialization();
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
    }

    static void testLenientNullSerialization() throws Exception {
        Calendar cal = utc();
        cal.set(2020, Calendar.MAY, 15);
        expectThrows(NullPointerException.class, () -> cal.compareTo(null), "compareTo(null) NPE");
        check(!cal.before(null) && !cal.after(null), "before(null)/after(null) false (no NPE)");
        check(!cal.before("x") && !cal.after("x"), "before/after non-Calendar false");

        // lenient vs strict.
        check(cal.isLenient(), "default lenient");
        cal.setLenient(false);
        cal.clear();
        cal.set(2020, Calendar.MAY, 15);
        cal.set(Calendar.MONTH, 13);
        expectThrows(IllegalArgumentException.class, () -> cal.getTimeInMillis(),
                "non-lenient MONTH 13 IAE on compute");
        cal.clear();
        cal.set(2020, Calendar.MAY, 15);
        cal.set(Calendar.DAY_OF_MONTH, 32);
        expectThrows(IllegalArgumentException.class, () -> cal.getTimeInMillis(),
                "non-lenient DAY_OF_MONTH 32 IAE on compute");
        cal.setLenient(true);
        cal.clear();
        cal.set(2020, Calendar.DECEMBER, 15);
        cal.set(Calendar.MONTH, 12);
        check(cal.get(Calendar.MONTH) == Calendar.JANUARY && cal.get(Calendar.YEAR) == 2021,
              "lenient normalizes month 12");

        // week settings round-trip (no validation: values are stored as-is).
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        check(cal.getFirstDayOfWeek() == Calendar.MONDAY, "setFirstDayOfWeek MONDAY");
        cal.setMinimalDaysInFirstWeek(4);
        check(cal.getMinimalDaysInFirstWeek() == 4, "setMinimalDaysInFirstWeek");

        // min/max contracts.
        check(cal.getMinimum(Calendar.MONTH) == Calendar.JANUARY, "getMinimum(MONTH)");
        check(cal.getMaximum(Calendar.MONTH) == Calendar.DECEMBER, "getMaximum(MONTH)");
        check(cal.getActualMinimum(Calendar.MONTH) == Calendar.JANUARY, "getActualMinimum(MONTH)");
        check(cal.getActualMaximum(Calendar.MONTH) == Calendar.DECEMBER, "getActualMaximum(MONTH)");

        // serialization round-trip.
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(cal);
        }
        Calendar revived;
        try (java.io.ObjectInputStream ois =
                new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            revived = (Calendar) ois.readObject();
        }
        check(revived != cal && revived.equals(cal), "serialization roundtrip preserves equality");
    }

    static void testEpoch() {
        Calendar cal = utc();
        cal.setTimeInMillis(0L);
        check(cal.get(Calendar.YEAR) == 1970, "epoch YEAR");
        check(cal.get(Calendar.MONTH) == Calendar.JANUARY, "epoch MONTH");
        check(cal.get(Calendar.DAY_OF_MONTH) == 1, "epoch DAY_OF_MONTH");
        check(cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0,
              "epoch HOUR_OF_DAY/MINUTE");
        check(cal.getTime().getTime() == 0L, "getTime == epoch millis");
        check(cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY, "epoch day of week == Thursday");
    }

    static void testFieldOps() {
        Calendar cal = utc();
        cal.clear();
        cal.set(Calendar.YEAR, 2000);
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 29);
        check(cal.get(Calendar.YEAR) == 2000, "set YEAR");
        check(cal.get(Calendar.MONTH) == Calendar.FEBRUARY, "set MONTH");
        check(cal.get(Calendar.DAY_OF_MONTH) == 29, "set DAY_OF_MONTH");
        cal.add(Calendar.YEAR, 1);
        check(cal.get(Calendar.YEAR) == 2001, "add YEAR");

        Calendar later = utc();
        later.clear();
        later.set(2002, Calendar.JANUARY, 1);
        check(later.after(cal) && cal.before(later), "before/after");
    }

    static void testAddVsRoll() {
        Calendar cal = utc();
        cal.clear();
        cal.set(2020, Calendar.DECEMBER, 15);
        cal.add(Calendar.MONTH, 1);
        check(cal.get(Calendar.YEAR) == 2021 && cal.get(Calendar.MONTH) == Calendar.JANUARY,
              "add MONTH rolls year forward");

        Calendar r = utc();
        r.clear();
        r.set(2020, Calendar.DECEMBER, 15);
        r.roll(Calendar.MONTH, true);
        check(r.get(Calendar.YEAR) == 2020 && r.get(Calendar.MONTH) == Calendar.JANUARY,
              "roll MONTH stays in same year");
    }

    static void testActualMaximum() {
        Calendar leap = utc();
        leap.clear();
        leap.set(2000, Calendar.FEBRUARY, 1);
        check(leap.getActualMaximum(Calendar.DAY_OF_MONTH) == 29, "Feb 2000 has 29 days");

        Calendar normal = utc();
        normal.clear();
        normal.set(2001, Calendar.FEBRUARY, 1);
        check(normal.getActualMaximum(Calendar.DAY_OF_MONTH) == 28, "Feb 2001 has 28 days");

        Calendar apr = utc();
        apr.clear();
        apr.set(2021, Calendar.APRIL, 1);
        check(apr.getActualMaximum(Calendar.DAY_OF_MONTH) == 30, "April has 30 days");
    }

    static void testTimeInMillisRoundTrip() {
        Calendar cal = utc();
        long ms = 1600000000000L; // a fixed instant
        cal.setTimeInMillis(ms);
        check(cal.getTimeInMillis() == ms, "setTimeInMillis/getTimeInMillis round-trip");
    }

    static void testCloneEquals() {
        Calendar a = utc();
        a.clear();
        a.set(2020, Calendar.JULY, 4);
        Calendar b = (Calendar) a.clone();
        check(b != a, "clone returns distinct object");
        check(b.equals(a), "clone equals original");
        b.add(Calendar.DAY_OF_MONTH, 1);
        check(!b.equals(a), "mutating clone breaks equality");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
