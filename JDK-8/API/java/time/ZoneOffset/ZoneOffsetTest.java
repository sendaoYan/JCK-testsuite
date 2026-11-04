/*
 * @test
 * @summary java.time.ZoneOffset API conformance: ofHours/getTotalSeconds, UTC id, and
 *          of(String).
 * @run main ZoneOffsetTest
 */

import java.time.ZoneOffset;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.time.temporal.UnsupportedTemporalTypeException;

public class ZoneOffsetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testZoneOffset() {
        check(ZoneOffset.ofHours(5).getTotalSeconds() == 5 * 3600, "ofHours/getTotalSeconds");
        check("Z".equals(ZoneOffset.UTC.getId()), "UTC id");
        check(ZoneOffset.of("+05:00").getTotalSeconds() == 18000, "of(String)");
    }

    public static void main(String[] args) {
        testZoneOffset();
        testDeepZoneOffset();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== contract deepening harness (appended) ====
    interface DeepRun { void run() throws Throwable; }

    static void xact(DeepRun r, Class<?> type, String exactMsg, String tag) {
        try {
            r.run();
            throw new AssertionError(tag + ": expected " + type.getSimpleName() + " but nothing was thrown");
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(tag + ": expected " + type.getSimpleName() + " but got "
                        + t.getClass().getName() + ": " + t.getMessage(), t);
            if (exactMsg != null && !exactMsg.equals(t.getMessage()))
                throw new AssertionError(tag + ": " + type.getSimpleName() + " message <" + t.getMessage()
                        + "> want <" + exactMsg + ">");
            passed++;
        }
    }

    static void xeq(Object got, Object want, String tag) {
        if (got == null ? want != null : !got.equals(want))
            throw new AssertionError(tag + ": got <" + got + "> want <" + want + ">");
        passed++;
    }


    static void testDeepZoneOffset() {
        check(ZoneOffset.UTC == ZoneOffset.of("+00:00") && ZoneOffset.UTC.getId().equals("Z"), "UTC==of(+00:00), id Z");
        xeq(ZoneOffset.of("Z").toString(), "Z", "of Z");
        xeq(ZoneOffset.of("+01:00").toString(), "+01:00", "of +01:00");
        xeq(ZoneOffset.of("-0530").toString(), "-05:30", "of -0530 normalized");
        xeq(ZoneOffset.of("+010105").toString(), "+01:01:05", "of +010105");
        check(ZoneOffset.of("+010105").getTotalSeconds() == 3665, "totalSeconds 3665");
        check(ZoneOffset.of("-0530").getTotalSeconds() == -19800, "totalSeconds -19800");
        xeq(ZoneOffset.ofHours(1).toString(), "+01:00", "ofHours");
        xeq(ZoneOffset.ofHoursMinutes(-1, -30).toString(), "-01:30", "ofHoursMinutes neg");
        xeq(ZoneOffset.ofHoursMinutesSeconds(0, 0, 1).toString(), "+00:00:01", "ofHMS seconds");
        xeq(ZoneOffset.ofTotalSeconds(3665).toString(), "+01:01:05", "ofTotalSeconds");
        xeq(ZoneOffset.ofTotalSeconds(-1).toString(), "-00:00:01", "ofTotalSeconds -1");
        xeq(ZoneOffset.MAX.toString(), "+18:00", "MAX");
        xeq(ZoneOffset.MIN.toString(), "-18:00", "MIN");
        check(ZoneOffset.MAX.getTotalSeconds() == 64800, "MAX secs");
        check(ZoneOffset.MIN.getTotalSeconds() == -64800, "MIN secs");
        xact(() -> ZoneOffset.of("+19:00"), DateTimeException.class,
            "Zone offset hours not in valid range: value 19 is not in the range -18 to 18", "of +19");
        xact(() -> ZoneOffset.of("+18:00:01"), DateTimeException.class,
            "Zone offset not in valid range: -18:00 to +18:00", "of +18:00:01");
        xact(() -> ZoneOffset.of("ABC"), DateTimeException.class,
            "Invalid ID for ZoneOffset, non numeric characters found: ABC", "of ABC");
        xact(() -> ZoneOffset.of(""), DateTimeException.class,
            "Invalid ID for ZoneOffset, invalid format: ", "of empty");
        xact(() -> ZoneOffset.of(null), NullPointerException.class, null, "of null");
        xact(() -> ZoneOffset.ofTotalSeconds(64801), DateTimeException.class,
            "Zone offset not in valid range: -18:00 to +18:00", "ofTotalSeconds 64801");
        xact(() -> ZoneOffset.ofTotalSeconds(-64801), DateTimeException.class,
            "Zone offset not in valid range: -18:00 to +18:00", "ofTotalSeconds -64801");
        xact(() -> ZoneOffset.ofHours(19), DateTimeException.class,
            "Zone offset hours not in valid range: value 19 is not in the range -18 to 18", "ofHours 19");
        xact(() -> ZoneOffset.ofHoursMinutes(19, 0), DateTimeException.class,
            "Zone offset hours not in valid range: value 19 is not in the range -18 to 18", "ofHM 19h");
        xact(() -> ZoneOffset.ofHoursMinutes(0, -60), DateTimeException.class,
            "Zone offset minutes not in valid range: value -60 is not in the range -59 to 59", "ofHM -60m");
        xact(() -> ZoneOffset.ofHoursMinutesSeconds(0, 0, 60), DateTimeException.class,
            "Zone offset seconds not in valid range: value 60 is not in the range -59 to 59", "ofHMS 60s");
        ZoneOffset z = ZoneOffset.ofHours(2);
        check(z.get(ChronoField.OFFSET_SECONDS) == 7200, "get OFFSET_SECONDS");
        check(z.getLong(ChronoField.OFFSET_SECONDS) == 7200L, "getLong");
        xeq(z.range(ChronoField.OFFSET_SECONDS).toString(), "-64800 - 64800", "range");
        check(z.query(TemporalQueries.offset()) == z, "query offset self");
        check(z.query(TemporalQueries.zoneId()) == null, "query zoneId null for offset");
        check(z.isSupported(ChronoField.OFFSET_SECONDS), "supported");
        check(!z.isSupported(ChronoField.YEAR), "not supported YEAR");
        xeq(z.adjustInto(OffsetTime.of(LocalTime.of(10, 0), ZoneOffset.UTC)).toString(), "10:00+02:00", "adjustInto OffsetTime");
        xact(() -> z.adjustInto(LocalTime.of(10, 0)), UnsupportedTemporalTypeException.class,
            "Unsupported field: OffsetSeconds", "adjustInto LocalTime throws");
        check(ZoneOffset.of("+01:00").hashCode() != ZoneOffset.of("+02:00").hashCode(), "hash distinct");
        check(ZoneOffset.of("+01:00").equals(ZoneOffset.ofTotalSeconds(3600)), "equals cross factory");
        check(ZoneOffset.of("+01:00").compareTo(ZoneOffset.of("+02:00")) > 0, "compareTo is descending (larger offset sorts first)");
        check(ZoneOffset.UTC.compareTo(ZoneOffset.MIN) == -64800, "compareTo returns raw seconds difference MIN");
        check(ZoneOffset.ofHours(1).compareTo(ZoneOffset.ofHours(2)) == 3600, "compareTo returns 3600 for 1h vs 2h");
        check(ZoneOffset.ofHours(1).compareTo(ZoneOffset.ofHours(1)) == 0, "compareTo equal 0");
        xeq(ZoneOffset.from(OffsetTime.of(LocalTime.of(0, 0), ZoneOffset.ofHours(5))).toString(), "+05:00", "from");
        xact(() -> ZoneOffset.from(LocalDate.of(2024, 1, 1)), DateTimeException.class,
            "Unable to obtain ZoneOffset from TemporalAccessor: 2024-01-01 of type java.time.LocalDate", "from LocalDate");
    }

}
