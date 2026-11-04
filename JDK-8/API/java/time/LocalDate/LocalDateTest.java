/*
 * @test
 * @summary java.time.LocalDate API conformance: full contract coverage.
 *          factories (of/ofYearDay/ofEpochDay/parse/from/now fixed Clock) +
 *          exact exception matrix, null semantics, getters (year/month/day/
 *          dayOfWeek/isLeapYear/lengthOfMonth/lengthOfYear), temporal accessors
 *          (isSupported/range/get/getLong/query/adjustInto), arithmetic
 *          (plus/minus/with all overloads) with immutability and month-end
 *          clamping, until(Period|ChronoUnit), comparisons, conversions
 *          (atTime/atStartOfDay/toEpochDay/format), MIN/MAX boundaries, and
 *          serialization round-trip + serialVersionUID.
 * @run main LocalDateTest
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
import java.time.OffsetTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalQueries;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.time.Year;
import java.time.temporal.JulianFields;

public class LocalDateTest {
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

    static LocalDate roundTrip(LocalDate v) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(v);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (LocalDate) ois.readObject();
    }

    static void testFactories() {
        LocalDate d = LocalDate.of(2024, 2, 29);
        check(d.getYear() == 2024 && d.getMonthValue() == 2 && d.getDayOfMonth() == 29,
                "of(int,int,int)");
        check(LocalDate.of(2024, Month.FEBRUARY, 29).equals(d), "of(int,Month,int)");
        check(LocalDate.ofYearDay(2024, 60).equals(d), "ofYearDay leap 60");
        check(LocalDate.ofYearDay(2023, 60).equals(LocalDate.of(2023, 3, 1)), "ofYearDay non-leap");
        check(LocalDate.ofYearDay(2024, 366).equals(LocalDate.of(2024, 12, 31)), "ofYearDay 366");
        check(LocalDate.ofEpochDay(0).equals(LocalDate.of(1970, 1, 1)), "ofEpochDay 0");
        check(LocalDate.ofEpochDay(-1).equals(LocalDate.of(1969, 12, 31)), "ofEpochDay -1");
        check(LocalDate.parse("2024-02-29").equals(d), "parse");
        check(LocalDate.parse("+999999999-12-31").equals(LocalDate.MAX), "parse MAX");
        check(LocalDate.parse("-999999999-01-01").equals(LocalDate.MIN), "parse MIN");
        check(LocalDate.from(d).equals(d), "from");
        check(LocalDate.from(LocalDateTime.of(2024, 2, 29, 12, 30)).equals(d), "from LDT");
        check(LocalDate.now(Clock.fixed(Instant.ofEpochSecond(0), ZoneOffset.UTC))
                .equals(LocalDate.of(1970, 1, 1)), "now fixed Clock");
        check(LocalDate.now(ZoneId.of("UTC")).isSupported(ChronoField.YEAR), "now ZoneId");
        check(LocalDate.MIN.equals(LocalDate.of(-999999999, 1, 1)), "MIN");
        check(LocalDate.MAX.equals(LocalDate.of(999999999, 12, 31)), "MAX");
        check(d.toString().equals("2024-02-29"), "toString");
    }

    static void testGetters() {
        LocalDate d = LocalDate.of(2024, 2, 29);
        check(d.getMonth() == Month.FEBRUARY, "getMonth");
        check(d.getDayOfYear() == 60, "getDayOfYear");
        check(d.getDayOfWeek() == DayOfWeek.THURSDAY, "getDayOfWeek");
        check(d.isLeapYear(), "isLeapYear 2024");
        check(!LocalDate.of(1900, 1, 1).isLeapYear(), "isLeapYear 1900");
        check(LocalDate.of(2000, 1, 1).isLeapYear(), "isLeapYear 2000");
        check(d.lengthOfMonth() == 29, "lengthOfMonth leap");
        check(LocalDate.of(2023, 2, 28).lengthOfMonth() == 28, "lengthOfMonth non-leap");
        check(d.lengthOfYear() == 366, "lengthOfYear leap");
        check(LocalDate.of(2023, 1, 1).lengthOfYear() == 365, "lengthOfYear non-leap");
        check(d.getChronology() == IsoChronology.INSTANCE, "getChronology");
        check(d.getEra() == IsoChronology.INSTANCE.eraOf(1), "getEra CE");
        check(LocalDate.of(-1, 1, 1).getEra() == IsoChronology.INSTANCE.eraOf(0), "getEra BCE");
    }

    static void testTemporalAccessors() {
        LocalDate d = LocalDate.of(2024, 2, 29);
        check(d.isSupported(ChronoField.DAY_OF_MONTH), "isSupported DAY_OF_MONTH");
        check(d.isSupported(ChronoUnit.DAYS), "isSupported DAYS");
        check(!d.isSupported(ChronoField.SECOND_OF_DAY), "isSupported SECOND_OF_DAY false");
        check(!d.isSupported(ChronoUnit.HOURS), "isSupported HOURS false");
        ValueRange yr = d.range(ChronoField.YEAR);
        check(yr.getMinimum() == -999999999 && yr.getMaximum() == 999999999, "range YEAR");
        check(d.range(ChronoField.MONTH_OF_YEAR).getMaximum() == 12, "range MONTH_OF_YEAR");
        check(d.range(ChronoField.DAY_OF_MONTH).getMaximum() == 29, "range DAY_OF_MONTH");
        check(d.get(ChronoField.YEAR) == 2024, "get YEAR");
        check(d.get(ChronoField.MONTH_OF_YEAR) == 2, "get MONTH_OF_YEAR");
        check(d.get(ChronoField.DAY_OF_MONTH) == 29, "get DAY_OF_MONTH");
        check(d.getLong(ChronoField.EPOCH_DAY) == d.toEpochDay(), "getLong EPOCH_DAY");
        check(d.getLong(ChronoField.PROLEPTIC_MONTH) == 2024L * 12 + 1, "getLong PROLEPTIC_MONTH");
        check(d.query(TemporalQueries.localDate()) == d, "query localDate");
        check(d.query(TemporalQueries.precision()) == ChronoUnit.DAYS, "query precision");
        check(d.query(TemporalQueries.chronology()) == IsoChronology.INSTANCE, "query chronology");
        checkThrows(UnsupportedTemporalTypeException.class, () -> d.get(ChronoField.SECOND_OF_DAY),
                "get SECOND_OF_DAY");
        checkThrows(UnsupportedTemporalTypeException.class, () -> d.getLong(ChronoField.SECOND_OF_DAY),
                "getLong SECOND_OF_DAY");
        checkThrows(UnsupportedTemporalTypeException.class, () -> d.range(ChronoField.SECOND_OF_DAY),
                "range SECOND_OF_DAY");
    }

    static void testArithmeticImmutability() {
        LocalDate base = LocalDate.of(2024, 1, 31);
        check(base.plusDays(1).equals(LocalDate.of(2024, 2, 1)), "plusDays");
        check(base.plusMonths(1).equals(LocalDate.of(2024, 2, 29)), "plusMonths clamps");
        check(base.plusYears(1).equals(LocalDate.of(2025, 1, 31)), "plusYears");
        check(base.plusWeeks(1).equals(LocalDate.of(2024, 2, 7)), "plusWeeks");
        check(base.plus(Period.ofMonths(1)).equals(LocalDate.of(2024, 2, 29)), "plus Period");
        check(base.plus(2, ChronoUnit.DAYS).equals(LocalDate.of(2024, 2, 2)), "plus unit DAYS");
        check(base.plus(1, ChronoUnit.MONTHS).equals(LocalDate.of(2024, 2, 29)), "plus unit MONTHS");
        check(base.plus(1, ChronoUnit.DECADES).equals(LocalDate.of(2034, 1, 31)), "plus unit DECADES");
        check(base.plus(-1, ChronoUnit.DAYS).equals(LocalDate.of(2024, 1, 30)), "plus negative");
        check(base.minusDays(1).equals(LocalDate.of(2024, 1, 30)), "minusDays");
        check(base.minusMonths(1).equals(LocalDate.of(2023, 12, 31)), "minusMonths");
        check(base.minusYears(1).equals(LocalDate.of(2023, 1, 31)), "minusYears");
        check(base.minusWeeks(1).equals(LocalDate.of(2024, 1, 24)), "minusWeeks");
        check(base.minus(Period.ofDays(1)).equals(LocalDate.of(2024, 1, 30)), "minus Period");
        check(base.withYear(2023).equals(LocalDate.of(2023, 1, 31)), "withYear");
        check(base.withMonth(2).equals(LocalDate.of(2024, 2, 29)), "withMonth clamps");
        check(base.withDayOfMonth(15).equals(LocalDate.of(2024, 1, 15)), "withDayOfMonth");
        check(base.withDayOfYear(60).equals(LocalDate.of(2024, 2, 29)), "withDayOfYear");
        check(base.with(ChronoField.YEAR, 2025).equals(LocalDate.of(2025, 1, 31)), "with field YEAR");
        check(base.with(ChronoField.DAY_OF_MONTH, 1).equals(LocalDate.of(2024, 1, 1)), "with field DAY");
        check(base.with(TemporalAdjusters.firstDayOfMonth()).equals(LocalDate.of(2024, 1, 1)),
                "with adjuster firstDayOfMonth");
        check(base.with(TemporalAdjusters.lastDayOfYear()).equals(LocalDate.of(2024, 12, 31)),
                "with adjuster lastDayOfYear");
        check(base.equals(LocalDate.of(2024, 1, 31)), "immutability");
        // adjustInto
        check(base.adjustInto(LocalDateTime.of(2020, 6, 6, 12, 30))
                .equals(LocalDateTime.of(2024, 1, 31, 12, 30)), "adjustInto LDT");
    }

    static void testUntil() {
        LocalDate start = LocalDate.of(2024, 1, 15);
        LocalDate end = LocalDate.of(2024, 3, 20);
        Period p = start.until(end);
        check(p.getYears() == 0 && p.getMonths() == 2 && p.getDays() == 5, "until Period");
        check(start.until(end, ChronoUnit.DAYS) == 65, "until DAYS");
        check(start.until(end, ChronoUnit.MONTHS) == 2, "until MONTHS");
        check(end.until(start, ChronoUnit.DAYS) == -65, "until negative");
        check(start.until(start, ChronoUnit.DAYS) == 0, "until same");
        check(start.until(end, ChronoUnit.YEARS) == 0, "until YEARS");
    }

    static void testComparison() {
        LocalDate a = LocalDate.of(2024, 3, 1);
        LocalDate b = LocalDate.of(2024, 3, 2);
        LocalDate a2 = LocalDate.of(2024, 3, 1);
        check(a.isBefore(b) && b.isAfter(a), "isBefore/isAfter");
        check(a.isEqual(a2) && !a.isAfter(a2) && !a.isBefore(a2), "isEqual");
        check(a.compareTo(b) < 0 && b.compareTo(a) > 0 && a.compareTo(a2) == 0, "compareTo");
        check(a.equals(a2) && !a.equals(b), "equals");
        check(a.hashCode() == a2.hashCode(), "hashCode");
        check(a.equals(null) == false, "equals null");
        check(a.equals("2024-03-01") == false, "equals other type");
        checkThrows(NullPointerException.class, () -> a.compareTo(null), "compareTo null");
    }

    static void testConversions() {
        LocalDate d = LocalDate.of(2024, 3, 1);
        check(d.atTime(LocalTime.of(12, 30)).equals(LocalDateTime.of(2024, 3, 1, 12, 30)),
                "atTime(LocalTime)");
        check(d.atTime(10, 15).equals(LocalDateTime.of(2024, 3, 1, 10, 15)), "atTime(int,int)");
        check(d.atTime(10, 15, 20).equals(LocalDateTime.of(2024, 3, 1, 10, 15, 20)),
                "atTime(int,int,int)");
        check(d.atTime(10, 15, 20, 30).equals(LocalDateTime.of(2024, 3, 1, 10, 15, 20, 30)),
                "atTime(int,int,int,int)");
        check(d.atTime(OffsetTime.of(10, 15, 0, 0, ZoneOffset.ofHours(1))).getOffset()
                .equals(ZoneOffset.ofHours(1)), "atTime(OffsetTime)");
        check(d.atStartOfDay().equals(LocalDateTime.of(2024, 3, 1, 0, 0)), "atStartOfDay");
        check(d.atStartOfDay(ZoneOffset.UTC).getOffset().equals(ZoneOffset.UTC), "atStartOfDay zone");
        check(d.toEpochDay() == LocalDate.of(1970, 1, 1).until(d, ChronoUnit.DAYS), "toEpochDay");
        check(LocalDate.MIN.toEpochDay() == -365243219162L, "toEpochDay MIN");
        check(d.format(DateTimeFormatter.ISO_LOCAL_DATE).equals("2024-03-01"), "format");
        check(d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).equals("01/03/2024"),
                "format pattern");
    }

    static void testSerialization() throws Exception {
        check(ObjectStreamClass.lookup(LocalDate.class).getSerialVersionUID() == 2942565459149668126L,
                "serialVersionUID");
        LocalDate[] vals = { LocalDate.MIN, LocalDate.MAX, LocalDate.of(1970, 1, 1),
                LocalDate.of(2024, 2, 29), LocalDate.of(-1, 1, 1) };
        for (LocalDate v : vals) {
            LocalDate r = roundTrip(v);
            check(r.equals(v), "serialization round-trip " + v);
            check(r != v, "serialization new instance " + v);
        }
    }

    static void testErrors() {
        checkThrows(DateTimeException.class, () -> LocalDate.of(2024, 2, 30), "of Feb 30");
        checkThrows(DateTimeException.class, () -> LocalDate.of(2023, 2, 29), "of Feb 29 non-leap");
        checkThrows(DateTimeException.class, () -> LocalDate.of(2024, 13, 1), "of month 13");
        checkThrows(DateTimeException.class, () -> LocalDate.of(2024, 0, 1), "of month 0");
        checkThrows(DateTimeException.class, () -> LocalDate.of(2024, 1, 32), "of day 32");
        checkThrows(DateTimeException.class, () -> LocalDate.of(2024, 1, 0), "of day 0");
        checkThrows(DateTimeException.class, () -> LocalDate.of(1000000000, 1, 1), "of year overflow");
        checkThrows(DateTimeException.class, () -> LocalDate.ofYearDay(2023, 366), "ofYearDay 366 non-leap");
        checkThrows(DateTimeException.class, () -> LocalDate.ofYearDay(2024, 0), "ofYearDay 0");
        checkThrows(DateTimeException.class, () -> LocalDate.ofEpochDay(Long.MAX_VALUE),
                "ofEpochDay overflow");
        checkThrows(DateTimeParseException.class, () -> LocalDate.parse("2024-02-30"), "parse Feb 30");
        checkThrows(DateTimeParseException.class, () -> LocalDate.parse("2024/03/01"), "parse bad format");
        checkThrows(DateTimeParseException.class, () -> LocalDate.parse("garbage"), "parse garbage");
        checkThrows(DateTimeException.class, () -> LocalDate.of(2024, 1, 15).plusYears(Long.MAX_VALUE),
                "plusYears overflow");
        checkThrows(DateTimeException.class, () -> LocalDate.of(2024, 1, 15).withYear(Integer.MAX_VALUE),
                "withYear overflow");
        checkThrows(UnsupportedTemporalTypeException.class,
                () -> LocalDate.of(2024, 1, 15).plus(1, ChronoUnit.HOURS), "plus HOURS");
        checkThrows(UnsupportedTemporalTypeException.class,
                () -> LocalDate.of(2024, 1, 15).with(ChronoField.SECOND_OF_DAY, 100), "with SECOND_OF_DAY");
    }

    static void testNullSemantics() {
        checkThrows(NullPointerException.class, () -> LocalDate.of(2024, (Month) null, 1), "of Month null");
        checkThrows(NullPointerException.class, () -> LocalDate.parse(null), "parse null");
        checkThrows(NullPointerException.class, () -> LocalDate.from(null), "from null");
        checkThrows(NullPointerException.class, () -> LocalDate.now((Clock) null), "now Clock null");
        checkThrows(NullPointerException.class, () -> LocalDate.now((ZoneId) null), "now ZoneId null");
        LocalDate d = LocalDate.of(2024, 1, 1);
        checkThrows(NullPointerException.class, () -> d.plus(null), "plus TemporalAmount null");
        checkThrows(NullPointerException.class, () -> d.plus(1, null), "plus unit null");
        checkThrows(NullPointerException.class, () -> d.minus(null), "minus TemporalAmount null");
        checkThrows(NullPointerException.class, () -> d.with((java.time.temporal.TemporalAdjuster) null),
                "with adjuster null");
        checkThrows(NullPointerException.class, () -> d.with((java.time.temporal.TemporalField) null, 1),
                "with field null");
        checkThrows(NullPointerException.class, () -> d.until(null), "until null");
        checkThrows(NullPointerException.class, () -> d.isAfter(null), "isAfter null");
        checkThrows(NullPointerException.class, () -> d.isBefore(null), "isBefore null");
        checkThrows(NullPointerException.class, () -> d.isEqual(null), "isEqual null");
        checkThrows(NullPointerException.class, () -> d.atTime((LocalTime) null), "atTime LocalTime null");
        checkThrows(NullPointerException.class, () -> d.atStartOfDay((ZoneId) null), "atStartOfDay null");
        checkThrows(NullPointerException.class, () -> d.format(null), "format null");
        checkThrows(NullPointerException.class, () -> d.query(null), "query null");
    }

    public static void main(String[] args) throws Exception {
        testFactories();
        testGetters();
        testTemporalAccessors();
        testArithmeticImmutability();
        testUntil();
        testComparison();
        testConversions();
        testSerialization();
        testErrors();
        testNullSemantics();
        testDeepOfMatrix();
        testDeepClampAndWith();
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


    // ===== deepening: of() boundary matrix + exact DTE messages (probe-locked) =====
    static void testDeepOfMatrix() {
        check(LocalDate.of(1, 1, 1).toString().equals("0001-01-01"), "of min-ish");
        check(LocalDate.of(0, 1, 1).toString().equals("0000-01-01"), "year 0 legal ISO");
        check(LocalDate.of(-44, 1, 1).toString().equals("-0044-01-01"), "negative year padded");
        check(LocalDate.of(999, 12, 31).toString().equals("0999-12-31"), "3-digit year padded");
        check(LocalDate.of(999999999, 12, 31).toString().equals("+999999999-12-31"), "MAX year");
        check(LocalDate.of(-999999999, 1, 1).toString().equals("-999999999-01-01"), "MIN year");
        int[] dom = {31,29,31,30,31,30,31,31,30,31,30,31};
        String[] mn = {"JANUARY","FEBRUARY","MARCH","APRIL","MAY","JUNE","JULY","AUGUST","SEPTEMBER","OCTOBER","NOVEMBER","DECEMBER"};
        for (int m = 1; m <= 12; m++) {
            final int mm = m, lim = dom[m-1];
            check(LocalDate.of(2024, mm, lim).getMonthValue() == mm, "last day of month " + mm);
            if (lim + 1 > 31)
                xact(() -> LocalDate.of(2024, mm, lim + 1), DateTimeException.class,
                    "Invalid value for DayOfMonth (valid values 1 - 28/31): 32", "dom>31 m=" + mm);
            else
                xact(() -> LocalDate.of(2024, mm, lim + 1), DateTimeException.class,
                    "Invalid date '" + mn[mm-1] + " " + (lim + 1) + "'", "dom invalid m=" + mm);
        }
        check(!Year.isLeap(1900) && Year.isLeap(2000) && Year.isLeap(2024) && !Year.isLeap(2100), "leap matrix");
        xact(() -> LocalDate.of(1900, 2, 29), DateTimeException.class,
            "Invalid date 'February 29' as '1900' is not a leap year", "1900 non-leap");
        xact(() -> LocalDate.of(2100, 2, 29), DateTimeException.class,
            "Invalid date 'February 29' as '2100' is not a leap year", "2100 non-leap");
        xact(() -> LocalDate.of(2024, 2, 30), DateTimeException.class, "Invalid date 'FEBRUARY 30'", "Feb 30");
        xact(() -> LocalDate.of(2024, 4, 31), DateTimeException.class, "Invalid date 'APRIL 31'", "Apr 31");
        xact(() -> LocalDate.of(2024, 13, 1), DateTimeException.class,
            "Invalid value for MonthOfYear (valid values 1 - 12): 13", "month 13");
        xact(() -> LocalDate.of(2024, 0, 1), DateTimeException.class,
            "Invalid value for MonthOfYear (valid values 1 - 12): 0", "month 0");
        xact(() -> LocalDate.of(2024, 1, 0), DateTimeException.class,
            "Invalid value for DayOfMonth (valid values 1 - 28/31): 0", "day 0");
        xact(() -> LocalDate.of(999999999 + 1, 1, 1), DateTimeException.class,
            "Invalid value for Year (valid values -999999999 - 999999999): 1000000000", "year overflow");
        xact(() -> LocalDate.ofYearDay(2024, 0), DateTimeException.class,
            "Invalid value for DayOfYear (valid values 1 - 365/366): 0", "doy 0");
        xact(() -> LocalDate.ofYearDay(2024, 367), DateTimeException.class,
            "Invalid value for DayOfYear (valid values 1 - 365/366): 367", "doy 367 leap");
        xact(() -> LocalDate.ofYearDay(2023, 366), DateTimeException.class,
            "Invalid date 'DayOfYear 366' as '2023' is not a leap year", "doy 366 non-leap");
        check(LocalDate.ofYearDay(2024, 366).toString().equals("2024-12-31"), "doy 366");
        check(LocalDate.ofYearDay(2024, 60).toString().equals("2024-02-29"), "doy 60 leap");
        check(LocalDate.ofYearDay(2023, 60).toString().equals("2023-03-01"), "doy 60 non-leap");
        xact(() -> LocalDate.of(1, (Month) null, 1), NullPointerException.class, null, "null month");
    }


    // ===== deepening: month-end clamping, with* semantics, until matrix, adjusters =====
    static void testDeepClampAndWith() {
        xeq(LocalDate.of(2024, 1, 31).plusMonths(1).toString(), "2024-02-29", "clamp leap");
        xeq(LocalDate.of(2023, 1, 31).plusMonths(1).toString(), "2023-02-28", "clamp non-leap");
        xeq(LocalDate.of(2024, 3, 31).minusMonths(1).toString(), "2024-02-29", "clamp minus leap");
        xeq(LocalDate.of(2024, 2, 29).plusYears(1).toString(), "2025-02-28", "plusYears clamp");
        xeq(LocalDate.of(2024, 2, 29).plus(Period.ofYears(1)).toString(), "2025-02-28", "plus P1Y clamp");
        xeq(LocalDate.of(2024, 2, 29).withYear(2023).toString(), "2023-02-28", "withYear clamps silently");
        xeq(LocalDate.of(2024, 2, 29).with(ChronoField.YEAR, 2023).toString(), "2023-02-28", "with(YEAR) clamps");
        xeq(LocalDate.of(2024, 1, 30).withMonth(2).toString(), "2024-02-29", "withMonth clamps leap");
        xeq(LocalDate.of(2023, 1, 30).withMonth(2).toString(), "2023-02-28", "withMonth clamps non-leap");
        xeq(LocalDate.of(2024, 3, 31).withDayOfMonth(30).toString(), "2024-03-30", "withDayOfMonth simple");
        xact(() -> LocalDate.of(2024, 4, 10).withDayOfMonth(31), DateTimeException.class,
            "Invalid date 'APRIL 31'", "withDayOfMonth 31 April");
        xact(() -> LocalDate.of(2024, 1, 1).withDayOfYear(367), DateTimeException.class,
            "Invalid value for DayOfYear (valid values 1 - 365/366): 367", "withDayOfYear 367");
        xeq(LocalDate.of(2024, 2, 29).withDayOfYear(60).toString(), "2024-02-29", "withDayOfYear 60");
        xact(() -> LocalDate.MAX.plusDays(1), DateTimeException.class,
            "Invalid value for Year (valid values -999999999 - 999999999): 1000000000", "MAX.plusDays");
         xact(() -> LocalDate.MIN.minusDays(1), DateTimeException.class,
             "Invalid value for Year (valid values -999999999 - 999999999): -1000000000", "MIN.minusDays JDK8");
        LocalDate base = LocalDate.of(2024, 2, 29);
        Period[] ps = {Period.of(0, 13, 0), Period.of(0, 0, 400), Period.of(0, 0, 1461)};
        for (int i = 0; i < ps.length; i++) {
            check(base.plus(ps[i]).minus(ps[i]).equals(base), "period sym " + i);
            check(base.minus(ps[i]).plus(ps[i]).equals(base), "period sym rev " + i);
        }
        // Feb-29 + 1 year clamps and is LOSSY: round-trip does not return to Feb-29
        xeq(base.plus(Period.ofYears(1)).toString(), "2025-02-28", "Feb29 plus 1Y clamps");
        xeq(base.plus(Period.ofYears(1)).minus(Period.ofYears(1)).toString(), "2024-02-28", "Feb29 +1Y-1Y lossy");
        check(LocalDate.of(2024, 2, 28).plus(Period.ofYears(1)).minus(Period.ofYears(1)).equals(LocalDate.of(2024, 2, 28)), "Feb28 year sym");
        ChronoUnit[] units = {ChronoUnit.DAYS, ChronoUnit.WEEKS, ChronoUnit.MONTHS, ChronoUnit.YEARS, ChronoUnit.DECADES, ChronoUnit.CENTURIES, ChronoUnit.MILLENNIA};
        for (ChronoUnit u : units)
            check(LocalDate.of(2024, 2, 28).plus(5, u).minus(5, u).equals(LocalDate.of(2024, 2, 28)), "unit sym " + u);
        xeq(base.plus(5, ChronoUnit.YEARS).toString(), "2029-02-28", "Feb29 +5 YEARS clamps");
        xeq(base.plus(5, ChronoUnit.DECADES).toString(), "2074-02-28", "Feb29 +5 DECADES clamps");
        check(LocalDate.of(2024, 2, 28).until(LocalDate.of(2024, 3, 1), ChronoUnit.DAYS) == 2, "until DAYS leap");
        check(LocalDate.of(2023, 2, 28).until(LocalDate.of(2023, 3, 1), ChronoUnit.DAYS) == 1, "until DAYS non-leap");
        check(LocalDate.of(2024, 1, 31).until(LocalDate.of(2024, 3, 1), ChronoUnit.MONTHS) == 1, "until MONTHS clamp");
        check(LocalDate.of(2024, 1, 31).until(LocalDate.of(2024, 2, 29), ChronoUnit.MONTHS) == 0, "until MONTHS 0");
        check(LocalDate.of(2023, 3, 1).until(LocalDate.of(2024, 2, 29), ChronoUnit.YEARS) == 0, "until YEARS 0");
        check(LocalDate.of(2023, 2, 28).until(LocalDate.of(2024, 2, 28), ChronoUnit.YEARS) == 1, "until YEARS 1");
        check(LocalDate.of(2024, 2, 1).until(LocalDate.of(2024, 2, 29), ChronoUnit.WEEKS) == 4, "until WEEKS");
        check(LocalDate.of(1924, 1, 1).until(LocalDate.of(2024, 1, 1), ChronoUnit.CENTURIES) == 1, "until CENTURIES");
        check(LocalDate.of(24, 1, 1).until(LocalDate.of(2024, 1, 1), ChronoUnit.MILLENNIA) == 2, "until MILLENNIA");
        check(LocalDate.of(2024, 1, 1).until(LocalDate.of(2025, 1, 1), ChronoUnit.ERAS) == 0, "until ERAS 0");
        check(LocalDate.of(2024, 1, 1).until(LocalDate.of(2024, 1, 1), ChronoUnit.DAYS) == 0, "until self 0");
        check(LocalDate.of(2024, 1, 1).until(LocalDate.of(2025, 6, 15), ChronoUnit.MONTHS) == 17, "until MONTHS 17");
        LocalDate b = LocalDate.of(2024, 2, 29);
        check(b.getDayOfWeek() == DayOfWeek.THURSDAY, "base dow");
        xeq(b.with(TemporalAdjusters.firstDayOfMonth()).toString(), "2024-02-01", "firstDayOfMonth");
        xeq(b.with(TemporalAdjusters.lastDayOfMonth()).toString(), "2024-02-29", "lastDayOfMonth");
        xeq(b.with(TemporalAdjusters.firstDayOfNextMonth()).toString(), "2024-03-01", "firstDayOfNextMonth");
        xeq(b.with(TemporalAdjusters.firstDayOfYear()).toString(), "2024-01-01", "firstDayOfYear");
        xeq(b.with(TemporalAdjusters.lastDayOfYear()).toString(), "2024-12-31", "lastDayOfYear");
        xeq(b.with(TemporalAdjusters.firstDayOfNextYear()).toString(), "2025-01-01", "firstDayOfNextYear");
        xeq(b.with(TemporalAdjusters.firstInMonth(DayOfWeek.THURSDAY)).toString(), "2024-02-01", "firstInMonth THU");
        xeq(b.with(TemporalAdjusters.lastInMonth(DayOfWeek.WEDNESDAY)).toString(), "2024-02-28", "lastInMonth WED");
        xeq(b.with(TemporalAdjusters.dayOfWeekInMonth(2, DayOfWeek.SATURDAY)).toString(), "2024-02-10", "dowInMonth 2 SAT");
        xeq(b.with(TemporalAdjusters.dayOfWeekInMonth(-1, DayOfWeek.SUNDAY)).toString(), "2024-02-25", "dowInMonth -1 SUN");
        xeq(b.with(TemporalAdjusters.dayOfWeekInMonth(0, DayOfWeek.MONDAY)).toString(), "2024-01-29", "dowInMonth 0 JAN-29");
        xeq(b.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).toString(), "2024-03-04", "next MON");
        xeq(b.with(TemporalAdjusters.nextOrSame(DayOfWeek.THURSDAY)).toString(), "2024-02-29", "nextOrSame THU");
        xeq(b.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY)).toString(), "2024-02-23", "previous FRI");
        xeq(b.with(TemporalAdjusters.previousOrSame(DayOfWeek.THURSDAY)).toString(), "2024-02-29", "previousOrSame THU");
        xact(() -> TemporalAdjusters.dayOfWeekInMonth(1, null), NullPointerException.class, null, "dowInMonth null");
        xact(() -> TemporalAdjusters.next(null), NullPointerException.class, null, "next null");
        xact(() -> TemporalAdjusters.previousOrSame(null), NullPointerException.class, null, "previousOrSame null");
        xact(() -> TemporalAdjusters.ofDateAdjuster(null), NullPointerException.class, null, "ofDateAdjuster null");
        LocalDate[] samples = {LocalDate.of(2024,2,29), LocalDate.MIN, LocalDate.of(0,1,1), LocalDate.of(1970,1,1), LocalDate.MAX};
        for (LocalDate d : samples) {
            check(LocalDate.ofEpochDay(d.toEpochDay()).equals(d), "epochDay rt " + d);
            check(LocalDate.parse(d.toString()).equals(d), "toString/parse rt " + d);
        }
        check(LocalDate.ofEpochDay(0).toString().equals("1970-01-01"), "epochDay 0");
        check(LocalDate.ofEpochDay(19889).toString().equals("2024-06-15"), "epochDay 19889");
        xeq(LocalDate.of(2024, 2, 29).range(ChronoField.DAY_OF_MONTH).toString(), "1 - 29", "range DOM Feb leap");
        xeq(LocalDate.of(2023, 2, 21).range(ChronoField.DAY_OF_MONTH).toString(), "1 - 28", "range DOM Feb");
        xeq(LocalDate.of(2024, 1, 1).range(ChronoField.DAY_OF_MONTH).toString(), "1 - 31", "range DOM Jan");
        xeq(LocalDate.of(2024, 1, 1).range(ChronoField.DAY_OF_YEAR).toString(), "1 - 366", "range DOY leap");
        xeq(LocalDate.of(2023, 1, 1).range(ChronoField.DAY_OF_YEAR).toString(), "1 - 365", "range DOY");
        xact(() -> LocalDate.of(2024, 1, 1).getLong(ChronoField.NANO_OF_DAY), UnsupportedTemporalTypeException.class,
            "Unsupported field: NanoOfDay", "LD.getLong NANO_OF_DAY");
        xact(() -> LocalDate.of(2024, 1, 1).getLong(ChronoField.HOUR_OF_DAY), UnsupportedTemporalTypeException.class,
            "Unsupported field: HourOfDay", "LD.getLong HOUR");
        xact(() -> LocalDate.of(2024, 1, 1).plus(1, ChronoUnit.HOURS), UnsupportedTemporalTypeException.class,
            "Unsupported unit: Hours", "LD.plus hours");
        check(!LocalDate.of(2024, 1, 1).isSupported(ChronoUnit.HOURS), "LD not support HOURS");
        check(LocalDate.of(2024, 1, 1).isSupported(ChronoUnit.DAYS), "LD support DAYS");
        LocalDate jd = LocalDate.of(2024, 2, 29);
        check(jd.getLong(JulianFields.JULIAN_DAY) == 2460370L, "JULIAN_DAY value");
        check(jd.getLong(JulianFields.MODIFIED_JULIAN_DAY) == 60369L, "MJD value");
        check(jd.getLong(JulianFields.RATA_DIE) == 738945L, "RATA_DIE value");
        check(LocalDate.from(jd.with(JulianFields.JULIAN_DAY, 2460370L)).equals(jd), "julian rt");
        check(LocalDate.parse("2024-02-29").hashCode() == LocalDate.of(2024, 2, 29).hashCode(), "hash parse/of");
        check(LocalDate.of(2024, 2, 29).compareTo(LocalDate.of(2024, 3, 1)) < 0, "compareTo month");
        check(LocalDate.of(-1, 1, 1).compareTo(LocalDate.of(1, 1, 1)) < 0, "compareTo year sign");
        check(LocalDate.of(2024, 2, 29).equals(LocalDate.ofEpochDay(LocalDate.of(2024, 2, 29).toEpochDay())), "equals epoch");
    }

}
