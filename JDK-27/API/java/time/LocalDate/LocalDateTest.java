/*
 * @test
 * @summary java.time.LocalDate conformance: white-box shape + black-box factories/parse,
 *          arithmetic, comparisons, conversions, and error contracts.
 * @run main LocalDateTest
 *
 * 设计案（黑盒）：
 * 1. 契约：LocalDate 是 ISO-8601 无时区日期，值对象（不可变、线程安全），范围 -999999999-01-01..+999999999-12-31。
 * 2. 场景：用户用 of/ofYearDay/ofEpochDay/parse 构造，再链式 plus/minus/with 做日历运算，最后 atTime/atStartOfDay 转带时间类型。
 * 3. 组合链路：of -> plusDays/plusMonths/plusYears -> withDayOfYear -> atTime -> isBefore/isAfter/isEqual/compareTo -> until/datesUntil。
 * 4. 不变量：每次 plus/minus/with 返回新实例且原实例不变；equals/hashCode 按年月日；toString 为 ISO 字符串。
 * 5. 边界/异常：of(2,30) 等非法日期抛 DateTimeException；parse 非法字符串抛 DateTimeParseException；月尾加月做 day 钳制。
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Modifier;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
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
import java.util.List;
import java.util.stream.Collectors;

public class LocalDateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> t, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable e) {
            if (t.isInstance(e)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + t.getSimpleName()
                    + " but got " + e.getClass().getSimpleName(), e);
        }
        throw new AssertionError(msg + ": expected " + t.getSimpleName() + " but nothing thrown");
    }

    static LocalDate roundTrip(LocalDate v) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(v);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (LocalDate) ois.readObject();
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.LocalDate");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == Object.class, "superclass Object");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(java.time.temporal.Temporal.class.isAssignableFrom(c), "Temporal");
        check(java.time.temporal.TemporalAdjuster.class.isAssignableFrom(c), "TemporalAdjuster");
        check(java.time.chrono.ChronoLocalDate.class.isAssignableFrom(c), "ChronoLocalDate");
    }

    static void testBlackBox() throws Exception {
        testFactories();
        testGetters();
        testTemporalAccessors();
        testArithmeticImmutability();
        testComparison();
        testConversions();
        testPeriodsAndStreams();
        testErrors();
        testSerialization();
        testNullSemantics();

        // ---- 边界值/等价类/等价性（本棒补测） ----
        // round-trip：parse(toString(x)) == x（MIN/MAX/负年份）
        check(LocalDate.parse(LocalDate.MIN.toString()).equals(LocalDate.MIN), "round-trip MIN");
        check(LocalDate.parse(LocalDate.MAX.toString()).equals(LocalDate.MAX), "round-trip MAX");
        check(LocalDate.parse(LocalDate.of(-1, 1, 1).toString()).equals(LocalDate.of(-1, 1, 1)),
                "round-trip negative year");
        check(LocalDate.parse(LocalDate.of(0, 1, 1).toString()).equals(LocalDate.of(0, 1, 1)),
                "round-trip year zero");

        // 极值邻域：MIN/+1 与 MAX/-1
        check(LocalDate.MIN.plusDays(1).equals(LocalDate.of(-999999999, 1, 2)), "MIN plusDays(1)");
        check(LocalDate.MAX.minusDays(1).equals(LocalDate.of(999999999, 12, 30)), "MAX minusDays(1)");

        // epoch 单调性
        check(LocalDate.MIN.toEpochDay() + 1 == LocalDate.MIN.plusDays(1).toEpochDay(),
                "toEpochDay monotonic at MIN");

        // 闰日加一年钳制到非闰年 2/28
        check(LocalDate.of(2024, 2, 29).plusYears(1).equals(LocalDate.of(2025, 2, 28)),
                "leap day plusYears clamps");

        // compareTo 反对称
        LocalDate ca = LocalDate.of(2024, 3, 1);
        LocalDate cb = LocalDate.of(2024, 3, 2);
        check(ca.compareTo(cb) == -cb.compareTo(ca), "compareTo anti-symmetry");
    }

    static void testFactories() {
        LocalDate d = LocalDate.of(2024, 2, 29);
        check(d.getYear() == 2024 && d.getMonthValue() == 2 && d.getDayOfMonth() == 29, "of/getters");
        check(d.getMonth() == Month.FEBRUARY, "getMonth");
        check(LocalDate.of(2024, Month.FEBRUARY, 29).equals(d), "of(int,Month,int)");
        check(LocalDate.ofYearDay(2024, 60).equals(d), "ofYearDay leap day=60");
        check(LocalDate.ofYearDay(2023, 60).equals(LocalDate.of(2023, 3, 1)), "ofYearDay non-leap");
        check(LocalDate.ofEpochDay(0).equals(LocalDate.of(1970, 1, 1)), "ofEpochDay epoch");
        check(LocalDate.ofEpochDay(1).equals(LocalDate.of(1970, 1, 2)), "ofEpochDay +1");
        check(LocalDate.parse("2024-02-29").equals(d), "parse ISO");
        check(d.toString().equals("2024-02-29"), "toString");
        check(LocalDate.EPOCH.equals(LocalDate.of(1970, 1, 1)), "EPOCH constant");
        check(LocalDate.MIN.equals(LocalDate.of(-999999999, 1, 1)), "MIN constant");
        check(LocalDate.MAX.equals(LocalDate.of(999999999, 12, 31)), "MAX constant");
    }

    static void testGetters() {
        LocalDate d = LocalDate.of(2024, 2, 29);
        check(d.getDayOfYear() == 60, "getDayOfYear");
        check(d.getDayOfWeek() == DayOfWeek.THURSDAY, "getDayOfWeek");
        check(d.isLeapYear(), "isLeapYear");
        check(!LocalDate.of(2023, 2, 28).isLeapYear(), "isLeapYear false");
        check(LocalDate.of(1900, 1, 1).isLeapYear() == false, "century not leap");
        check(LocalDate.of(2000, 1, 1).isLeapYear() == true, "century divisible by 400 leap");
        check(d.lengthOfMonth() == 29, "lengthOfMonth leap");
        check(d.lengthOfYear() == 366, "lengthOfYear leap");
        check(LocalDate.of(2023, 1, 1).lengthOfYear() == 365, "lengthOfYear non-leap");
    }

    static void testArithmeticImmutability() {
        LocalDate base = LocalDate.of(2024, 1, 31);
        LocalDate p1d = base.plusDays(1);
        LocalDate p1m = base.plusMonths(1);
        LocalDate p1y = base.plusYears(1);
        LocalDate p1w = base.plusWeeks(1);
        check(p1d.equals(LocalDate.of(2024, 2, 1)), "plusDays");
        check(p1m.equals(LocalDate.of(2024, 2, 29)), "plusMonths clamps to month end");
        check(p1y.equals(LocalDate.of(2025, 1, 31)), "plusYears");
        check(p1w.equals(LocalDate.of(2024, 2, 7)), "plusWeeks");
        check(base.equals(LocalDate.of(2024, 1, 31)), "immutability after plus");
        check(base.minusDays(1).equals(LocalDate.of(2024, 1, 30)), "minusDays");
        check(base.minusMonths(1).equals(LocalDate.of(2023, 12, 31)), "minusMonths");
        check(base.minusYears(1).equals(LocalDate.of(2023, 1, 31)), "minusYears");
        check(base.minusWeeks(1).equals(LocalDate.of(2024, 1, 24)), "minusWeeks");
        check(base.withDayOfMonth(15).equals(LocalDate.of(2024, 1, 15)), "withDayOfMonth");
        check(base.withMonth(2).equals(LocalDate.of(2024, 2, 29)), "withMonth clamps");
        check(base.withYear(2023).equals(LocalDate.of(2023, 1, 31)), "withYear");
        check(base.withDayOfYear(60).equals(LocalDate.of(2024, 2, 29)), "withDayOfYear leap");
        check(base.with(TemporalAdjusters.firstDayOfMonth()).equals(LocalDate.of(2024, 1, 1)),
                "with(TemporalAdjusters.firstDayOfMonth)");
        check(base.equals(LocalDate.of(2024, 1, 31)), "immutability after with");
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
        check(a.equals("not a date") == false, "equals other type");
    }

    static void testConversions() {
        LocalDate d = LocalDate.of(2024, 3, 1);
        LocalDateTime ldt = d.atTime(LocalTime.of(12, 30));
        check(ldt.equals(LocalDateTime.of(2024, 3, 1, 12, 30)), "atTime(LocalTime)");
        check(d.atTime(10, 15).equals(LocalDateTime.of(2024, 3, 1, 10, 15)), "atTime(int,int)");
        check(d.atTime(10, 15, 20, 30).equals(LocalDateTime.of(2024, 3, 1, 10, 15, 20, 30)),
                "atTime(int,int,int,int)");
        check(d.atStartOfDay().equals(LocalDateTime.of(2024, 3, 1, 0, 0)), "atStartOfDay");
        check(d.atStartOfDay(ZoneOffset.UTC).getOffset().equals(ZoneOffset.UTC),
                "atStartOfDay(zone) keeps zone");
        check(d.toEpochDay() == LocalDate.of(1970, 1, 1).until(d, ChronoUnit.DAYS), "toEpochDay");
    }

    static void testPeriodsAndStreams() {
        LocalDate start = LocalDate.of(2024, 1, 15);
        LocalDate end = LocalDate.of(2024, 3, 20);
        Period p = start.until(end);
        check(p.getYears() == 0 && p.getMonths() == 2 && p.getDays() == 5, "until Period");
        check(start.until(end, ChronoUnit.DAYS) == 65, "until DAYS");
        check(start.until(end, ChronoUnit.MONTHS) == 2, "until MONTHS");
        List<LocalDate> dates = start.datesUntil(end).collect(Collectors.toList());
        check(dates.size() == 65, "datesUntil count");
        check(dates.get(0).equals(start) && dates.get(64).equals(LocalDate.of(2024, 3, 19)),
                "datesUntil inclusive/exclusive");
        List<LocalDate> step2 = start.datesUntil(end, Period.ofDays(2)).collect(Collectors.toList());
        check(step2.size() == 33 && step2.get(1).equals(LocalDate.of(2024, 1, 17)),
                "datesUntil with Period step");
    }

    static void testErrors() {
        expectThrows(DateTimeException.class, () -> LocalDate.of(2024, 2, 30), "of Feb 30");
        expectThrows(DateTimeException.class, () -> LocalDate.of(2023, 2, 29), "of Feb 29 non-leap");
        expectThrows(DateTimeException.class, () -> LocalDate.of(2024, 13, 1), "of month 13");
        expectThrows(DateTimeException.class, () -> LocalDate.of(2024, 0, 1), "of month 0");
        expectThrows(DateTimeException.class, () -> LocalDate.of(2024, 1, 32), "of day 32");
        expectThrows(DateTimeException.class, () -> LocalDate.of(2024, 1, 0), "of day 0");
        expectThrows(DateTimeException.class, () -> LocalDate.of(999999999 + 1, 1, 1), "of year overflow");
        expectThrows(DateTimeParseException.class, () -> LocalDate.parse("2024-02-30"), "parse Feb 30");
        expectThrows(DateTimeParseException.class, () -> LocalDate.parse("2024/03/01"), "parse bad format");
        expectThrows(DateTimeParseException.class, () -> LocalDate.parse("garbage"), "parse garbage");
        expectThrows(DateTimeException.class, () -> LocalDate.of(2024, 1, 15).plusYears(Long.MAX_VALUE),
                "plusYears overflow");
    }

    static void testTemporalAccessors() {
        LocalDate d = LocalDate.of(2024, 2, 29);
        check(d.isSupported(ChronoField.DAY_OF_MONTH) && d.isSupported(ChronoUnit.DAYS),
                "isSupported DAY");
        check(!d.isSupported(ChronoField.SECOND_OF_DAY), "isSupported SECOND_OF_DAY false");
        check(!d.isSupported(ChronoUnit.HOURS), "isSupported HOURS false");
        ValueRange yr = d.range(ChronoField.YEAR);
        check(yr.getMinimum() == -999999999 && yr.getMaximum() == 999999999, "range YEAR");
        check(d.range(ChronoField.MONTH_OF_YEAR).getMaximum() == 12, "range MONTH_OF_YEAR");
        check(d.range(ChronoField.DAY_OF_MONTH).getMaximum() == 29, "range DAY_OF_MONTH");
        check(d.get(ChronoField.YEAR) == 2024 && d.get(ChronoField.MONTH_OF_YEAR) == 2
                && d.get(ChronoField.DAY_OF_MONTH) == 29, "get date fields");
        check(d.getLong(ChronoField.EPOCH_DAY) == d.toEpochDay(), "getLong EPOCH_DAY");
        check(d.getLong(ChronoField.PROLEPTIC_MONTH) == 2024L * 12 + 1, "getLong PROLEPTIC_MONTH");
        check(d.query(TemporalQueries.localDate()) == d, "query localDate");
        check(d.query(TemporalQueries.precision()) == ChronoUnit.DAYS, "query precision");
        check(d.query(TemporalQueries.chronology()) == IsoChronology.INSTANCE, "query chronology");
        expectThrows(UnsupportedTemporalTypeException.class, () -> d.get(ChronoField.SECOND_OF_DAY),
                "get SECOND_OF_DAY");
        expectThrows(UnsupportedTemporalTypeException.class, () -> d.getLong(ChronoField.SECOND_OF_DAY),
                "getLong SECOND_OF_DAY");
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

    static void testNullSemantics() {
        expectThrows(NullPointerException.class, () -> LocalDate.of(2024, (Month) null, 1), "of Month null");
        expectThrows(NullPointerException.class, () -> LocalDate.parse(null), "parse null");
        expectThrows(NullPointerException.class, () -> LocalDate.from(null), "from null");
        expectThrows(NullPointerException.class, () -> LocalDate.now((Clock) null), "now Clock null");
        expectThrows(NullPointerException.class, () -> LocalDate.now((ZoneId) null), "now ZoneId null");
        LocalDate d = LocalDate.of(2024, 1, 1);
        expectThrows(NullPointerException.class, () -> d.plus(null), "plus null");
        expectThrows(NullPointerException.class, () -> d.plus(1, null), "plus unit null");
        expectThrows(NullPointerException.class, () -> d.minus(null), "minus null");
        expectThrows(NullPointerException.class,
                () -> d.with((java.time.temporal.TemporalAdjuster) null), "with adjuster null");
        expectThrows(NullPointerException.class, () -> d.with((java.time.temporal.TemporalField) null, 1),
                "with field null");
        expectThrows(NullPointerException.class, () -> d.until(null), "until null");
        expectThrows(NullPointerException.class, () -> d.isAfter(null), "isAfter null");
        expectThrows(NullPointerException.class, () -> d.isBefore(null), "isBefore null");
        expectThrows(NullPointerException.class, () -> d.isEqual(null), "isEqual null");
        expectThrows(NullPointerException.class, () -> d.atTime((LocalTime) null), "atTime null");
        expectThrows(NullPointerException.class, () -> d.atStartOfDay((ZoneId) null), "atStartOfDay null");
        expectThrows(NullPointerException.class, () -> d.format(null), "format null");
        expectThrows(NullPointerException.class, () -> d.query(null), "query null");
        expectThrows(NullPointerException.class, () -> d.compareTo(null), "compareTo null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
