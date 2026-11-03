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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
