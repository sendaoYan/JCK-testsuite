/*
 * @test
 * @summary java.time.OffsetTime API conformance: of/getters, getOffset, and plusHours.
 * @run main OffsetTimeTest
 */

import java.time.OffsetTime;
import java.time.ZoneOffset;

public class OffsetTimeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testOffsetTime() {
        OffsetTime ot = OffsetTime.of(10, 30, 0, 0, ZoneOffset.ofHours(2));
        check(ot.getHour() == 10 && ot.getMinute() == 30, "of/getters");
        check(ot.getOffset().equals(ZoneOffset.ofHours(2)), "getOffset");
        check(ot.plusHours(1).getHour() == 11, "plusHours");
    }

    public static void main(String[] args) {
        testOffsetTime();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
