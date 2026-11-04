/*
 * @test
 * @summary java.time.Year conformance: white-box shape + black-box factories/parse, leap/length,
 *          atDay/atMonth/atMonthDay, plus/minus, comparisons, and errors.
 * @run main YearTest
 *
 * 设计案（黑盒）：
 * 1. 契约：Year 是 ISO-8601 年份，值对象不可变，范围 -999999999..999999999。
 * 2. 场景：of/parse/now(clock) 构造，isLeap/length 查询，atDay/atMonth/atMonthDay 转日期，plus/minus 运算。
 * 3. 组合链路：of -> isLeap/length -> atDay(60) -> atMonth -> isValidMonthDay -> plusYears -> 比较。
 * 4. 不变量：运算返回新实例；闰年规则（4/100/400）；compareTo 按年份。
 * 5. 边界/异常：of 超出范围抛 DateTimeException；parse 非法抛 DateTimeParseException。
 */

import java.lang.reflect.Modifier;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class YearTest {
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
        Class<?> c = Class.forName("java.time.Year");
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
        testLeapAndLength();
        testConversions();
        testArithmeticImmutability();
        testComparison();
        testFormat();
        testErrors();

        // ---- 边界值/等价类/等价性（本棒补测） ----
        check(Year.parse(Year.of(Year.MIN_VALUE).toString()).equals(Year.of(Year.MIN_VALUE)),
                "round-trip MIN");
        check(Year.parse(Year.of(Year.MAX_VALUE).toString()).equals(Year.of(Year.MAX_VALUE)),
                "round-trip MAX");
        check(Year.parse(Year.of(-1).toString()).equals(Year.of(-1)), "round-trip -1");
        check(Year.parse(Year.of(0).toString()).equals(Year.of(0)), "round-trip 0");

        // length 与 isLeap 一致（极值年份）
        check(Year.of(Year.MIN_VALUE).length() == (Year.of(Year.MIN_VALUE).isLeap() ? 366 : 365),
                "MIN length/isLeap consistent");
        check(Year.of(Year.MAX_VALUE).length() == (Year.of(Year.MAX_VALUE).isLeap() ? 366 : 365),
                "MAX length/isLeap consistent");

        // 极值邻域运算溢出
        expectThrows(DateTimeException.class, () -> Year.of(Year.MIN_VALUE).minusYears(1),
                "MIN minusYears overflow");
        expectThrows(DateTimeException.class, () -> Year.of(Year.MAX_VALUE).plusYears(1),
                "MAX plusYears overflow");

        // atDay 年末边界
        check(Year.of(2023).atDay(365).equals(LocalDate.of(2023, 12, 31)), "atDay 365 non-leap");
        check(Year.of(2024).atDay(366).equals(LocalDate.of(2024, 12, 31)), "atDay 366 leap");

        // compareTo 反对称
        Year ya = Year.of(2024);
        Year yb = Year.of(2025);
        check(ya.compareTo(yb) == -yb.compareTo(ya), "compareTo anti-symmetry");
    }

    static void testFactories() {
        Year y = Year.of(2024);
        check(y.getValue() == 2024, "of/getValue");
        check(Year.parse("2024").equals(y), "parse");
        check(Year.of(0).getValue() == 0, "of year 0 valid");
        check(Year.of(Year.MIN_VALUE).getValue() == -999999999, "MIN_VALUE");
        check(Year.of(Year.MAX_VALUE).getValue() == 999999999, "MAX_VALUE");
        Year now = Year.now(Clock.fixed(Instant.parse("2024-06-01T00:00:00Z"), ZoneOffset.UTC));
        check(now.getValue() == 2024, "now(fixed clock)");
        check(y.toString().equals("2024"), "toString");
    }

    static void testLeapAndLength() {
        check(Year.of(2024).isLeap() && !Year.of(2023).isLeap(), "isLeap");
        check(!Year.of(1900).isLeap() && Year.of(2000).isLeap(), "century leap rules");
        check(Year.of(0).isLeap(), "year 0 leap (divisible by 400)");
        check(Year.isLeap(2024) && !Year.isLeap(1900) && Year.isLeap(2000), "static isLeap");
        check(Year.of(2024).length() == 366 && Year.of(2023).length() == 365, "length");
    }

    static void testConversions() {
        Year y = Year.of(2024);
        check(y.atDay(60).equals(LocalDate.of(2024, 2, 29)), "atDay leap");
        check(Year.of(2023).atDay(60).equals(LocalDate.of(2023, 3, 1)), "atDay non-leap");
        check(y.atMonth(Month.FEBRUARY).equals(YearMonth.of(2024, 2)), "atMonth(Month)");
        check(y.atMonth(2).equals(YearMonth.of(2024, 2)), "atMonth(int)");
        check(y.atMonthDay(MonthDay.of(2, 29)).equals(LocalDate.of(2024, 2, 29)), "atMonthDay");
        check(y.isValidMonthDay(MonthDay.of(2, 29)) && !Year.of(2023).isValidMonthDay(MonthDay.of(2, 29)),
                "isValidMonthDay");
    }

    static void testArithmeticImmutability() {
        Year y = Year.of(2024);
        check(y.plusYears(1).equals(Year.of(2025)), "plusYears");
        check(y.plusYears(-1).equals(Year.of(2023)), "plusYears negative");
        check(y.minusYears(1).equals(Year.of(2023)), "minusYears");
        check(y.equals(Year.of(2024)), "immutability");
    }

    static void testComparison() {
        Year a = Year.of(2024);
        Year b = Year.of(2025);
        Year a2 = Year.of(2024);
        check(a.isBefore(b) && b.isAfter(a), "isBefore/isAfter");
        check(!a.isBefore(a2) && !a.isAfter(a2), "equal not before/after");
        check(a.compareTo(b) < 0 && a.compareTo(a2) == 0, "compareTo");
        check(a.equals(a2) && a.hashCode() == a2.hashCode(), "equals/hashCode");
    }

    static void testFormat() {
        check(Year.of(2024).format(DateTimeFormatter.ofPattern("yyyy", Locale.US)).equals("2024"),
                "format yyyy");
    }

    static void testErrors() {
        expectThrows(DateTimeException.class, () -> Year.of(1000000000), "of above MAX");
        expectThrows(DateTimeException.class, () -> Year.of(-1000000000), "of below MIN");
        expectThrows(DateTimeException.class, () -> Year.of(2023).atDay(366), "atDay 366 non-leap");
        expectThrows(DateTimeParseException.class, () -> Year.parse("garbage"), "parse garbage");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
