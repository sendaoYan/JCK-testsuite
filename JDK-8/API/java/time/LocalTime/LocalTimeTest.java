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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
