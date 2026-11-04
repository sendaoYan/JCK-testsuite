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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
