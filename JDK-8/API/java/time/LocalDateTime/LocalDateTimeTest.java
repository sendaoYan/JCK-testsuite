/*
 * @test
 * @summary java.time.LocalDateTime API conformance: full contract coverage.
 *          factories (MIN/MAX, of all overloads, ofInstant/ofEpochSecond/from/
 *          parse/now fixed Clock) + exact exception matrix, null semantics,
 *          getters (date/time parts), temporal accessors, with/truncatedTo/
 *          plus/minus with immutability, until, atOffset/atZone, comparisons,
 *          and serialization round-trip + serialVersionUID.
 * @run main LocalDateTimeTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
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
import java.time.temporal.ValueRange;
import java.time.Duration;
import java.time.Period;

public class LocalDateTimeTest {
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

    static LocalDateTime roundTrip(LocalDateTime v) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(v);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (LocalDateTime) ois.readObject();
    }

    static void testFactories() {
        LocalDateTime d = LocalDateTime.of(2024, 2, 29, 13, 45, 20, 123456789);
        check(LocalDateTime.of(2024, 2, 29, 13, 45).equals(LocalDateTime.of(2024, 2, 29, 13, 45, 0, 0)),
                "of(5 args)");
        check(LocalDateTime.of(2024, 2, 29, 13, 45, 20).equals(LocalDateTime.of(2024, 2, 29, 13, 45, 20, 0)),
                "of(6 args)");
        check(LocalDateTime.of(2024, 2, 29, 13, 45, 20, 123456789).equals(d), "of(7 args)");
        check(LocalDateTime.of(2024, Month.FEBRUARY, 29, 13, 45).equals(LocalDateTime.of(2024, 2, 29, 13, 45)),
                "of(int,Month,...)");
        check(LocalDateTime.of(LocalDate.of(2024, 2, 29), LocalTime.of(13, 45))
                .equals(LocalDateTime.of(2024, 2, 29, 13, 45)), "of(LocalDate,LocalTime)");
        check(LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC)
                .equals(LocalDateTime.of(1970, 1, 1, 0, 0)), "ofInstant");
        check(LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)
                .equals(LocalDateTime.of(1970, 1, 1, 0, 0)), "ofEpochSecond");
        check(LocalDateTime.ofEpochSecond(86400, 0, ZoneOffset.UTC)
                .equals(LocalDateTime.of(1970, 1, 2, 0, 0)), "ofEpochSecond +1day");
        check(LocalDateTime.parse("2024-02-29T13:45:20.123456789").equals(d), "parse");
        check(LocalDateTime.parse("2024-02-29T13:45").equals(LocalDateTime.of(2024, 2, 29, 13, 45)),
                "parse T hh:mm");
        check(LocalDateTime.from(d).equals(d), "from");
        check(LocalDateTime.now(Clock.fixed(Instant.ofEpochSecond(0), ZoneOffset.UTC))
                .equals(LocalDateTime.of(1970, 1, 1, 0, 0)), "now fixed Clock");
        check(LocalDateTime.MIN.equals(LocalDateTime.of(-999999999, 1, 1, 0, 0)), "MIN");
        check(LocalDateTime.MAX.equals(LocalDateTime.of(999999999, 12, 31, 23, 59, 59, 999999999)), "MAX");
        check(d.toString().equals("2024-02-29T13:45:20.123456789"), "toString");
    }

    static void testGetters() {
        LocalDateTime d = LocalDateTime.of(2024, 2, 29, 13, 45, 20, 123456789);
        check(d.toLocalDate().equals(LocalDate.of(2024, 2, 29)), "toLocalDate");
        check(d.toLocalTime().equals(LocalTime.of(13, 45, 20, 123456789)), "toLocalTime");
        check(d.getYear() == 2024 && d.getMonthValue() == 2 && d.getDayOfMonth() == 29, "date getters");
        check(d.getMonth() == Month.FEBRUARY, "getMonth");
        check(d.getDayOfYear() == 60, "getDayOfYear");
        check(d.getDayOfWeek() == DayOfWeek.THURSDAY, "getDayOfWeek");
        check(d.getHour() == 13 && d.getMinute() == 45 && d.getSecond() == 20 && d.getNano() == 123456789,
                "time getters");
    }

    static void testTemporalAccessors() {
        LocalDateTime d = LocalDateTime.of(2024, 2, 29, 13, 45, 20);
        check(d.isSupported(ChronoField.DAY_OF_MONTH) && d.isSupported(ChronoField.HOUR_OF_DAY)
                && d.isSupported(ChronoUnit.DAYS), "isSupported");
        check(!d.isSupported(ChronoField.OFFSET_SECONDS), "isSupported OFFSET false");
        check(d.get(ChronoField.HOUR_OF_DAY) == 13, "get HOUR_OF_DAY");
        check(d.getLong(ChronoField.EPOCH_DAY) == LocalDate.of(2024, 2, 29).toEpochDay(),
                "getLong EPOCH_DAY");
        check(d.getLong(ChronoField.NANO_OF_DAY) == 49520000000000L, "getLong NANO_OF_DAY");
        ValueRange r = d.range(ChronoField.DAY_OF_MONTH);
        check(r.getMaximum() == 29, "range DAY_OF_MONTH");
        check(d.query(TemporalQueries.localDate()).equals(LocalDate.of(2024, 2, 29)), "query localDate");
        check(d.query(TemporalQueries.localTime()).equals(LocalTime.of(13, 45, 20)), "query localTime");
        check(d.query(TemporalQueries.precision()) == ChronoUnit.NANOS, "query precision");
        checkThrows(UnsupportedTemporalTypeException.class, () -> d.get(ChronoField.OFFSET_SECONDS),
                "get OFFSET_SECONDS");
    }

    static void testWithTruncateImmutable() {
        LocalDateTime base = LocalDateTime.of(2024, 2, 29, 13, 45, 20, 123456789);
        check(base.withYear(2023).equals(LocalDateTime.of(2023, 2, 28, 13, 45, 20, 123456789)),
                "withYear clamps");
        check(base.withMonth(3).equals(LocalDateTime.of(2024, 3, 29, 13, 45, 20, 123456789)), "withMonth");
        check(base.withDayOfMonth(1).equals(LocalDateTime.of(2024, 2, 1, 13, 45, 20, 123456789)),
                "withDayOfMonth");
        check(base.withHour(0).equals(LocalDateTime.of(2024, 2, 29, 0, 45, 20, 123456789)), "withHour");
        check(base.withNano(0).equals(LocalDateTime.of(2024, 2, 29, 13, 45, 20)), "withNano");
        check(base.with(ChronoField.MINUTE_OF_HOUR, 0).equals(LocalDateTime.of(2024, 2, 29, 13, 0, 20, 123456789)),
                "with field");
        check(base.truncatedTo(ChronoUnit.SECONDS).equals(LocalDateTime.of(2024, 2, 29, 13, 45, 20)),
                "truncatedTo SECONDS");
        check(base.truncatedTo(ChronoUnit.DAYS).equals(LocalDateTime.of(2024, 2, 29, 0, 0)), "truncatedTo DAYS");
        check(base.equals(LocalDateTime.of(2024, 2, 29, 13, 45, 20, 123456789)), "immutability");
    }

    static void testArithmetic() {
        LocalDateTime base = LocalDateTime.of(2024, 1, 31, 23, 59, 59);
        check(base.plusDays(1).equals(LocalDateTime.of(2024, 2, 1, 23, 59, 59)), "plusDays");
        check(base.plusMonths(1).equals(LocalDateTime.of(2024, 2, 29, 23, 59, 59)), "plusMonths clamps");
        check(base.plusYears(1).equals(LocalDateTime.of(2025, 1, 31, 23, 59, 59)), "plusYears");
        check(base.plusHours(1).equals(LocalDateTime.of(2024, 2, 1, 0, 59, 59)), "plusHours");
        check(base.plusMinutes(1).equals(LocalDateTime.of(2024, 2, 1, 0, 0, 59)), "plusMinutes");
        check(base.plusSeconds(1).equals(LocalDateTime.of(2024, 2, 1, 0, 0, 0)), "plusSeconds");
        check(base.plusNanos(1).equals(LocalDateTime.of(2024, 1, 31, 23, 59, 59, 1)), "plusNanos");
        check(base.plus(1, ChronoUnit.DAYS).equals(LocalDateTime.of(2024, 2, 1, 23, 59, 59)), "plus unit");
        check(base.minusDays(1).equals(LocalDateTime.of(2024, 1, 30, 23, 59, 59)), "minusDays");
        check(base.minusMonths(1).equals(LocalDateTime.of(2023, 12, 31, 23, 59, 59)), "minusMonths");
        check(base.minusHours(1).equals(LocalDateTime.of(2024, 1, 31, 22, 59, 59)), "minusHours");
        check(base.equals(LocalDateTime.of(2024, 1, 31, 23, 59, 59)), "immutability arithmetic");
    }

    static void testUntil() {
        LocalDateTime a = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime b = LocalDateTime.of(2024, 1, 16, 12, 30);
        check(a.until(b, ChronoUnit.DAYS) == 1, "until DAYS");
        check(a.until(b, ChronoUnit.HOURS) == 26, "until HOURS");
        check(a.until(b, ChronoUnit.MINUTES) == 1590, "until MINUTES");
        check(b.until(a, ChronoUnit.HOURS) == -26, "until negative");
        check(a.until(a, ChronoUnit.SECONDS) == 0, "until same");
    }

    static void testConversions() {
        LocalDateTime d = LocalDateTime.of(2024, 3, 1, 13, 45);
        OffsetDateTime odt = d.atOffset(ZoneOffset.ofHours(1));
        check(odt.getOffset().equals(ZoneOffset.ofHours(1)), "atOffset offset");
        check(odt.toLocalDateTime().equals(d), "atOffset round-trip");
        ZonedDateTime zdt = d.atZone(ZoneId.of("UTC"));
        check(zdt.getZone().equals(ZoneId.of("UTC")), "atZone zone");
        check(zdt.toLocalDateTime().equals(d), "atZone round-trip");
        check(d.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).equals("2024-03-01T13:45:00"), "format");
        check(d.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")).equals("2024/03/01 13:45"),
                "format pattern");
    }

    static void testComparison() {
        LocalDateTime a = LocalDateTime.of(2024, 3, 1, 10, 0);
        LocalDateTime b = LocalDateTime.of(2024, 3, 2, 10, 0);
        LocalDateTime a2 = LocalDateTime.of(2024, 3, 1, 10, 0);
        check(a.isBefore(b) && b.isAfter(a), "isBefore/isAfter");
        check(a.isEqual(a2) && !a.isAfter(a2) && !a.isBefore(a2), "isEqual");
        check(a.compareTo(b) < 0 && b.compareTo(a) > 0 && a.compareTo(a2) == 0, "compareTo");
        check(a.equals(a2) && !a.equals(b), "equals");
        check(a.hashCode() == a2.hashCode(), "hashCode");
        check(a.equals(null) == false, "equals null");
        check(a.equals("2024-03-01T10:00") == false, "equals other type");
        checkThrows(NullPointerException.class, () -> a.compareTo(null), "compareTo null");
    }

    static void testSerialization() throws Exception {
        check(ObjectStreamClass.lookup(LocalDateTime.class).getSerialVersionUID() == 6207766400415563566L,
                "serialVersionUID");
        LocalDateTime[] vals = { LocalDateTime.MIN, LocalDateTime.MAX,
                LocalDateTime.of(2024, 2, 29, 13, 45, 20, 123456789) };
        for (LocalDateTime v : vals) {
            check(roundTrip(v).equals(v), "serialization round-trip " + v);
        }
    }

    static void testErrors() {
        checkThrows(DateTimeException.class, () -> LocalDateTime.of(2024, 2, 30, 0, 0), "of bad date");
        checkThrows(DateTimeException.class, () -> LocalDateTime.of(2024, 1, 1, 24, 0), "of hour 24");
        checkThrows(DateTimeException.class, () -> LocalDateTime.of(2024, 1, 1, 0, 60), "of minute 60");
        checkThrows(DateTimeException.class,
                () -> LocalDateTime.of(2024, 1, 1, 0, 0).plusYears(Long.MAX_VALUE), "plusYears overflow");
        checkThrows(DateTimeParseException.class, () -> LocalDateTime.parse("2024-02-30T10:00"),
                "parse bad date");
        checkThrows(DateTimeParseException.class, () -> LocalDateTime.parse("garbage"), "parse garbage");
        checkThrows(UnsupportedTemporalTypeException.class,
                () -> LocalDateTime.of(2024, 1, 1, 0, 0).get(ChronoField.OFFSET_SECONDS),
                "get OFFSET_SECONDS");
    }

    static void testNullSemantics() {
        checkThrows(NullPointerException.class, () -> LocalDateTime.of((LocalDate) null, LocalTime.NOON),
                "of LocalDate null");
        checkThrows(NullPointerException.class, () -> LocalDateTime.of(LocalDate.of(2024, 2, 29), (LocalTime) null),
                "of LocalTime null");
        checkThrows(NullPointerException.class, () -> LocalDateTime.parse(null), "parse null");
        checkThrows(NullPointerException.class, () -> LocalDateTime.from(null), "from null");
        checkThrows(NullPointerException.class, () -> LocalDateTime.now((Clock) null), "now null");
        LocalDateTime d = LocalDateTime.of(2024, 1, 1, 0, 0);
        checkThrows(NullPointerException.class, () -> d.plus(null), "plus null");
        checkThrows(NullPointerException.class, () -> d.plus(1, null), "plus unit null");
        checkThrows(NullPointerException.class, () -> d.minus(null), "minus null");
        checkThrows(NullPointerException.class, () -> d.with((java.time.temporal.TemporalField) null, 1),
                "with field null");
        checkThrows(NullPointerException.class, () -> d.until(null, ChronoUnit.DAYS), "until null");
        checkThrows(NullPointerException.class, () -> d.atOffset(null), "atOffset null");
        checkThrows(NullPointerException.class, () -> d.atZone(null), "atZone null");
        checkThrows(NullPointerException.class, () -> d.format(null), "format null");
    }

    public static void main(String[] args) throws Exception {
        testFactories();
        testGetters();
        testTemporalAccessors();
        testWithTruncateImmutable();
        testArithmetic();
        testUntil();
        testConversions();
        testComparison();
        testSerialization();
        testErrors();
        testNullSemantics();
        testDeepLocalDateTime();
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


    static void testDeepLocalDateTime() {
        LocalDateTime ldt = LocalDateTime.of(2024, 2, 29, 10, 15, 30, 123);
        xeq(ldt.toString(), "2024-02-29T10:15:30.000000123", "toString nano");
        xeq(LocalDateTime.of(2024, 2, 29, 10, 15).toString(), "2024-02-29T10:15", "toString hm");
        xeq(LocalDateTime.of(LocalDate.of(2024, 2, 29), LocalTime.of(10, 15)).toString(), "2024-02-29T10:15", "of(date,time)");
        xact(() -> LocalDateTime.of(2024, 2, 30, 10, 15), DateTimeException.class,
            "Invalid date 'FEBRUARY 30'", "invalid date");
        xact(() -> LocalDateTime.of(2024, 2, 29, 24, 0), DateTimeException.class,
            "Invalid value for HourOfDay (valid values 0 - 23): 24", "hour 24");
        xeq(ldt.toLocalDate().toString(), "2024-02-29", "toLocalDate");
        xeq(ldt.toLocalTime().toString(), "10:15:30.000000123", "toLocalTime");
        xeq(ldt.toInstant(ZoneOffset.ofHours(1)).toString(), "2024-02-29T09:15:30.000000123Z", "toInstant");
        check(ldt.toEpochSecond(ZoneOffset.ofHours(1)) == 1709198130L, "toEpochSecond");
        xeq(ldt.atZone(ZoneOffset.ofHours(2)).toString(), "2024-02-29T10:15:30.000000123+02:00", "atZone");
        check(LocalDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneOffset.ofHours(1)).toString().equals("1970-01-01T01:00"), "ofInstant");
        xeq(ldt.truncatedTo(ChronoUnit.HOURS).toString(), "2024-02-29T10:00", "trunc HOURS");
        xeq(ldt.truncatedTo(ChronoUnit.DAYS).toString(), "2024-02-29T00:00", "trunc DAYS");
        xeq(ldt.truncatedTo(ChronoUnit.MINUTES).toString(), "2024-02-29T10:15", "trunc MINUTES");
        xact(() -> ldt.truncatedTo(ChronoUnit.FOREVER), UnsupportedTemporalTypeException.class,
            "Unit is too large to be used for truncation", "trunc FOREVER");
        xeq(ldt.withNano(5).toString(), "2024-02-29T10:15:30.000000005", "withNano");
        xeq(ldt.withDayOfYear(60).toString(), "2024-02-29T10:15:30.000000123", "withDayOfYear 60");
        xact(() -> ldt.withDayOfYear(367), DateTimeException.class,
            "Invalid value for DayOfYear (valid values 1 - 365/366): 367", "withDayOfYear 367");
        xeq(ldt.withYear(2023).toString(), "2023-02-28T10:15:30.000000123", "withYear clamp");
        xeq(ldt.plusWeeks(1).toString(), "2024-03-07T10:15:30.000000123", "plusWeeks");
        xeq(LocalDateTime.of(2024, 1, 31, 0, 0).plusMonths(1).toString(), "2024-02-29T00:00", "plusMonths clamp");
        check(ldt.isSupported(ChronoField.NANO_OF_SECOND), "support nano");
        check(ldt.isSupported(ChronoUnit.HOURS), "support hours");
        check(!ldt.isSupported(ChronoField.OFFSET_SECONDS), "no offset field");
        xact(() -> ldt.getLong(ChronoField.OFFSET_SECONDS), UnsupportedTemporalTypeException.class,
            "Unsupported field: OffsetSeconds", "getLong offset");
        xact(() -> ldt.getLong(ChronoField.INSTANT_SECONDS), UnsupportedTemporalTypeException.class,
            "Unsupported field: InstantSeconds", "getLong instant");
        check(LocalDateTime.parse("2024-02-29T10:15:30").equals(LocalDateTime.of(2024, 2, 29, 10, 15, 30)), "parse equals of");
        String[] rts = {"2024-02-29T10:15:30", "2024-02-29T10:15", "1970-01-01T00:00:00.000000001"};
        for (String s : rts) check(LocalDateTime.parse(s).toString().equals(s), "rt " + s);
        xeq(ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), "2024-02-29T10:15:30.000000123", "format ISO");
        check(ldt.plus(Duration.ofHours(25)).minus(Duration.ofHours(25)).equals(ldt), "dur sym");
        check(ldt.plus(Period.ofMonths(1)).minus(Period.ofMonths(1)).equals(ldt), "period sym");
        check(ldt.plusDays(1).minusDays(1).equals(ldt), "days sym");
        check(LocalDateTime.of(2024, 1, 1, 0, 0).compareTo(ldt) < 0, "compareTo");
        check(ldt.isAfter(LocalDateTime.of(2024, 2, 29, 10, 15, 30, 122)), "isAfter nano");
        check(LocalDateTime.of(2024, 2, 29, 10, 15, 30, 123).hashCode()
                == LocalDateTime.of(LocalDate.of(2024, 2, 29), LocalTime.of(10, 15, 30, 123)).hashCode(), "hash path eq");
    }

}
