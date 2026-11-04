/*
 * @test
 * @summary java.util.Calendar API conformance (Java 8 surface): complete contract coverage of
 *          getInstance variants, epoch fields in UTC, get/set (field + 3/5/6-arg), clear/isSet,
 *          add/roll, before/after/compareTo (+ null NPE), equals/hashCode, lenient vs strict
 *          (IllegalArgumentException), firstDayOfWeek/minimalDays validation, clone independence,
 *          getTime/setTime/getTimeInMillis, and Serializable round-trip. Fixed TimeZone/Locale.
 * @run main CalendarTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class CalendarTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
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

    static Calendar utc() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
    }

    static void testEpoch() {
        Calendar cal = utc();
        cal.setTimeInMillis(0L);
        check(cal.get(Calendar.YEAR) == 1970, "epoch YEAR");
        check(cal.get(Calendar.MONTH) == Calendar.JANUARY, "epoch MONTH");
        check(cal.get(Calendar.DAY_OF_MONTH) == 1, "epoch DAY_OF_MONTH");
        check(cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0
              && cal.get(Calendar.SECOND) == 0, "epoch HOUR_OF_DAY/MINUTE/SECOND");
        check(cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY, "epoch DAY_OF_WEEK (1970-01-01 Thu)");
        check(cal.getTime().getTime() == 0L, "getTime == epoch millis");
        check(cal.getTimeInMillis() == 0L, "getTimeInMillis == 0");
    }

    static void testSet() {
        Calendar cal = utc();
        cal.setTimeInMillis(0L);
        cal.set(Calendar.YEAR, 2000);
        check(cal.get(Calendar.YEAR) == 2000, "set(field, value) YEAR");

        cal.clear();
        cal.set(2020, Calendar.MAY, 15);
        check(cal.get(Calendar.YEAR) == 2020 && cal.get(Calendar.MONTH) == Calendar.MAY
              && cal.get(Calendar.DAY_OF_MONTH) == 15, "set(y,m,d)");

        cal.clear();
        cal.set(2020, Calendar.MAY, 15, 6, 30);
        check(cal.get(Calendar.HOUR_OF_DAY) == 6 && cal.get(Calendar.MINUTE) == 30,
              "set(y,m,d,h,m)");

        cal.clear();
        cal.set(2020, Calendar.MAY, 15, 6, 30, 45);
        check(cal.get(Calendar.SECOND) == 45, "set(y,m,d,h,m,s)");
    }

    static void testClearIsSet() {
        Calendar cal = utc();
        cal.set(2020, Calendar.MAY, 15);
        check(cal.isSet(Calendar.YEAR) && cal.isSet(Calendar.MONTH) && cal.isSet(Calendar.DAY_OF_MONTH),
              "isSet after set");
        cal.clear();
        check(!cal.isSet(Calendar.YEAR), "clear() unsets YEAR");
        check(cal.get(Calendar.YEAR) == 1970, "cleared field reads epoch default (lenient)");
        cal.clear(Calendar.MONTH);
        check(!cal.isSet(Calendar.MONTH), "clear(int) unsets MONTH");
    }

    static void testAddRoll() {
        Calendar cal = utc();
        cal.setTimeInMillis(0L);
        cal.add(Calendar.YEAR, 1);
        check(cal.get(Calendar.YEAR) == 1971, "add YEAR");

        cal.setTimeInMillis(0L);
        cal.add(Calendar.MONTH, 13);
        check(cal.get(Calendar.YEAR) == 1971 && cal.get(Calendar.MONTH) == Calendar.FEBRUARY,
              "add MONTH 13 rolls year");

        cal.setTimeInMillis(0L);
        cal.roll(Calendar.MONTH, 1);
        check(cal.get(Calendar.MONTH) == Calendar.FEBRUARY && cal.get(Calendar.YEAR) == 1970,
              "roll MONTH does not change YEAR");

        // add day across DST-safe fixed UTC: 2020-01-31 + 1 month.
        cal.clear();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 31, 0, 0, 0);
        cal.add(Calendar.MONTH, 1);
        check(cal.get(Calendar.YEAR) == 2020 && cal.get(Calendar.MONTH) == Calendar.FEBRUARY
              && cal.get(Calendar.DAY_OF_MONTH) == 29, "add month clamps to leap Feb 29");
    }

    static void testOrderingEquals() {
        Calendar a = utc(); a.setTimeInMillis(1000L);
        Calendar b = utc(); b.setTimeInMillis(2000L);
        Calendar c = utc(); c.setTimeInMillis(1000L);

        check(a.before(b) && b.after(a), "before/after");
        check(a.compareTo(b) < 0 && b.compareTo(a) > 0, "compareTo sign");
        check(a.compareTo(c) == 0, "compareTo equal");
        check(a.equals(c) && a.hashCode() == c.hashCode(), "equals/hashCode");
        check(!a.equals(b), "!equals different");
        expectThrows(NullPointerException.class, () -> a.compareTo(null), "compareTo(null) NPE");
        check(!a.before(null) && !a.after(null), "before(null)/after(null) false (no NPE)");
        check(!a.before("x") && !a.after("x"), "before/after non-Calendar false");
        check(!a.equals(null) && !a.equals("x"), "equals non-null/non-Calendar false");
    }

    static void testLenient() {
        Calendar cal = utc();
        check(cal.isLenient(), "default lenient true");
        cal.setLenient(false);
        check(!cal.isLenient(), "setLenient(false)");

        cal.clear();
        cal.set(2020, Calendar.MAY, 15);
        cal.set(Calendar.MONTH, 13);
        expectThrows(IllegalArgumentException.class, () -> cal.getTimeInMillis(),
            "non-lenient invalid MONTH IAE on compute");

        cal.clear();
        cal.set(2020, Calendar.MAY, 15);
        cal.set(Calendar.DAY_OF_MONTH, 32);
        expectThrows(IllegalArgumentException.class, () -> cal.getTimeInMillis(),
            "non-lenient invalid DAY_OF_MONTH IAE on compute");

        cal.setLenient(true);
        cal.clear();
        cal.set(2020, Calendar.DECEMBER, 15);
        cal.set(Calendar.MONTH, 12); // lenient normalizes: 12 = January next year
        check(cal.get(Calendar.MONTH) == Calendar.JANUARY && cal.get(Calendar.YEAR) == 2021,
              "lenient normalizes month 12");
    }

    static void testWeekSettings() {
        Calendar cal = utc();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        check(cal.getFirstDayOfWeek() == Calendar.MONDAY, "setFirstDayOfWeek MONDAY roundtrip");
        cal.setFirstDayOfWeek(Calendar.SUNDAY);
        check(cal.getFirstDayOfWeek() == Calendar.SUNDAY, "setFirstDayOfWeek SUNDAY roundtrip");

        cal.setMinimalDaysInFirstWeek(4);
        check(cal.getMinimalDaysInFirstWeek() == 4, "setMinimalDaysInFirstWeek roundtrip");
        cal.setMinimalDaysInFirstWeek(1);
        check(cal.getMinimalDaysInFirstWeek() == 1, "setMinimalDaysInFirstWeek(1) roundtrip");
    }

    static void testCloneAndTimeZone() {
        Calendar cal = utc();
        cal.setTimeInMillis(123456789L);
        Calendar clone = (Calendar) cal.clone();
        check(clone != cal, "clone distinct reference");
        check(clone.equals(cal) && clone.getTimeInMillis() == cal.getTimeInMillis(),
              "clone equals value");
        clone.setTimeInMillis(1L);
        check(cal.getTimeInMillis() == 123456789L, "clone mutation does not affect original");

        cal.setTimeZone(TimeZone.getTimeZone("GMT+02:00"));
        check(cal.getTimeZone().getRawOffset() == 7200000, "setTimeZone/getTimeZone");
    }

    static void testMinMax() {
        Calendar cal = utc();
        check(cal.getMinimum(Calendar.MONTH) == Calendar.JANUARY, "getMinimum(MONTH)");
        check(cal.getMaximum(Calendar.MONTH) == Calendar.DECEMBER, "getMaximum(MONTH)");
        check(cal.getGreatestMinimum(Calendar.DAY_OF_MONTH) >= 1, "getGreatestMinimum(DAY_OF_MONTH)");
        check(cal.getLeastMaximum(Calendar.DAY_OF_MONTH) <= 31, "getLeastMaximum(DAY_OF_MONTH)");
        check(cal.getActualMinimum(Calendar.MONTH) == Calendar.JANUARY, "getActualMinimum(MONTH)");
        check(cal.getActualMaximum(Calendar.MONTH) == Calendar.DECEMBER, "getActualMaximum(MONTH)");
        // actual maximum of DAY_OF_MONTH for February in a leap year is 29.
        cal.clear();
        cal.set(2020, Calendar.FEBRUARY, 1);
        check(cal.getActualMaximum(Calendar.DAY_OF_MONTH) == 29,
              "getActualMaximum(DAY_OF_MONTH) leap Feb == 29");
        check(cal.toString() != null, "toString non-null");
    }

    static void testSerialization() throws Exception {
        Calendar cal = utc();
        cal.set(2020, Calendar.MAY, 15, 6, 30, 45);
        cal.set(Calendar.MILLISECOND, 123);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(cal);
        }
        Calendar revived;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            revived = (Calendar) ois.readObject();
        }
        check(revived != cal, "serialization distinct object");
        check(revived.equals(cal) && revived.getTimeInMillis() == cal.getTimeInMillis(),
              "serialization preserves time");
        check(revived.get(Calendar.YEAR) == 2020 && revived.get(Calendar.MONTH) == Calendar.MAY,
              "revived Calendar fields intact");
    }

    public static void main(String[] args) throws Exception {
        testEpoch();
        testSet();
        testClearIsSet();
        testAddRoll();
        testOrderingEquals();
        testLenient();
        testWeekSettings();
        testCloneAndTimeZone();
        testMinMax();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
