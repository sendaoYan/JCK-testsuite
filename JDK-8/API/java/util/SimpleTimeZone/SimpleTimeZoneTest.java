/*
 * @test
 * @summary java.util.SimpleTimeZone API conformance: raw offset and daylight time.
 * @run main SimpleTimeZoneTest
 */

import java.util.SimpleTimeZone;

public class SimpleTimeZoneTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSimpleTimeZone() {
        SimpleTimeZone tz = new SimpleTimeZone(3600000, "GMT+01:00");
        check(tz.getRawOffset() == 3600000, "getRawOffset");
        check("GMT+01:00".equals(tz.getID()), "getID");
        check(!tz.useDaylightTime(), "useDaylightTime false without DST rules");

        SimpleTimeZone dst = new SimpleTimeZone(0, "GMT");
        dst.setStartRule(3, 1, 0, 3600000);
        dst.setEndRule(10, 1, 0, 3600000);
        check(dst.useDaylightTime(), "useDaylightTime true after setStartRule/setEndRule");
    }

    public static void main(String[] args) {
        testSimpleTimeZone();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
