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
        testNullUnknownAndId();
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

    static void testNullUnknownAndId() {
        expectThrows(NullPointerException.class, () -> TimeZone.getTimeZone((String) null),
                "getTimeZone(null String) NPE");
        expectThrows(NullPointerException.class, () -> TimeZone.getTimeZone((ZoneId) null),
                "getTimeZone(null ZoneId) NPE");
        check("GMT".equals(TimeZone.getTimeZone("Not/AZone").getID()),
              "unknown id falls back to GMT");

        TimeZone utc = TimeZone.getTimeZone("UTC");
        utc.setID("Custom/ID");
        check("Custom/ID".equals(utc.getID()), "setID/getID");
        expectThrows(NullPointerException.class, () -> utc.setID(null), "setID(null) NPE");

        check(utc.getDisplayName().length() > 0, "getDisplayName() non-empty");
        check(utc.getDisplayName(false, TimeZone.LONG, Locale.US).length() > 0,
              "getDisplayName(daylight, style, locale) non-empty");
        check(!TimeZone.getTimeZone("UTC").inDaylightTime(new java.util.Date(0L)),
              "UTC inDaylightTime(epoch) false");
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


    // ===================== 契约深化：自定义偏移/未知ID/DST 判定/双 getOffset/默认区切换 =====================
    static void testTimeZoneContracts() {
        TimeZone gmt530 = TimeZone.getTimeZone("GMT+05:30");
        check(gmt530.getRawOffset() == 19800000 && "GMT+05:30".equals(gmt530.getID()),
              "custom GMT offset parsed");
        check(TimeZone.getTimeZone("GMT+5").getRawOffset() == 18000000, "single-digit offset");
        TimeZone bogus = TimeZone.getTimeZone("Bogus/Zone");
        check("GMT".equals(bogus.getID()) && bogus.getRawOffset() == 0, "unknown id -> GMT");
        try { TimeZone.getTimeZone((String) null); check(false, "getTimeZone(null) NPE"); }
        catch (NullPointerException npe) { check(true, "getTimeZone(null) NPE"); }
        TimeZone ny = TimeZone.getTimeZone("America/New_York");
        java.util.Calendar jul = java.util.Calendar.getInstance(
              TimeZone.getTimeZone("GMT"), java.util.Locale.US);
        jul.clear(); jul.set(2023, java.util.Calendar.JULY, 1);
        java.util.Calendar jan = java.util.Calendar.getInstance(
              TimeZone.getTimeZone("GMT"), java.util.Locale.US);
        jan.clear(); jan.set(2023, java.util.Calendar.JANUARY, 1);
        check(ny.inDaylightTime(jul.getTime()) && !ny.inDaylightTime(jan.getTime()),
              "NY DST on in July, off in January");
        check(ny.useDaylightTime() && !TimeZone.getTimeZone("GMT").useDaylightTime(),
              "useDaylightTime");
        check(ny.getDSTSavings() == 3600000 && TimeZone.getTimeZone("GMT").getDSTSavings() == 0,
              "DST savings 1h vs 0");
        check(ny.getOffset(1, 2023, java.util.Calendar.JULY, 15, java.util.Calendar.SATURDAY, 0)
              == -14400000, "era-form getOffset EDT -4h");
        check(ny.getOffset(123456789L) == -18000000, "millis-form getOffset EST -5h");
        check(TimeZone.getTimeZone("GMT").hasSameRules(TimeZone.getTimeZone("UTC")),
              "GMT/UTC same rules");
        TimeZone old = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT+02:00"));
            check("GMT+02:00".equals(TimeZone.getDefault().getID()), "setDefault/getDefault");
        } finally {
            TimeZone.setDefault(old);
        }
        check("America/New_York".equals(TimeZone.getTimeZone("America/New_York").toZoneId().getId()),
              "toZoneId round-trip");
        check(TimeZone.getTimeZone("GMT").clone() instanceof TimeZone, "clone returns TimeZone");
        check(TimeZone.getAvailableIDs().length > 0
              && TimeZone.getAvailableIDs(3600000).length > 0,
              "available ids overall and by offset");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testTimeZoneContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
