/*
 * @test
 * @summary java.util.Date conformance: white-box structure plus black-box behavior of
 *          constructor(long), getTime/setTime, equals/hashCode, compareTo, before/after,
 *          clone, UTC/parse, and Instant round-trip.
 * @run main DateTest
 */

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

public class DateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void assertThrows(Class<? extends Throwable> exType, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (exType.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Date");
        check(!c.isInterface(), "Date is a class, not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "Date is concrete");
        check(c.getSuperclass() == Object.class, "Date extends Object");
        check(Serializable.class.isAssignableFrom(c), "Date implements Serializable");
        check(Cloneable.class.isAssignableFrom(c), "Date implements Cloneable");
        check(Comparable.class.isAssignableFrom(c), "Date implements Comparable");
        c.getMethod("getTime");
        c.getMethod("setTime", long.class);
        c.getMethod("before", Date.class);
        c.getMethod("after", Date.class);
        c.getMethod("compareTo", Date.class);
        c.getMethod("toInstant");
        c.getMethod("from", Instant.class);
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testEpochAndTime();
        testOrdering();
        testClone();
        testUtcAndInstant();
        testParse();
    }

    static void testEpochAndTime() {
        long t = 1000L * 60 * 60 * 24; // 1 day in milliseconds
        Date d = new Date(t);
        check(d.getTime() == t, "constructor(long)/getTime");
        check(new Date(0).getTime() == 0L, "epoch constructor == 0");

        Date d2 = new Date();
        d2.setTime(t);
        check(d2.getTime() == t && d.equals(d2), "setTime/equals");
        check(d.hashCode() == d2.hashCode(), "equal Dates have equal hashCode");

        check(d.compareTo(new Date(t)) == 0, "compareTo equal");
        check(d.compareTo(new Date(t - 1)) > 0, "compareTo later > 0");
        check(d.compareTo(new Date(t + 1)) < 0, "compareTo earlier < 0");

        check(d.before(new Date(t + 1000)), "before");
        check(d.after(new Date(t - 1000)), "after");
        check(!d.before(new Date(t)) && !d.after(new Date(t)), "neither before nor after equal");
        check(!d.equals("not a date"), "equals false for non-Date");
    }

    static void testOrdering() {
        Date a = new Date(500L);
        Date b = new Date(1000L);
        Date c = new Date(1500L);
        check(a.before(b) && b.before(c), "transitive before chain");
        check(c.after(b) && b.after(a), "transitive after chain");
    }

    static void testClone() {
        Date d = new Date(12345L);
        Date clone = (Date) d.clone();
        check(clone != d, "clone returns distinct object");
        check(clone.equals(d) && clone.getTime() == 12345L, "clone equals original value");
    }

    static void testUtcAndInstant() {
        check(Date.UTC(70, 0, 1, 0, 0, 0) == 0L, "Date.UTC(1970-01-01) == 0");
        check(Date.UTC(70, 0, 1, 1, 0, 0) == 3600000L, "Date.UTC(1970-01-01T01:00) == 1 hour");
        Date d = new Date(0);
        Instant inst = d.toInstant();
        check(inst.equals(Instant.EPOCH), "toInstant of epoch == Instant.EPOCH");
        check(Date.from(inst).equals(d), "from(toInstant) round-trip");
    }

    static void testParse() {
        assertThrows(IllegalArgumentException.class,
                () -> Date.parse("not a date"), "parse(invalid) throws IllegalArgumentException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
