/*
 * @test
 * @summary java.time.OffsetDateTime API conformance: of/getters, getOffset, toInstant, and
 *          plusDays.
 * @run main OffsetDateTimeTest
 */

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalQueries;

public class OffsetDateTimeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testOffsetDateTime() {
        OffsetDateTime odt = OffsetDateTime.of(2024, 3, 1, 10, 30, 0, 0, ZoneOffset.ofHours(2));
        check(odt.getYear() == 2024 && odt.getHour() == 10, "of/getters");
        check(odt.getOffset().equals(ZoneOffset.ofHours(2)), "getOffset");
        check(odt.toInstant().equals(Instant.parse("2024-03-01T08:30:00Z")), "toInstant");
        check(odt.plusDays(1).getDayOfMonth() == 2, "plusDays");
    }

    public static void main(String[] args) {
        testOffsetDateTime();
        testDeepOffsetDateTime();
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


    static void testDeepOffsetDateTime() {
        OffsetDateTime odt = OffsetDateTime.of(2024, 2, 29, 10, 15, 30, 0, ZoneOffset.ofHours(1));
        xeq(odt.toString(), "2024-02-29T10:15:30+01:00", "toString");
        xeq(odt.toLocalDate().toString(), "2024-02-29", "toLocalDate");
        xeq(odt.toLocalTime().toString(), "10:15:30", "toLocalTime");
        xeq(odt.toLocalDateTime().toString(), "2024-02-29T10:15:30", "toLocalDateTime");
        xeq(odt.toOffsetTime().toString(), "10:15:30+01:00", "toOffsetTime");
        xeq(odt.toInstant().toString(), "2024-02-29T09:15:30Z", "toInstant");
        check(odt.toEpochSecond() == 1709198130L, "toEpochSecond");
        xeq(odt.toZonedDateTime().toString(), "2024-02-29T10:15:30+01:00", "toZonedDateTime");
        xeq(odt.atZoneSameInstant(ZoneId.of("Europe/Berlin")).toString(), "2024-02-29T10:15:30+01:00[Europe/Berlin]", "atZoneSameInstant");
        xeq(ZonedDateTime.ofLocal(odt.toLocalDateTime(), ZoneId.of("America/New_York"), odt.getOffset()).toString(),
            "2024-02-29T10:15:30-05:00[America/New_York]", "ofLocal same wall time NY");
        xeq(odt.withOffsetSameInstant(ZoneOffset.ofHours(3)).toString(), "2024-02-29T12:15:30+03:00", "withOffsetSameInstant");
        xeq(odt.withOffsetSameLocal(ZoneOffset.ofHours(3)).toString(), "2024-02-29T10:15:30+03:00", "withOffsetSameLocal");
        check(odt.getOffset().equals(ZoneOffset.ofHours(1)), "getOffset");
        check(odt.getLong(ChronoField.OFFSET_SECONDS) == 3600L, "getLong offset");
        check(odt.getLong(ChronoField.INSTANT_SECONDS) == 1709198130L, "getLong instant");
        xeq(odt.truncatedTo(ChronoUnit.DAYS).toString(), "2024-02-29T00:00+01:00", "trunc DAYS");
        xeq(odt.truncatedTo(ChronoUnit.SECONDS).toString(), "2024-02-29T10:15:30+01:00", "trunc SECONDS");
        OffsetDateTime sameInstantOtherZone = OffsetDateTime.of(2024, 2, 29, 4, 15, 30, 0, ZoneOffset.ofHours(-5));
        check(odt.isEqual(sameInstantOtherZone), "isEqual across offsets");
        check(!odt.equals(sameInstantOtherZone), "equals false across offsets");
        check(odt.compareTo(sameInstantOtherZone) > 0, "compareTo orders by local time even when instant equal-or-later");
        check(sameInstantOtherZone.compareTo(odt) < 0, "compareTo mirror by local time");
        check(OffsetDateTime.of(2024, 2, 29, 10, 15, 30, 0, ZoneOffset.ofHours(-5)).compareTo(odt) > 0, "compareTo by instant: -05 wall equals +01 wall but is 6h later");
        check(OffsetDateTime.parse("2024-02-29T10:15:30+01:00").equals(odt), "parse rt");
        check(odt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME).equals("2024-02-29T10:15:30+01:00"), "ISO_OFFSET_DATE_TIME");
        check(odt.format(DateTimeFormatter.ISO_INSTANT).equals("2024-02-29T09:15:30Z"), "ISO_INSTANT converts");
        xact(() -> OffsetDateTime.of(2024, 2, 30, 0, 0, 0, 0, ZoneOffset.UTC), DateTimeException.class,
            "Invalid date 'FEBRUARY 30'", "invalid date");
        xact(() -> OffsetDateTime.of(2024, 2, 29, 0, 0, 0, 0, null), NullPointerException.class, "offset", "null offset");
        xact(() -> OffsetDateTime.ofInstant(null, ZoneOffset.UTC), NullPointerException.class, null, "ofInstant null");
        xact(() -> OffsetDateTime.ofInstant(Instant.EPOCH, null), NullPointerException.class, "zone", "ofInstant null zone");
        check(odt.plusDays(1).minusDays(1).equals(odt), "days sym");
        check(odt.plusHours(2).minusHours(2).equals(odt), "hours sym");
        check(odt.plusNanos(5).minusNanos(5).equals(odt), "nanos sym");
        check(odt.plus(Period.ofMonths(1)).toString().equals("2024-03-29T10:15:30+01:00"), "plusMonths");
        check(odt.plus(Duration.ofHours(24)).toString().equals("2024-03-01T10:15:30+01:00"), "plus 24h duration");
        check(odt.query(TemporalQueries.offset()).equals(ZoneOffset.ofHours(1)), "query offset");
        check(odt.query(TemporalQueries.precision()) == ChronoUnit.NANOS, "precision");
        check(odt.isSupported(ChronoField.DAY_OF_YEAR), "support DOY");
        check(odt.isSupported(ChronoField.INSTANT_SECONDS), "support INSTANT");
        xeq(OffsetDateTime.from(ZonedDateTime.of(2024, 2, 29, 10, 15, 30, 0, ZoneId.of("Europe/Berlin"))).toString(),
            "2024-02-29T10:15:30+01:00", "from ZDT");
    }

}
