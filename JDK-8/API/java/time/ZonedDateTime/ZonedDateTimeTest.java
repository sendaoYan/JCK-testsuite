/*
 * @test
 * @summary java.time.ZonedDateTime API conformance: full contract coverage.
 *          factories (of/ofLocal/ofInstant/ofStrict/from/parse/now fixed Clock)
 *          + exact exception matrix, null semantics, getters (offset/zone/date/
 *          time parts), withZoneSameInstant/withZoneSameLocal/withFixedOffsetZone/
 *          withEarlierOffsetAtOverlap/withLaterOffsetAtOverlap, with/truncatedTo/
 *          plus/minus with immutability, until, toOffsetDateTime, comparisons,
 *          and serialization round-trip + serialVersionUID. Deterministic: fixed
 *          offsets / UTC zone (no tzdata-sensitive DST assertions).
 * @run main ZonedDateTimeTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalQueries;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.Duration;
import java.time.Period;

public class ZonedDateTimeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static ZonedDateTime roundTrip(ZonedDateTime v) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(v);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (ZonedDateTime) ois.readObject();
    }

    static void testFactories() {
        ZonedDateTime z = ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        check(ZonedDateTime.of(LocalDate.of(2024, 1, 1), LocalTime.of(12, 0), ZoneOffset.UTC).equals(z),
                "of(LocalDate,LocalTime,ZoneId)");
        check(ZonedDateTime.of(LocalDateTime.of(2024, 1, 1, 12, 0), ZoneOffset.UTC).equals(z),
                "of(LocalDateTime,ZoneId)");
        check(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC).equals(z), "of(7 ints)");
        check(ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.ofHours(5))
                .equals(ZonedDateTime.of(1970, 1, 1, 5, 0, 0, 0, ZoneOffset.ofHours(5))), "ofInstant");
        check(ZonedDateTime.ofInstant(LocalDateTime.of(1970, 1, 1, 5, 0), ZoneOffset.ofHours(5),
                ZoneOffset.ofHours(5)).equals(ZonedDateTime.of(1970, 1, 1, 5, 0, 0, 0, ZoneOffset.ofHours(5))),
                "ofInstant(LDT,offset,zone)");
        check(ZonedDateTime.ofLocal(LocalDateTime.of(2024, 1, 1, 12, 0), ZoneOffset.UTC, ZoneOffset.UTC)
                .equals(z), "ofLocal");
        check(ZonedDateTime.ofStrict(LocalDateTime.of(2024, 1, 1, 12, 0), ZoneOffset.UTC, ZoneOffset.UTC)
                .equals(z), "ofStrict");
        check(ZonedDateTime.parse("2024-01-01T12:00Z").equals(z), "parse");
        check(ZonedDateTime.parse("2024-01-01T12:00+00:00").equals(z), "parse offset");
        check(ZonedDateTime.parse("2024-01-01T12:00+05:00").getOffset().equals(ZoneOffset.ofHours(5)),
                "parse non-zero offset");
        check(ZonedDateTime.from(z).equals(z), "from");
        check(ZonedDateTime.now(Clock.fixed(Instant.ofEpochSecond(0), ZoneOffset.UTC))
                .equals(ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)), "now fixed Clock");
        check(z.toString().equals("2024-01-01T12:00Z"), "toString");
    }

    static void testGetters() {
        ZonedDateTime z = ZonedDateTime.of(2024, 2, 29, 13, 45, 20, 123456789, ZoneOffset.ofHours(1));
        check(z.getOffset().equals(ZoneOffset.ofHours(1)), "getOffset");
        check(z.getZone().equals(ZoneOffset.ofHours(1)), "getZone");
        check(z.toLocalDateTime().equals(LocalDateTime.of(2024, 2, 29, 13, 45, 20, 123456789)),
                "toLocalDateTime");
        check(z.toLocalDate().equals(LocalDate.of(2024, 2, 29)), "toLocalDate");
        check(z.toLocalTime().equals(LocalTime.of(13, 45, 20, 123456789)), "toLocalTime");
        check(z.getYear() == 2024 && z.getMonthValue() == 2 && z.getDayOfMonth() == 29, "date getters");
        check(z.getHour() == 13 && z.getMinute() == 45 && z.getSecond() == 20 && z.getNano() == 123456789,
                "time getters");
        check(z.query(TemporalQueries.zone()).equals(ZoneOffset.ofHours(1)), "query zone");
        check(z.query(TemporalQueries.offset()).equals(ZoneOffset.ofHours(1)), "query offset");
    }

    static void testZoneConversions() {
        ZonedDateTime z = ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        check(z.withZoneSameInstant(ZoneOffset.ofHours(1))
                .equals(ZonedDateTime.of(2024, 1, 1, 13, 0, 0, 0, ZoneOffset.ofHours(1))),
                "withZoneSameInstant");
        check(z.withZoneSameLocal(ZoneOffset.ofHours(1))
                .equals(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.ofHours(1))),
                "withZoneSameLocal");
        check(z.withFixedOffsetZone().getZone().equals(ZoneOffset.UTC), "withFixedOffsetZone");
        check(z.withEarlierOffsetAtOverlap().equals(z), "withEarlierOffsetAtOverlap fixed");
        check(z.withLaterOffsetAtOverlap().equals(z), "withLaterOffsetAtOverlap fixed");
        check(z.equals(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)), "immutability zone");
    }

    static void testWithTruncateImmutable() {
        ZonedDateTime base = ZonedDateTime.of(2024, 2, 29, 13, 45, 20, 123456789, ZoneOffset.UTC);
        check(base.withYear(2023).equals(ZonedDateTime.of(2023, 2, 28, 13, 45, 20, 123456789, ZoneOffset.UTC)),
                "withYear clamps");
        check(base.withMonth(3).equals(ZonedDateTime.of(2024, 3, 29, 13, 45, 20, 123456789, ZoneOffset.UTC)),
                "withMonth");
        check(base.withHour(0).equals(ZonedDateTime.of(2024, 2, 29, 0, 45, 20, 123456789, ZoneOffset.UTC)),
                "withHour");
        check(base.withNano(0).equals(ZonedDateTime.of(2024, 2, 29, 13, 45, 20, 0, ZoneOffset.UTC)), "withNano");
        check(base.with(ChronoField.MINUTE_OF_HOUR, 0)
                .equals(ZonedDateTime.of(2024, 2, 29, 13, 0, 20, 123456789, ZoneOffset.UTC)), "with field");
        check(base.truncatedTo(ChronoUnit.SECONDS)
                .equals(ZonedDateTime.of(2024, 2, 29, 13, 45, 20, 0, ZoneOffset.UTC)), "truncatedTo SECONDS");
        check(base.truncatedTo(ChronoUnit.DAYS)
                .equals(ZonedDateTime.of(2024, 2, 29, 0, 0, 0, 0, ZoneOffset.UTC)), "truncatedTo DAYS");
        check(base.equals(ZonedDateTime.of(2024, 2, 29, 13, 45, 20, 123456789, ZoneOffset.UTC)), "immutability");
    }

    static void testArithmetic() {
        ZonedDateTime base = ZonedDateTime.of(2024, 1, 31, 23, 59, 59, 0, ZoneOffset.UTC);
        check(base.plusDays(1).equals(ZonedDateTime.of(2024, 2, 1, 23, 59, 59, 0, ZoneOffset.UTC)), "plusDays");
        check(base.plusMonths(1).equals(ZonedDateTime.of(2024, 2, 29, 23, 59, 59, 0, ZoneOffset.UTC)),
                "plusMonths clamps");
        check(base.plusHours(1).equals(ZonedDateTime.of(2024, 2, 1, 0, 59, 59, 0, ZoneOffset.UTC)), "plusHours");
        check(base.plusNanos(1).equals(ZonedDateTime.of(2024, 1, 31, 23, 59, 59, 1, ZoneOffset.UTC)), "plusNanos");
        check(base.plus(1, ChronoUnit.DAYS)
                .equals(ZonedDateTime.of(2024, 2, 1, 23, 59, 59, 0, ZoneOffset.UTC)), "plus unit");
        check(base.minusDays(1).equals(ZonedDateTime.of(2024, 1, 30, 23, 59, 59, 0, ZoneOffset.UTC)), "minusDays");
        check(base.minusHours(1).equals(ZonedDateTime.of(2024, 1, 31, 22, 59, 59, 0, ZoneOffset.UTC)), "minusHours");
        check(base.equals(ZonedDateTime.of(2024, 1, 31, 23, 59, 59, 0, ZoneOffset.UTC)), "immutability arithmetic");
    }

    static void testUntil() {
        ZonedDateTime a = ZonedDateTime.of(2024, 1, 15, 10, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime b = ZonedDateTime.of(2024, 1, 16, 12, 30, 0, 0, ZoneOffset.UTC);
        check(a.until(b, ChronoUnit.DAYS) == 1, "until DAYS");
        check(a.until(b, ChronoUnit.HOURS) == 26, "until HOURS");
        check(b.until(a, ChronoUnit.HOURS) == -26, "until negative");
    }

    static void testConversions() {
        ZonedDateTime z = ZonedDateTime.of(2024, 3, 1, 13, 45, 0, 0, ZoneOffset.ofHours(1));
        OffsetDateTime odt = z.toOffsetDateTime();
        check(odt.getOffset().equals(ZoneOffset.ofHours(1)), "toOffsetDateTime offset");
        check(odt.toZonedDateTime().equals(z), "toOffsetDateTime round-trip");
        check(z.format(DateTimeFormatter.ISO_ZONED_DATE_TIME).equals("2024-03-01T13:45:00+01:00"),
                "format");
    }

    static void testComparison() {
        ZonedDateTime a = ZonedDateTime.of(2024, 3, 1, 10, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime b = ZonedDateTime.of(2024, 3, 2, 10, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime a2 = ZonedDateTime.of(2024, 3, 1, 10, 0, 0, 0, ZoneOffset.UTC);
        check(a.compareTo(b) < 0 && b.compareTo(a) > 0 && a.compareTo(a2) == 0, "compareTo");
        check(a.equals(a2) && !a.equals(b), "equals");
        check(a.hashCode() == a2.hashCode(), "hashCode");
        check(a.equals(null) == false, "equals null");
        check(a.equals("x") == false, "equals other type");
    }

    static void testSerialization() throws Exception {
        check(ObjectStreamClass.lookup(ZonedDateTime.class).getSerialVersionUID() == -6260982410461394882L,
                "serialVersionUID");
        ZonedDateTime[] vals = { ZonedDateTime.of(2024, 2, 29, 13, 45, 20, 123456789, ZoneOffset.UTC),
                ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")),
                ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.ofHours(5)) };
        for (ZonedDateTime v : vals) {
            check(roundTrip(v).equals(v), "serialization round-trip " + v);
        }
    }

    static void testErrors() {
        checkThrows(DateTimeException.class, () -> ZonedDateTime.of(2024, 2, 30, 0, 0, 0, 0, ZoneOffset.UTC),
                "of bad date");
        checkThrows(DateTimeException.class, () -> ZonedDateTime.ofStrict(
                LocalDateTime.of(2024, 1, 1, 12, 0), ZoneOffset.ofHours(1), ZoneOffset.UTC),
                "ofStrict offset mismatch");
        checkThrows(DateTimeException.class,
                () -> ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).plusYears(Long.MAX_VALUE),
                "plusYears overflow");
        checkThrows(DateTimeParseException.class, () -> ZonedDateTime.parse("2024-01-01T12:00"), "parse no zone");
        checkThrows(DateTimeParseException.class, () -> ZonedDateTime.parse("garbage"), "parse garbage");
        checkThrows(UnsupportedTemporalTypeException.class,
                () -> ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).get(ChronoField.INSTANT_SECONDS),
                "get INSTANT_SECONDS");
    }

    static void testNullSemantics() {
        checkThrows(NullPointerException.class, () -> ZonedDateTime.of((LocalDateTime) null, ZoneOffset.UTC),
                "of LDT null");
        checkThrows(NullPointerException.class,
                () -> ZonedDateTime.of(LocalDateTime.of(2024, 1, 1, 0, 0), (ZoneId) null), "of zone null");
        checkThrows(NullPointerException.class, () -> ZonedDateTime.parse(null), "parse null");
        checkThrows(NullPointerException.class, () -> ZonedDateTime.from(null), "from null");
        checkThrows(NullPointerException.class, () -> ZonedDateTime.now((Clock) null), "now null");
        ZonedDateTime z = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        checkThrows(NullPointerException.class, () -> z.withZoneSameInstant(null), "withZoneSameInstant null");
        checkThrows(NullPointerException.class, () -> z.withZoneSameLocal(null), "withZoneSameLocal null");
        checkThrows(NullPointerException.class, () -> z.plus(null), "plus null");
        checkThrows(NullPointerException.class, () -> z.minus(null), "minus null");
        checkThrows(NullPointerException.class, () -> z.until(null, ChronoUnit.DAYS), "until null");
        checkThrows(NullPointerException.class, () -> z.format(null), "format null");
    }

    public static void main(String[] args) throws Exception {
        testFactories();
        testGetters();
        testZoneConversions();
        testWithTruncateImmutable();
        testArithmetic();
        testUntil();
        testConversions();
        testComparison();
        testSerialization();
        testErrors();
        testNullSemantics();
        testDeepZonedDateTime();
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


    static void testDeepZonedDateTime() {
        ZoneId ber = ZoneId.of("Europe/Berlin");
        ZonedDateTime z = ZonedDateTime.of(2024, 2, 29, 10, 15, 30, 0, ber);
        xeq(z.toString(), "2024-02-29T10:15:30+01:00[Europe/Berlin]", "toString");
        check(z.getZone() == ber && z.getOffset().equals(ZoneOffset.ofHours(1)), "zone/offset");
        check(z.getDayOfYear() == 60, "dayOfYear leap");
        xeq(z.toLocalDate().toString(), "2024-02-29", "toLocalDate");
        xeq(z.toLocalTime().toString(), "10:15:30", "toLocalTime");
        xeq(z.toLocalDateTime().toString(), "2024-02-29T10:15:30", "toLocalDateTime");
        xeq(z.toInstant().toString(), "2024-02-29T09:15:30Z", "toInstant");
        check(z.toEpochSecond() == 1709198130L, "toEpochSecond");
        xeq(z.toOffsetDateTime().toString(), "2024-02-29T10:15:30+01:00", "toOffsetDateTime");
        xeq(ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.ofHours(1)).toString(), "1970-01-01T01:00+01:00", "ofInstant");
        xeq(ZonedDateTime.ofLocal(LocalDateTime.of(2024, 2, 29, 10, 15, 30), ber, null).toString(),
            "2024-02-29T10:15:30+01:00[Europe/Berlin]", "ofLocal");
        xeq(ZonedDateTime.ofStrict(LocalDateTime.of(2024, 2, 29, 10, 15, 30), ZoneOffset.ofHours(1), ber).toString(),
            "2024-02-29T10:15:30+01:00[Europe/Berlin]", "ofStrict normal");
        xact(() -> ZonedDateTime.ofStrict(LocalDateTime.of(2024, 3, 31, 2, 30), ZoneOffset.ofHours(1), ber),
            DateTimeException.class,
            "LocalDateTime '2024-03-31T02:30' does not exist in zone 'Europe/Berlin' due to a gap in the local time-line, typically caused by daylight savings",
            "ofStrict gap");
        xact(() -> ZonedDateTime.ofStrict(LocalDateTime.of(2024, 2, 29, 10, 15, 30), ZoneOffset.ofHours(5), ber),
            DateTimeException.class,
            "ZoneOffset '+05:00' is not valid for LocalDateTime '2024-02-29T10:15:30' in zone 'Europe/Berlin'",
            "ofStrict wrong offset");
        xact(() -> ZonedDateTime.ofStrict(LocalDateTime.of(2024, 10, 27, 2, 30), ZoneOffset.ofHours(6), ber),
            DateTimeException.class,
            "ZoneOffset '+06:00' is not valid for LocalDateTime '2024-10-27T02:30' in zone 'Europe/Berlin'",
            "ofStrict overlap wrong offset");
        // tzdata Berlin 2024: gap 2024-03-31 02:00+01:00 -> 03:00+02:00; overlap 2024-10-27 03:00+02:00 -> 02:00+01:00
        ZonedDateTime gap = ZonedDateTime.of(LocalDateTime.of(2024, 3, 31, 2, 30), ber);
        xeq(gap.toString(), "2024-03-31T03:30+02:00[Europe/Berlin]", "gap shifts forward +1h");
        check(gap.getOffset().equals(ZoneOffset.ofHours(2)), "gap offset");
        ZonedDateTime ovl = ZonedDateTime.of(LocalDateTime.of(2024, 10, 27, 2, 30), ber);
        xeq(ovl.toString(), "2024-10-27T02:30+02:00[Europe/Berlin]", "overlap picks earlier offset");
        xeq(ovl.withLaterOffsetAtOverlap().toString(), "2024-10-27T02:30+01:00[Europe/Berlin]", "withLaterOffsetAtOverlap");
        xeq(ovl.withEarlierOffsetAtOverlap().toString(), "2024-10-27T02:30+02:00[Europe/Berlin]", "withEarlierOffsetAtOverlap");
        xeq(z.withZoneSameInstant(ZoneOffset.UTC).toString(), "2024-02-29T09:15:30Z", "withZoneSameInstant UTC");
        xeq(z.withZoneSameLocal(ZoneId.of("America/New_York")).toString(),
            "2024-02-29T10:15:30-05:00[America/New_York]", "withZoneSameLocal NY");
        xeq(z.withZoneSameLocal(ber).withZoneSameInstant(ZoneId.of("America/New_York")).getOffset().toString(),
            "-05:00", "instant-correct NY offset");
        xeq(z.withDayOfMonth(1).toString(), "2024-02-01T10:15:30+01:00[Europe/Berlin]", "withDayOfMonth");
        xeq(z.withDayOfYear(60).toString(), "2024-02-29T10:15:30+01:00[Europe/Berlin]", "withDayOfYear 60");
        xeq(z.withMonth(1).toString(), "2024-01-29T10:15:30+01:00[Europe/Berlin]", "withMonth");
        xeq(z.withYear(2023).toString(), "2023-02-28T10:15:30+01:00[Europe/Berlin]", "withYear clamps Feb29");
        check(z.getLong(ChronoField.OFFSET_SECONDS) == 3600L, "getLong offsetSeconds");
        check(z.format(DateTimeFormatter.ISO_ZONED_DATE_TIME).equals("2024-02-29T10:15:30+01:00[Europe/Berlin]"), "ISO_ZONED");
        check(ZonedDateTime.parse("2024-02-29T10:15:30+01:00[Europe/Berlin]").equals(z), "parse rt");
        check(ZonedDateTime.parse(z.toString()).equals(z), "toString rt");
        check(z.query(TemporalQueries.zoneId()) == ber, "query zoneId");
        check(z.query(TemporalQueries.offset()).equals(ZoneOffset.ofHours(1)), "query offset");
        check(z.query(TemporalQueries.localDate()).toString().equals("2024-02-29"), "query localDate");
        check(z.isSupported(ChronoField.INSTANT_SECONDS), "support INSTANT_SECONDS");
        check(z.isSupported(ChronoField.OFFSET_SECONDS), "support OFFSET_SECONDS");
        check(z.plusDays(1).minusDays(1).equals(z), "days sym");
        check(z.plusMonths(1).minusMonths(1).equals(z), "months sym");
        check(z.plus(Duration.ofHours(3)).minus(Duration.ofHours(3)).equals(z), "dur sym");
        check(z.plus(Period.ofWeeks(1)).minus(Period.ofWeeks(1)).equals(z), "period sym");
        check(z.plus(1, ChronoUnit.HOURS).equals(ZonedDateTime.of(2024, 2, 29, 11, 15, 30, 0, ber)), "plus hours");
        xact(() -> ZonedDateTime.of(null, ber), NullPointerException.class, null, "of null ldt");
        xact(() -> ZonedDateTime.of(LocalDateTime.of(2024, 2, 29, 10, 15, 30), null), NullPointerException.class, "zone", "of null zone");
        check(z.truncatedTo(ChronoUnit.DAYS).toString().equals("2024-02-29T00:00+01:00[Europe/Berlin]"), "trunc DAYS");
        check(z.truncatedTo(ChronoUnit.HOURS).toString().equals("2024-02-29T10:00+01:00[Europe/Berlin]"), "trunc HOURS");
        check(z.compareTo(ZonedDateTime.of(2024, 2, 29, 10, 15, 31, 0, ber)) < 0, "compareTo");
        check(z.isBefore(ZonedDateTime.of(2024, 3, 1, 0, 0, 0, 0, ber)), "isBefore");
        check(z.isEqual(ZonedDateTime.of(2024, 2, 29, 4, 15, 30, 0, ZoneId.of("America/New_York"))), "isEqual across zones");
        check(!z.equals(ZonedDateTime.of(2024, 2, 29, 4, 15, 30, 0, ZoneId.of("America/New_York"))), "equals differs from isEqual");
        check(z.hashCode() == ZonedDateTime.parse(z.toString()).hashCode(), "hash rt");
        xeq(z.minusYears(24).toString(), "2000-02-29T10:15:30+01:00[Europe/Berlin]", "minusYears keeps Feb29 across 2 leaps");
    }

}
