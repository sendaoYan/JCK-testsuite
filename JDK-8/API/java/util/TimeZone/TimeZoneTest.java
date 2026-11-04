/*
 * @test
 * @summary java.util.TimeZone API conformance: UTC raw offset, GMT id, hasSameRules, and
 *          getAvailableIDs.
 * @run main TimeZoneTest
 */

import java.util.TimeZone;

public class TimeZoneTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTimeZone() {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        check(utc.getRawOffset() == 0, "UTC raw offset 0");
        check("GMT".equals(TimeZone.getTimeZone("GMT").getID()), "GMT id");
        check(utc.hasSameRules(TimeZone.getTimeZone("GMT")), "hasSameRules GMT");
        check(TimeZone.getAvailableIDs().length > 0, "getAvailableIDs non-empty");
    }

    public static void main(String[] args) {
        testTimeZone();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
