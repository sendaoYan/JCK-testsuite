/*
 * @test
 * @summary java.time.OffsetTime API conformance: time accessors and offset.
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
        OffsetTime ot = OffsetTime.of(10, 30, 15, 0, ZoneOffset.ofHours(2));
        check(ot.getHour() == 10, "hour");
        check(ot.getMinute() == 30, "minute");
        check(ot.getSecond() == 15, "second");
        check(ot.getOffset().equals(ZoneOffset.ofHours(2)), "offset");
    }

    public static void main(String[] args) {
        testOffsetTime();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
