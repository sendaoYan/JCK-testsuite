/*
 * @test
 * @summary java.util.Date API conformance (Java 8 surface): complete contract coverage of
 *          constructors (long + deprecated field constructors), getTime/setTime (incl. extremes),
 *          before/after/equals/hashCode/compareTo (+ null NPE), clone independence,
 *          from/toInstant round-trip (+ null NPE), UTC (fixed epoch), deprecated field
 *          getter/setter round-trips (TZ-independent), and Serializable round-trip.
 * @run main DateTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Instant;
import java.util.Date;

public class DateTest {
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

    static void testConstructorsAndTime() {
        long t = 1000L * 60 * 60 * 24; // 1 day in milliseconds
        Date d = new Date(t);
        check(d.getTime() == t, "constructor(long)/getTime");
        check(new Date(0).getTime() == 0L, "epoch constructor == 0");
        check(new Date(-1).getTime() == -1L, "constructor(-1)");
        check(new Date(Long.MAX_VALUE).getTime() == Long.MAX_VALUE, "constructor(MAX_VALUE)");
        check(new Date(Long.MIN_VALUE).getTime() == Long.MIN_VALUE, "constructor(MIN_VALUE)");
        check(new Date().getTime() >= 0, "no-arg constructor non-negative");

        Date d2 = new Date();
        d2.setTime(t);
        check(d2.getTime() == t && d.equals(d2), "setTime/equals");
        d2.setTime(Long.MIN_VALUE);
        check(d2.getTime() == Long.MIN_VALUE, "setTime(MIN_VALUE) roundtrip");
        d2.setTime(Long.MAX_VALUE);
        check(d2.getTime() == Long.MAX_VALUE, "setTime(MAX_VALUE) roundtrip");

        // deprecated field constructor round-trips in the local time zone.
        Date field = new Date(120, 5, 15); // 2020-06-15 00:00:00 local
        check(field.getYear() == 120, "deprecated constructor getYear");
        check(field.getMonth() == 5, "deprecated constructor getMonth");
        check(field.getDate() == 15, "deprecated constructor getDate");
    }

    static void testOrdering() {
        long t = 1000L * 60 * 60 * 24;
        Date d = new Date(t);
        check(d.compareTo(new Date(t)) == 0, "compareTo equal");
        check(d.compareTo(new Date(t - 1)) > 0, "compareTo later > 0");
        check(d.compareTo(new Date(t + 1)) < 0, "compareTo earlier < 0");
        check(d.compareTo(d) == 0, "compareTo self == 0");

        check(d.before(new Date(t + 1000)), "before");
        check(d.after(new Date(t - 1000)), "after");
        check(!d.before(new Date(t)) && !d.after(new Date(t)), "neither before nor after equal");
        expectThrows(NullPointerException.class, () -> d.before(null), "before(null) NPE");
        expectThrows(NullPointerException.class, () -> d.after(null), "after(null) NPE");
        expectThrows(NullPointerException.class, () -> d.compareTo(null), "compareTo(null) NPE");

        check(d.hashCode() == new Date(t).hashCode(), "equal Dates equal hashCode");
        check(d.equals(d) && !d.equals(null) && !d.equals("not a date"),
              "equals reflexive / non-null / non-Date");

        // transitivity.
        Date a = new Date(500L), b = new Date(1000L), c = new Date(1500L);
        check(a.before(b) && b.before(c) && a.before(c), "before transitivity");
        check(c.after(b) && b.after(a) && c.after(a), "after transitivity");
    }

    static void testClone() {
        Date d = new Date(12345L);
        Date clone = (Date) d.clone();
        check(clone != d, "clone distinct reference");
        check(clone.equals(d) && clone.getTime() == 12345L, "clone equals value");
        clone.setTime(99999L);
        check(d.getTime() == 12345L, "clone mutation does not affect original");
    }

    static void testInstant() {
        Date d = new Date(123456789L);
        check(d.toInstant().equals(Instant.ofEpochMilli(123456789L)), "toInstant");
        check(Date.from(Instant.ofEpochMilli(123456789L)).equals(d), "from(toInstant) roundtrip");
        check(Date.from(Instant.EPOCH).getTime() == 0L, "from(EPOCH)");
        check(new Date(Long.MIN_VALUE).toInstant().equals(Instant.ofEpochMilli(Long.MIN_VALUE)),
              "toInstant(MIN_VALUE)");
        check(new Date(Long.MAX_VALUE).toInstant().equals(Instant.ofEpochMilli(Long.MAX_VALUE)),
              "toInstant(MAX_VALUE)");
        expectThrows(NullPointerException.class, () -> Date.from(null), "from(null) NPE");
    }

    static void testUtc() {
        check(Date.UTC(70, 0, 1, 0, 0, 0) == 0L, "UTC(1970-01-01) == 0");
        check(Date.UTC(70, 0, 1, 1, 0, 0) == 3600000L, "UTC(1970-01-01T01:00) == 1 hour");
        check(Date.UTC(70, 0, 1, 0, 0, 1) == 1000L, "UTC(1970-01-01T00:00:01) == 1 second");
    }

    static void testDeprecatedFieldRoundtrip() {
        Date d = new Date(0);
        d.setHours(5);
        check(d.getHours() == 5, "setHours/getHours roundtrip");
        d.setMinutes(15);
        check(d.getMinutes() == 15, "setMinutes/getMinutes roundtrip");
        d.setSeconds(30);
        check(d.getSeconds() == 30, "setSeconds/getSeconds roundtrip");
        d.setYear(120);
        check(d.getYear() == 120, "setYear/getYear roundtrip");
        d.setMonth(5);
        check(d.getMonth() == 5, "setMonth/getMonth roundtrip");
        d.setDate(15);
        check(d.getDate() == 15, "setDate/getDate roundtrip");
        int day = d.getDay();
        check(day >= 0 && day <= 6, "getDay in [0,6]");
    }

    static void testSerialization() throws Exception {
        Date d = new Date(9876543210L);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(d);
        }
        byte[] bytes = bos.toByteArray();
        Date revived;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            revived = (Date) ois.readObject();
        }
        check(revived != d, "serialization roundtrip distinct object");
        check(revived.equals(d) && revived.getTime() == d.getTime(),
              "serialization roundtrip preserves time");
        // revived object remains operable.
        check(revived.after(new Date(0)), "revived Date still operable");

        Date min = new Date(Long.MIN_VALUE);
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos2)) {
            oos.writeObject(min);
        }
        Date revivedMin;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos2.toByteArray()))) {
            revivedMin = (Date) ois.readObject();
        }
        check(revivedMin.getTime() == Long.MIN_VALUE, "serialization MIN_VALUE roundtrip");
    }


    // ===================== 契约深化：hashCode 公式/排序矩阵/parse/UTC 静态 =====================
    static void testDateContracts() {
        long t = 123456789L;
        Date d = new Date(t);
        check(d.hashCode() == (int) (t ^ (t >>> 32)), "hashCode == (int)(ms ^ (ms>>>32))");
        Date later = new Date(t + 1);
        check(d.compareTo(later) < 0 && later.compareTo(d) > 0, "compareTo signs both directions");
        check(d.compareTo(new Date(t)) == 0, "compareTo equal times -> 0");
        check(!d.equals(later) && d.before(later) && later.after(d), "before/after/equals");
        Object cl = d.clone();
        check(cl != d && cl instanceof Date && cl.equals(d), "clone distinct but equal");
        check(Date.parse("Jan 1, 2000") > 0, "Date.parse accepts 'Jan 1, 2000'");
        check(Date.parse("1 January 1970 00:00:00 GMT") == 0, "Date.parse epoch GMT == 0");
        try { Date.from(null); check(false, "Date.from(null) NPE"); }
        catch (NullPointerException npe) { check(true, "Date.from(null) NPE"); }
        check(Date.from(d.toInstant()).getTime() == t, "toInstant/Date.from round-trip");
        @SuppressWarnings("deprecation")
        long utc = Date.UTC(2020 - 1900, 5, 15, 10, 30, 45);
        java.util.Calendar g = java.util.Calendar.getInstance(
              java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US);
        g.clear(); g.set(2020, 5, 15, 10, 30, 45);
        check(utc == g.getTimeInMillis(), "Date.UTC matches GMT calendar millis");
        Date mut = new Date(0L);
        mut.setTime(t);
        check(mut.getTime() == t, "setTime/getTime");
    }

    public static void main(String[] args) throws Exception {
        testConstructorsAndTime();
        testOrdering();
        testClone();
        testInstant();
        testUtc();
        testDeprecatedFieldRoundtrip();
        testSerialization();
        testDateContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
