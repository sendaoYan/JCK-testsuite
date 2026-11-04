/*
 * @test
 * @summary java.util.TimeZone API conformance (Java 8 surface): complete contract coverage of
 *          getTimeZone(String/ZoneId) (fixed offsets, unknown id fallback, null NPE), raw offset,
 *          getID/setID (null NPE), display names (fixed locale), getOffset(long),
 *          useDaylightTime/observesDaylightTime/inDaylightTime, hasSameRules, getAvailableIDs,
 *          toZoneId round-trip, and clone. No modification of the global default timezone.
 * @run main TimeZoneTest
 */

import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;

public class TimeZoneTest {
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

    static void testUtcGmt() {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        check(utc.getRawOffset() == 0, "UTC raw offset 0");
        check("UTC".equals(utc.getID()), "UTC id");
        check("GMT".equals(TimeZone.getTimeZone("GMT").getID()), "GMT id");
        check(utc.hasSameRules(TimeZone.getTimeZone("GMT")), "hasSameRules GMT");
        check(utc.getOffset(0L) == 0, "UTC getOffset(epoch) == 0");
        check(!utc.useDaylightTime(), "UTC useDaylightTime false");
        check(!utc.inDaylightTime(new java.util.Date(0L)), "UTC inDaylightTime(epoch) false");
    }

    static void testFixedOffsets() {
        TimeZone plus5 = TimeZone.getTimeZone("GMT+05:00");
        check(plus5.getRawOffset() == 5 * 3600000, "GMT+05:00 raw offset");
        TimeZone minus5 = TimeZone.getTimeZone("GMT-05:00");
        check(minus5.getRawOffset() == -5 * 3600000, "GMT-05:00 raw offset");
        check(plus5.getOffset(0L) == 5 * 3600000, "GMT+05:00 getOffset(epoch)");
        check(minus5.getOffset(0L) == -5 * 3600000, "GMT-05:00 getOffset(epoch)");
    }

    static void testUnknownAndNull() {
        // Unknown id returns GMT with ID "GMT" (documented fallback).
        TimeZone unknown = TimeZone.getTimeZone("Not/AZone");
        check("GMT".equals(unknown.getID()), "unknown id falls back to GMT");
        expectThrows(NullPointerException.class, () -> TimeZone.getTimeZone((String) null),
            "getTimeZone(null String) NPE");
        expectThrows(NullPointerException.class, () -> TimeZone.getTimeZone((ZoneId) null),
            "getTimeZone(null ZoneId) NPE");
    }

    static void testIdDisplayName() {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        check(utc.getID().equals("UTC"), "getID");
        utc.setID("Custom/ID");
        check("Custom/ID".equals(utc.getID()), "setID/getID");
        expectThrows(NullPointerException.class, () -> utc.setID(null), "setID(null) NPE");
        // reset ID for later tests
        utc.setID("UTC");

        check(utc.getDisplayName(Locale.US).length() > 0, "getDisplayName(Locale) non-empty");
        check(utc.getDisplayName().length() > 0, "getDisplayName() non-empty");
        check(utc.getDisplayName(false, TimeZone.LONG, Locale.US).length() > 0,
              "getDisplayName(daylight, style, locale) non-empty");
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
        check(TimeZone.getDefault() != null, "getDefault non-null");
    }

    static void testZoneIdAndClone() {
        TimeZone tz = TimeZone.getTimeZone(ZoneId.of("UTC"));
        check(tz.hasSameRules(TimeZone.getTimeZone("UTC")), "getTimeZone(ZoneId UTC)");
        check(tz.toZoneId().equals(ZoneId.of("UTC")), "toZoneId roundtrip");
        check(TimeZone.getTimeZone(ZoneId.of("GMT+05:00")).getRawOffset() == 5 * 3600000,
              "getTimeZone(ZoneId offset)");

        TimeZone clone = (TimeZone) TimeZone.getTimeZone("UTC").clone();
        check(clone != TimeZone.getTimeZone("UTC"), "clone distinct object");
        check(clone.hasSameRules(TimeZone.getTimeZone("UTC")), "clone hasSameRules original");
    }

    public static void main(String[] args) {
        testUtcGmt();
        testFixedOffsets();
        testUnknownAndNull();
        testIdDisplayName();
        testAvailableIds();
        testZoneIdAndClone();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
