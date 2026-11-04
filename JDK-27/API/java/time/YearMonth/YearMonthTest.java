/*
 * @test
 * @summary java.time.YearMonth conformance: white-box shape + black-box factories/parse,
 *          getters, leap/length, atDay/atEndOfMonth, plus/minus rollover, comparisons, and errors.
 * @run main YearMonthTest
 *
 * 设计案（黑盒）：
 * 1. 契约：YearMonth 是年+月（无日），值对象不可变，ISO-8601。
 * 2. 场景：of/parse 构造，isLeapYear/lengthOfMonth/lengthOfYear/isValidDay 查询，atDay/atEndOfMonth 转日期，plusMonths/plusYears 运算。
 * 3. 组合链路：of -> lengthOfMonth -> atEndOfMonth -> plusMonths(跨年) -> 比较。
 * 4. 不变量：运算返回新实例；plusMonths 跨年进位；compareTo 按年月。
 * 5. 边界/异常：of(2024,13)/of(2024,0) 抛 DateTimeException；parse 非法抛 DateTimeParseException。
 */

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.time.Period;
import java.time.Year;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalQueries;
import java.time.temporal.UnsupportedTemporalTypeException;

public class YearMonthTest {
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.YearMonth");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == Object.class, "superclass Object");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(java.time.temporal.Temporal.class.isAssignableFrom(c), "Temporal");
        check(java.lang.Comparable.class.isAssignableFrom(c), "Comparable");
    }

    static void testBlackBox() {
        testFactories();
        testQueries();
        testConversions();
        testArithmeticImmutability();
        testComparison();
        testFormat();
        testErrors();

        // ---- 边界值/等价类/等价性（本棒补测） ----
        check(YearMonth.parse(YearMonth.of(-1, 1).toString()).equals(YearMonth.of(-1, 1)),
                "round-trip negative year");
        check(YearMonth.of(999999999, 12).equals(YearMonth.parse("+999999999-12")),
                "MAX parse with explicit sign");
        check(YearMonth.parse(YearMonth.of(-999999999, 1).toString())
                .equals(YearMonth.of(-999999999, 1)), "round-trip MIN");

        // plusMonths 跨年进位/借位
        check(YearMonth.of(2024, 12).plusMonths(1).equals(YearMonth.of(2025, 1)),
                "plusMonths Dec->Jan");
        check(YearMonth.of(2024, 1).minusMonths(1).equals(YearMonth.of(2023, 12)),
                "minusMonths Jan->Dec");
        check(YearMonth.of(2024, 1).plusMonths(24).equals(YearMonth.of(2026, 1)),
                "plusMonths 24 = +2y");

        // 每月月末
        check(YearMonth.of(2024, 4).atEndOfMonth().equals(LocalDate.of(2024, 4, 30)),
                "atEndOfMonth 30-day");
        check(YearMonth.of(2024, 12).atEndOfMonth().equals(LocalDate.of(2024, 12, 31)),
                "atEndOfMonth December");

        // compareTo 反对称
        YearMonth ma = YearMonth.of(2024, 2);
        YearMonth mb = YearMonth.of(2024, 3);
        check(ma.compareTo(mb) == -mb.compareTo(ma), "compareTo anti-symmetry");

        // 非法月 0 等价类
        expectThrows(DateTimeException.class, () -> YearMonth.of(2024, 0), "of month 0");
    }

    static void testFactories() {
        YearMonth ym = YearMonth.of(2024, 2);
        check(ym.getYear() == 2024 && ym.getMonthValue() == 2 && ym.getMonth() == Month.FEBRUARY,
                "of/getters");
        check(YearMonth.of(2024, Month.FEBRUARY).equals(ym), "of(int,Month)");
        check(YearMonth.parse("2024-02").equals(ym), "parse");
        check(ym.toString().equals("2024-02"), "toString");
    }

    static void testQueries() {
        check(YearMonth.of(2024, 2).isLeapYear() && !YearMonth.of(2023, 2).isLeapYear(),
                "isLeapYear");
        check(YearMonth.of(2024, 2).lengthOfMonth() == 29, "lengthOfMonth leap");
        check(YearMonth.of(2023, 2).lengthOfMonth() == 28, "lengthOfMonth non-leap");
        check(YearMonth.of(2024, 4).lengthOfMonth() == 30, "lengthOfMonth 30-day");
        check(YearMonth.of(2024, 2).lengthOfYear() == 366, "lengthOfYear leap");
        check(YearMonth.of(2023, 2).lengthOfYear() == 365, "lengthOfYear non-leap");
        check(YearMonth.of(2024, 2).isValidDay(29) && !YearMonth.of(2023, 2).isValidDay(29),
                "isValidDay");
        check(YearMonth.of(2024, 4).isValidDay(30) && !YearMonth.of(2024, 4).isValidDay(31),
                "isValidDay 30-day month");
    }

    static void testConversions() {
        YearMonth ym = YearMonth.of(2024, 2);
        check(ym.atDay(15).equals(LocalDate.of(2024, 2, 15)), "atDay");
        check(ym.atEndOfMonth().equals(LocalDate.of(2024, 2, 29)), "atEndOfMonth leap");
        check(YearMonth.of(2023, 2).atEndOfMonth().equals(LocalDate.of(2023, 2, 28)),
                "atEndOfMonth non-leap");
    }

    static void testArithmeticImmutability() {
        YearMonth ym = YearMonth.of(2024, 12);
        check(ym.plusMonths(1).equals(YearMonth.of(2025, 1)), "plusMonths rolls year");
        check(ym.plusMonths(13).equals(YearMonth.of(2026, 1)), "plusMonths 13");
        check(ym.plusMonths(-1).equals(YearMonth.of(2024, 11)), "plusMonths negative");
        check(ym.plusYears(1).equals(YearMonth.of(2025, 12)), "plusYears");
        check(ym.minusMonths(1).equals(YearMonth.of(2024, 11)), "minusMonths");
        check(ym.minusYears(1).equals(YearMonth.of(2023, 12)), "minusYears");
        check(ym.equals(YearMonth.of(2024, 12)), "immutability");
    }

    static void testComparison() {
        YearMonth a = YearMonth.of(2024, 2);
        YearMonth b = YearMonth.of(2024, 3);
        YearMonth a2 = YearMonth.of(2024, 2);
        check(a.isBefore(b) && b.isAfter(a), "isBefore/isAfter");
        check(!a.isBefore(a2) && !a.isAfter(a2), "equal not before/after");
        check(a.compareTo(b) < 0 && a.compareTo(a2) == 0, "compareTo");
        check(a.equals(a2) && a.hashCode() == a2.hashCode(), "equals/hashCode");
    }

    static void testFormat() {
        check(YearMonth.of(2024, 2).format(DateTimeFormatter.ofPattern("MM/yyyy", Locale.US))
                .equals("02/2024"), "format MM/yyyy");
    }

    static void testErrors() {
        expectThrows(DateTimeException.class, () -> YearMonth.of(2024, 13), "of month 13");
        expectThrows(DateTimeException.class, () -> YearMonth.of(2024, 0), "of month 0");
        expectThrows(DateTimeException.class, () -> YearMonth.of(2024, 2).atDay(30), "atDay Feb 30");
        expectThrows(DateTimeParseException.class, () -> YearMonth.parse("2024-13"), "parse month 13");
        expectThrows(DateTimeParseException.class, () -> YearMonth.parse("garbage"), "parse garbage");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepYearMonth();
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


    static void testDeepYearMonth() {
        YearMonth ym = YearMonth.of(2024, 2);
        check(ym.getYear() == 2024 && ym.getMonthValue() == 2, "getters");
        check(ym.getMonth() == Month.FEBRUARY, "getMonth");
        check(ym.isLeapYear(), "isLeapYear");
        check(ym.lengthOfYear() == 366 && ym.lengthOfMonth() == 29, "lengths leap");
        check(YearMonth.of(2023, 2).lengthOfMonth() == 28, "lengthOfMonth non-leap");
        check(YearMonth.of(2024, 4).lengthOfMonth() == 30, "April 30");
        xeq(ym.plusMonths(13).toString(), "2025-03", "plusMonths 13");
        xeq(ym.minusMonths(1).toString(), "2024-01", "minusMonths");
        xeq(ym.plusYears(1).toString(), "2025-02", "plusYears");
        xeq(ym.minusYears(24).toString(), "2000-02", "minusYears");
        xeq(ym.withYear(2025).toString(), "2025-02", "withYear");
        xeq(ym.withMonth(3).toString(), "2024-03", "withMonth");
        xeq(YearMonth.of(2024, 1).plusMonths(13).toString(), "2025-02", "year rollover plus");
        xeq(YearMonth.of(2024, 1).minusMonths(2).toString(), "2023-11", "year rollover minus");
        xeq(ym.atDay(29).toString(), "2024-02-29", "atDay leap");
        xeq(YearMonth.of(2024, 4).atEndOfMonth().toString(), "2024-04-30", "atEndOfMonth");
        xeq(YearMonth.of(2023, 2).atEndOfMonth().toString(), "2023-02-28", "atEndOfMonth non-leap");
        xeq(ym.atEndOfMonth().toString(), "2024-02-29", "atEndOfMonth leap");
        xeq(ym.atDay(1).toString(), "2024-02-01", "atDay 1");
        xact(() -> ym.atDay(30), DateTimeException.class, "Invalid date 'FEBRUARY 30'", "atDay 30 Feb");
        xact(() -> YearMonth.of(2024, 4).atDay(31), DateTimeException.class, "Invalid date 'APRIL 31'", "atDay 31 Apr");
        check(ym.isValidDay(29) && !YearMonth.of(2023, 2).isValidDay(29), "isValidDay");
        check(ym.until(YearMonth.of(2026, 3), ChronoUnit.MONTHS) == 25, "until MONTHS");
        check(ym.until(YearMonth.of(2026, 3), ChronoUnit.YEARS) == 2, "until YEARS trunc");
        xact(() -> ym.plus(1, ChronoUnit.DAYS), UnsupportedTemporalTypeException.class,
            "Unsupported unit: Days", "plus DAYS");
        check(ym.query(TemporalQueries.precision()) == ChronoUnit.MONTHS, "precision Months");
        check(ym.isSupported(ChronoUnit.MONTHS) && !ym.isSupported(ChronoUnit.DAYS), "support units");
        check(ym.isSupported(ChronoField.YEAR) && ym.isSupported(ChronoField.MONTH_OF_YEAR), "support fields");
        xact(() -> YearMonth.of(2024, 13), DateTimeException.class,
            "Invalid value for MonthOfYear (valid values 1 - 12): 13", "of month 13");
        xact(() -> YearMonth.of(2024, 2).withMonth(13), DateTimeException.class,
            "Invalid value for MonthOfYear (valid values 1 - 12): 13", "withMonth 13");
        check(YearMonth.parse("2024-02").equals(ym), "parse");
        check(YearMonth.from(LocalDate.of(2024, 2, 29)).equals(ym), "from");
        xeq(YearMonth.from(Year.of(2024).atMonth(2)).toString(), "2024-02", "from YearMonth");
        check(ym.compareTo(YearMonth.of(2024, 3)) < 0, "compareTo");
        check(ym.isBefore(YearMonth.of(2024, 3)) && ym.isAfter(YearMonth.of(2024, 1)), "isBefore/isAfter");
        check(ym.format(DateTimeFormatter.ofPattern("yyyy/MM")).equals("2024/02"), "format");
        xeq(ym.adjustInto(LocalDate.of(2024, 5, 10)).toString(), "2024-02-10", "adjustInto");
        xeq(ym.plus(Period.ofMonths(14)).toString(), "2025-04", "plus Period");
        check(YearMonth.of(2024, 2).atDay(29).toString().equals("2024-02-29"), "27 atDay");
    }

}
