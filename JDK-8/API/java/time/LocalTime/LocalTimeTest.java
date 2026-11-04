/*
 * @test
 * @summary java.time.LocalTime API conformance: full contract coverage.
 *          factories (MIN/MIDNIGHT/MAX/NOON, of/ofSecondOfDay/ofNanoOfDay/from/
 *          parse/now fixed Clock) + exact exception matrix, null semantics,
 *          getters, temporal accessors (isSupported/range/get/getLong/query),
 *          with/truncatedTo/plus/minus with immutability and nanosecond
 *          precision, until, atDate/atOffset, toSecondOfDay/toNanoOfDay,
 *          comparisons, and serialization round-trip + serialVersionUID.
 * @run main LocalTimeTest
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
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalQueries;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.time.Duration;

public class LocalTimeTest {
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

    static LocalTime roundTrip(LocalTime v) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(v);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (LocalTime) ois.readObject();
    }

    static void testFactories() {
        LocalTime t = LocalTime.of(13, 45, 20, 123456789);
        check(LocalTime.of(13, 45).equals(LocalTime.of(13, 45, 0, 0)), "of(int,int)");
        check(LocalTime.of(13, 45, 20).equals(LocalTime.of(13, 45, 20, 0)), "of(int,int,int)");
        check(LocalTime.of(13, 45, 20, 123456789).equals(t), "of(int,int,int,int)");
        check(LocalTime.MIDNIGHT.equals(LocalTime.of(0, 0)), "MIDNIGHT");
        check(LocalTime.MIN.equals(LocalTime.MIDNIGHT), "MIN == MIDNIGHT");
        check(LocalTime.MAX.equals(LocalTime.of(23, 59, 59, 999999999)), "MAX");
        check(LocalTime.NOON.equals(LocalTime.of(12, 0)), "NOON");
        check(LocalTime.ofSecondOfDay(0).equals(LocalTime.MIDNIGHT), "ofSecondOfDay 0");
        check(LocalTime.ofSecondOfDay(86399).equals(LocalTime.of(23, 59, 59)), "ofSecondOfDay max");
        check(LocalTime.ofNanoOfDay(0).equals(LocalTime.MIDNIGHT), "ofNanoOfDay 0");
        check(LocalTime.ofNanoOfDay(86399999999999L).equals(LocalTime.MAX), "ofNanoOfDay max");
        check(LocalTime.parse("13:45:20.123456789").equals(t), "parse");
        check(LocalTime.parse("13:45").equals(LocalTime.of(13, 45)), "parse HH:mm");
        check(LocalTime.parse(LocalTime.MAX.toString()).equals(LocalTime.MAX), "parse MAX round-trip");
        check(LocalTime.from(LocalDateTime.of(2024, 1, 1, 13, 45)).equals(LocalTime.of(13, 45)), "from");
        check(LocalTime.now(Clock.fixed(Instant.ofEpochSecond(45296), ZoneOffset.UTC))
                .equals(LocalTime.of(12, 34, 56)), "now fixed Clock");
        check(t.toString().equals("13:45:20.123456789"), "toString");
    }

    static void testGetters() {
        LocalTime t = LocalTime.of(13, 45, 20, 123456789);
        check(t.getHour() == 13 && t.getMinute() == 45 && t.getSecond() == 20
                && t.getNano() == 123456789, "getters");
        check(t.toSecondOfDay() == 49520, "toSecondOfDay");
        check(t.toNanoOfDay() == 49520123456789L, "toNanoOfDay");
    }

    static void testTemporalAccessors() {
        LocalTime t = LocalTime.of(13, 45, 20);
        check(t.isSupported(ChronoField.HOUR_OF_DAY) && t.isSupported(ChronoField.MINUTE_OF_HOUR)
                && t.isSupported(ChronoUnit.HOURS), "isSupported");
        check(!t.isSupported(ChronoField.DAY_OF_MONTH), "isSupported DAY_OF_MONTH false");
        check(t.get(ChronoField.HOUR_OF_DAY) == 13, "get HOUR_OF_DAY");
        check(t.get(ChronoField.MINUTE_OF_HOUR) == 45, "get MINUTE_OF_HOUR");
        check(t.getLong(ChronoField.NANO_OF_DAY) == 49520000000000L, "getLong NANO_OF_DAY");
        check(t.getLong(ChronoField.SECOND_OF_DAY) == 49520, "getLong SECOND_OF_DAY");
        ValueRange r = t.range(ChronoField.HOUR_OF_DAY);
        check(r.getMinimum() == 0 && r.getMaximum() == 23, "range HOUR_OF_DAY");
        check(t.query(TemporalQueries.localTime()) == t, "query localTime");
        check(t.query(TemporalQueries.precision()) == ChronoUnit.NANOS, "query precision");
        checkThrows(UnsupportedTemporalTypeException.class, () -> t.get(ChronoField.DAY_OF_MONTH),
                "get DAY_OF_MONTH");
        checkThrows(UnsupportedTemporalTypeException.class, () -> t.range(ChronoField.DAY_OF_MONTH),
                "range DAY_OF_MONTH");
    }

    static void testWithTruncateImmutable() {
        LocalTime base = LocalTime.of(13, 45, 20, 123456789);
        check(base.withHour(1).equals(LocalTime.of(1, 45, 20, 123456789)), "withHour");
        check(base.withMinute(0).equals(LocalTime.of(13, 0, 20, 123456789)), "withMinute");
        check(base.withSecond(0).equals(LocalTime.of(13, 45, 0, 123456789)), "withSecond");
        check(base.withNano(0).equals(LocalTime.of(13, 45, 20)), "withNano");
        check(base.with(ChronoField.HOUR_OF_DAY, 5).equals(LocalTime.of(5, 45, 20, 123456789)),
                "with field");
        check(base.truncatedTo(ChronoUnit.SECONDS).equals(LocalTime.of(13, 45, 20)), "truncatedTo SECONDS");
        check(base.truncatedTo(ChronoUnit.MINUTES).equals(LocalTime.of(13, 45)), "truncatedTo MINUTES");
        check(base.truncatedTo(ChronoUnit.HOURS).equals(LocalTime.of(13, 0)), "truncatedTo HOURS");
        check(base.equals(LocalTime.of(13, 45, 20, 123456789)), "immutability");
    }

    static void testArithmetic() {
        LocalTime base = LocalTime.of(23, 59, 59, 999999999);
        check(base.plusNanos(1).equals(LocalTime.MIDNIGHT), "plusNanos wrap");
        check(base.plusSeconds(1).equals(LocalTime.of(0, 0, 0, 999999999)), "plusSeconds wrap");
        check(base.plusMinutes(1).equals(LocalTime.of(0, 0, 59, 999999999)), "plusMinutes wrap");
        check(base.plusHours(1).equals(LocalTime.of(0, 59, 59, 999999999)), "plusHours wrap");
        check(base.plus(1, ChronoUnit.NANOS).equals(LocalTime.MIDNIGHT), "plus unit");
        check(base.plus(1, ChronoUnit.HOURS).equals(LocalTime.of(0, 59, 59, 999999999)), "plus unit HOURS");
        check(LocalTime.MIDNIGHT.minusNanos(1).equals(base), "minusNanos wrap");
        check(LocalTime.of(12, 0).minusHours(1).equals(LocalTime.of(11, 0)), "minusHours");
        check(LocalTime.of(12, 0).minusMinutes(1).equals(LocalTime.of(11, 59)), "minusMinutes");
        check(LocalTime.of(12, 0).minusSeconds(1).equals(LocalTime.of(11, 59, 59)), "minusSeconds");
        check(LocalTime.of(12, 0).minus(1, ChronoUnit.HOURS).equals(LocalTime.of(11, 0)), "minus unit");
        check(base.equals(LocalTime.of(23, 59, 59, 999999999)), "immutability arithmetic");
    }

    static void testUntil() {
        LocalTime a = LocalTime.of(10, 0);
        LocalTime b = LocalTime.of(12, 30);
        check(a.until(b, ChronoUnit.HOURS) == 2, "until HOURS");
        check(a.until(b, ChronoUnit.MINUTES) == 150, "until MINUTES");
        check(a.until(b, ChronoUnit.SECONDS) == 9000, "until SECONDS");
        check(b.until(a, ChronoUnit.MINUTES) == -150, "until negative");
        check(a.until(a, ChronoUnit.MINUTES) == 0, "until same");
    }

    static void testConversions() {
        LocalTime t = LocalTime.of(13, 45);
        check(t.atDate(LocalDate.of(2024, 3, 1)).equals(LocalDateTime.of(2024, 3, 1, 13, 45)), "atDate");
        check(t.atOffset(ZoneOffset.ofHours(1)).getOffset().equals(ZoneOffset.ofHours(1)), "atOffset");
        check(t.format(DateTimeFormatter.ISO_LOCAL_TIME).equals("13:45:00"), "format");
        check(t.format(DateTimeFormatter.ofPattern("HH-mm")).equals("13-45"), "format pattern");
    }

    static void testComparison() {
        LocalTime a = LocalTime.of(10, 0);
        LocalTime b = LocalTime.of(11, 0);
        LocalTime a2 = LocalTime.of(10, 0);
        check(a.isBefore(b) && b.isAfter(a), "isBefore/isAfter");
        check(a.compareTo(b) < 0 && b.compareTo(a) > 0 && a.compareTo(a2) == 0, "compareTo");
        check(a.equals(a2) && !a.equals(b), "equals");
        check(a.hashCode() == a2.hashCode(), "hashCode");
        check(a.equals(null) == false, "equals null");
        check(a.equals("10:00") == false, "equals other type");
        checkThrows(NullPointerException.class, () -> a.compareTo(null), "compareTo null");
    }

    static void testSerialization() throws Exception {
        check(ObjectStreamClass.lookup(LocalTime.class).getSerialVersionUID() == 6414437269572265201L,
                "serialVersionUID");
        LocalTime[] vals = { LocalTime.MIDNIGHT, LocalTime.NOON, LocalTime.MAX,
                LocalTime.of(13, 45, 20, 123456789) };
        for (LocalTime v : vals) {
            check(roundTrip(v).equals(v), "serialization round-trip " + v);
        }
    }

    static void testErrors() {
        checkThrows(DateTimeException.class, () -> LocalTime.of(24, 0), "of hour 24");
        checkThrows(DateTimeException.class, () -> LocalTime.of(13, 60), "of minute 60");
        checkThrows(DateTimeException.class, () -> LocalTime.of(13, 45, 60), "of second 60");
        checkThrows(DateTimeException.class, () -> LocalTime.of(13, 45, 20, 1000000000), "of nano overflow");
        checkThrows(DateTimeException.class, () -> LocalTime.ofSecondOfDay(86400), "ofSecondOfDay overflow");
        checkThrows(DateTimeException.class, () -> LocalTime.ofSecondOfDay(-1), "ofSecondOfDay negative");
        checkThrows(DateTimeException.class, () -> LocalTime.ofNanoOfDay(86400000000000L),
                "ofNanoOfDay overflow");
        checkThrows(DateTimeParseException.class, () -> LocalTime.parse("25:00"), "parse bad");
        checkThrows(DateTimeParseException.class, () -> LocalTime.parse("garbage"), "parse garbage");
        checkThrows(UnsupportedTemporalTypeException.class,
                () -> LocalTime.MIDNIGHT.truncatedTo(ChronoUnit.WEEKS), "truncatedTo WEEKS");
    }

    static void testNullSemantics() {
        checkThrows(NullPointerException.class, () -> LocalTime.parse(null), "parse null");
        checkThrows(NullPointerException.class, () -> LocalTime.from(null), "from null");
        checkThrows(NullPointerException.class, () -> LocalTime.now((Clock) null), "now null");
        LocalTime t = LocalTime.NOON;
        checkThrows(NullPointerException.class, () -> t.plus(null), "plus null");
        checkThrows(NullPointerException.class, () -> t.plus(1, null), "plus unit null");
        checkThrows(NullPointerException.class, () -> t.minus(null), "minus null");
        checkThrows(NullPointerException.class, () -> t.with((java.time.temporal.TemporalField) null, 1),
                "with field null");
        checkThrows(NullPointerException.class, () -> t.until(null, ChronoUnit.HOURS), "until null");
        checkThrows(NullPointerException.class, () -> t.atDate(null), "atDate null");
        checkThrows(NullPointerException.class, () -> t.atOffset(null), "atOffset null");
        checkThrows(NullPointerException.class, () -> t.isBefore(null), "isBefore null");
        checkThrows(NullPointerException.class, () -> t.format(null), "format null");
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
        testDeepLocalTime();
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


    // ===== deepening: boundary matrix, wrap-around, truncation, conversions =====
    static void testDeepLocalTime() {
        xeq(LocalTime.of(0, 0).toString(), "00:00", "min");
        xeq(LocalTime.of(23, 59, 59, 999999999).toString(), "23:59:59.999999999", "max");
        xeq(LocalTime.of(23, 59, 59).toString(), "23:59:59", "hms");
        xeq(LocalTime.of(12, 30).toString(), "12:30", "hm");
        xact(() -> LocalTime.of(24, 0), DateTimeException.class,
            "Invalid value for HourOfDay (valid values 0 - 23): 24", "hour 24");
        xact(() -> LocalTime.of(-1, 0), DateTimeException.class,
            "Invalid value for HourOfDay (valid values 0 - 23): -1", "hour -1");
        xact(() -> LocalTime.of(23, 60), DateTimeException.class,
            "Invalid value for MinuteOfHour (valid values 0 - 59): 60", "minute 60");
        xact(() -> LocalTime.of(23, 59, 60), DateTimeException.class,
            "Invalid value for SecondOfMinute (valid values 0 - 59): 60", "second 60");
        xact(() -> LocalTime.of(0, 0, 0, 1000000000), DateTimeException.class,
            "Invalid value for NanoOfSecond (valid values 0 - 999999999): 1000000000", "nano 10^9");
        xact(() -> LocalTime.ofSecondOfDay(86400), DateTimeException.class,
            "Invalid value for SecondOfDay (valid values 0 - 86399): 86400", "secondOfDay overflow");
        xact(() -> LocalTime.ofNanoOfDay(-1), DateTimeException.class,
            "Invalid value for NanoOfDay (valid values 0 - 86399999999999): -1", "nanoOfDay neg");
        xeq(LocalTime.MAX.plusNanos(1).toString(), "00:00", "MAX.plusNanos wraps");
        xeq(LocalTime.MIDNIGHT.minusNanos(1).toString(), "23:59:59.999999999", "MIDNIGHT.minusNanos wraps");
        xeq(LocalTime.of(23, 0).plusHours(2).toString(), "01:00", "plusHours wrap");
        xeq(LocalTime.of(1, 0).minusHours(2).toString(), "23:00", "minusHours wrap");
        xeq(LocalTime.of(23, 59).plusMinutes(1).toString(), "00:00", "plusMinutes wrap");
        xeq(Duration.ofMillis(-1).getSeconds() + "/" + Duration.ofMillis(-1).getNano(), "-1/999000000", "ofMillis(-1) normalized");
        xeq(LocalTime.of(10, 15, 30).plusNanos(1500000000L).toString(), "10:15:31.500", "plusNanos 1.5s keeps ms group");
        LocalTime lt = LocalTime.of(10, 15, 30, 123);
        check(LocalTime.ofNanoOfDay(lt.toNanoOfDay()).equals(lt), "nanoOfDay rt");
        check(lt.toNanoOfDay() == 36930000000123L, "nanoOfDay value");
        check(lt.toSecondOfDay() == 36930, "secondOfDay value");
        xeq(lt.truncatedTo(ChronoUnit.HOURS).toString(), "10:00", "trunc HOURS");
        xeq(lt.truncatedTo(ChronoUnit.MINUTES).toString(), "10:15", "trunc MINUTES");
        xeq(lt.truncatedTo(ChronoUnit.SECONDS).toString(), "10:15:30", "trunc SECONDS");
        xeq(lt.truncatedTo(ChronoUnit.NANOS).toString(), "10:15:30.000000123", "trunc NANOS");
        xeq(LocalTime.of(23, 59, 59).truncatedTo(ChronoUnit.HALF_DAYS).toString(), "12:00", "trunc HALF_DAYS pm");
        xeq(LocalTime.of(10, 0).truncatedTo(ChronoUnit.HALF_DAYS).toString(), "00:00", "trunc HALF_DAYS am");
        xeq(lt.truncatedTo(ChronoUnit.DAYS).toString(), "00:00", "trunc DAYS -> midnight");
        xact(() -> lt.truncatedTo(ChronoUnit.FOREVER), UnsupportedTemporalTypeException.class,
            "Unit is too large to be used for truncation", "trunc FOREVER");
        xeq(lt.range(ChronoField.HOUR_OF_DAY).toString(), "0 - 23", "range hour");
        xeq(lt.range(ChronoField.MINUTE_OF_HOUR).toString(), "0 - 59", "range minute");
        xeq(lt.range(ChronoField.NANO_OF_SECOND).toString(), "0 - 999999999", "range nano");
        xact(() -> lt.getLong(ChronoField.DAY_OF_YEAR), UnsupportedTemporalTypeException.class,
            "Unsupported field: DayOfYear", "LT.getLong DOY");
        xact(() -> lt.plus(1, ChronoUnit.DAYS), UnsupportedTemporalTypeException.class,
            "Unsupported unit: Days", "LT.plus DAYS");
        check(!lt.isSupported(ChronoField.DAY_OF_MONTH), "LT not support DOM");
        check(LocalTime.MIDNIGHT.compareTo(LocalTime.NOON) < 0, "compareTo");
        check(lt.isBefore(LocalTime.NOON), "isBefore");
        check(LocalTime.NOON.isAfter(lt), "isAfter");
        xeq(lt.adjustInto(LocalTime.MIDNIGHT).toString(), "10:15:30.000000123", "adjustInto");
        xeq(LocalTime.MIDNIGHT.toString(), "00:00", "MIDNIGHT");
        xeq(LocalTime.NOON.toString(), "12:00", "NOON");
        xeq(LocalTime.MIN.toString(), "00:00", "MIN");
        xeq(LocalTime.MAX.toString(), "23:59:59.999999999", "MAX");
        check(LocalTime.parse("10:15").toString().equals("10:15"), "parse hm");
        check(LocalTime.parse("10:15:30.123456789").toString().equals("10:15:30.123456789"), "parse nano");
        xact(() -> LocalTime.parse("10"), DateTimeParseException.class,
            "Text '10' could not be parsed at index 2", "parse '10'");
        xact(() -> LocalTime.parse("10:15:70"), DateTimeParseException.class,
            "Text '10:15:70' could not be parsed: Invalid value for SecondOfMinute (valid values 0 - 59): 70", "parse sec 70");
        xact(() -> LocalTime.parse(""), DateTimeParseException.class, null, "parse empty");
        xact(() -> LocalTime.parse(null), NullPointerException.class, null, "parse null");
        String[] rts = {"00:00", "10:15", "10:15:30", "23:59:59.999999999", "10:15:30.500"};
        for (String s : rts) check(LocalTime.parse(s).toString().equals(s), "rt " + s);
        check(LocalTime.parse("10:15:30.5").toString().equals("10:15:30.500"), "parse .5 pads to ms group");
        ChronoUnit[] symUnits = {ChronoUnit.NANOS, ChronoUnit.MICROS, ChronoUnit.MILLIS, ChronoUnit.SECONDS, ChronoUnit.MINUTES, ChronoUnit.HOURS, ChronoUnit.HALF_DAYS};
        for (ChronoUnit u : symUnits) check(lt.plus(7, u).minus(7, u).equals(lt), "sym " + u);
        check(lt.query(TemporalQueries.precision()) == ChronoUnit.NANOS, "precision");
        check(lt.query(TemporalQueries.localDate()) == null, "localDate null");
        check(lt.query(TemporalQueries.chronology()) == null, "chronology null");
    }

}
