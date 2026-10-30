/*
 * @test
 * @summary java.time.LocalTime API conformance: of/getters, plusHours, isBefore/isAfter,
 *          and parse.
 * @run main LocalTimeTest
 */

import java.time.LocalTime;

public class LocalTimeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLocalTime() {
        LocalTime t = LocalTime.of(13, 30, 45);
        check(t.getHour() == 13 && t.getMinute() == 30 && t.getSecond() == 45, "of/getters");
        check(t.plusHours(1).equals(LocalTime.of(14, 30, 45)), "plusHours");
        check(t.isBefore(LocalTime.of(14, 0)) && t.isAfter(LocalTime.of(13, 0)),
              "isBefore/isAfter");
        check(LocalTime.parse("13:30:45").equals(t), "parse");
    }

    public static void main(String[] args) {
        testLocalTime();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
