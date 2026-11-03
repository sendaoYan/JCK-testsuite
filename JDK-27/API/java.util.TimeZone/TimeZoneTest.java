/*
 * @test
 * @summary java.util.TimeZone conformance: white-box structure plus black-box behavior of
 *          UTC/GMT raw offsets, fixed-offset zones, getOffset for a fixed instant,
 *          hasSameRules, getAvailableIDs, and ZoneId round-trip.
 * @run main TimeZoneTest
 */

import java.io.Serializable;
import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;

public class TimeZoneTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.TimeZone");
        check(!c.isInterface(), "TimeZone is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "TimeZone is abstract");
        check(Serializable.class.isAssignableFrom(c), "TimeZone implements Serializable");
        check(Cloneable.class.isAssignableFrom(c), "TimeZone implements Cloneable");
        c.getMethod("getRawOffset");
        c.getMethod("setRawOffset", int.class);
        c.getMethod("getOffset", long.class);
        c.getMethod("getID");
        c.getMethod("getTimeZone", String.class);
        c.getMethod("getTimeZone", ZoneId.class);
        c.getMethod("hasSameRules", TimeZone.class);
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testUtcGmt();
        testFixedOffsets();
        testAvailableIds();
        testZoneIdRoundTrip();
    }

    static void testUtcGmt() {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        check(utc.getRawOffset() == 0, "UTC raw offset 0");
        check("UTC".equals(utc.getID()), "UTC id");
        check("GMT".equals(TimeZone.getTimeZone("GMT").getID()), "GMT id");
        check(utc.hasSameRules(TimeZone.getTimeZone("GMT")), "hasSameRules GMT");
        // getOffset for a fixed instant: UTC has offset 0 year-round.
        check(utc.getOffset(0L) == 0, "UTC getOffset(epoch) == 0");
    }

    static void testFixedOffsets() {
        TimeZone plus5 = TimeZone.getTimeZone("GMT+05:00");
        check(plus5.getRawOffset() == 5 * 3600000, "GMT+05:00 raw offset");
        TimeZone minus5 = TimeZone.getTimeZone("GMT-05:00");
        check(minus5.getRawOffset() == -5 * 3600000, "GMT-05:00 raw offset");
        check(plus5.getOffset(0L) == 5 * 3600000, "GMT+05:00 getOffset(epoch)");
    }

    static void testAvailableIds() {
        check(TimeZone.getAvailableIDs().length > 0, "getAvailableIDs non-empty");
        String[] zero = TimeZone.getAvailableIDs(0);
        check(zero.length > 0, "getAvailableIDs(0) non-empty");
        boolean hasGmt = false;
        for (String id : zero) {
            if ("GMT".equals(id)) { hasGmt = true; break; }
        }
        check(hasGmt, "getAvailableIDs(0) contains GMT");
    }

    static void testZoneIdRoundTrip() {
        TimeZone tz = TimeZone.getTimeZone(ZoneId.of("UTC"));
        check(tz.hasSameRules(TimeZone.getTimeZone("UTC")), "getTimeZone(ZoneId UTC)");
        check(tz.toZoneId().equals(ZoneId.of("UTC")), "toZoneId round-trip");
        // display name with a fixed locale is deterministic and non-empty.
        check(TimeZone.getTimeZone("UTC").getDisplayName(Locale.US).length() > 0,
              "getDisplayName non-empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
