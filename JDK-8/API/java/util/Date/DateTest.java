/*
 * @test
 * @summary java.util.Date API conformance: getTime/setTime, constructor(long), equals,
 *          compareTo, before, and after.
 * @run main DateTest
 */

import java.util.Date;

public class DateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDate() {
        long t = 1000L * 60 * 60 * 24; // 1 day in milliseconds
        Date d = new Date(t);
        check(d.getTime() == t, "constructor(long)/getTime");
        Date d2 = new Date();
        d2.setTime(t);
        check(d2.getTime() == t && d.equals(d2), "setTime/equals");
        check(d.compareTo(new Date(t)) == 0, "compareTo equal");
        check(d.before(new Date(t + 1000)), "before");
        check(d.after(new Date(t - 1000)), "after");
        check(!d.before(new Date(t)) && !d.after(new Date(t)), "neither before nor after equal");
    }

    public static void main(String[] args) {
        testDate();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
