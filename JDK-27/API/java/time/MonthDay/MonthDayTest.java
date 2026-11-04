/*
 * @test
 * @summary java.time.MonthDay conformance: white-box shape + black-box factories/parse,
 *          getters, isValidYear, with/atYear conversions, comparisons, and errors.
 * @run main MonthDayTest
 *
 * 设计案（黑盒）：
 * 1. 契约：MonthDay 是月+日（无年），值对象不可变，用于表示生日/纪念日等周期性日期。
 * 2. 场景：of/parse 构造，getMonth/getDayOfMonth/isValidYear 查询，withDayOfMonth/withMonth 修改，atYear 转 LocalDate。
 * 3. 组合链路：of(2,29) -> isValidYear -> atYear -> withDayOfMonth -> 比较。
 * 4. 不变量：运算返回新实例；2/29 只在闰年有效；compareTo 按月后日。
 * 5. 边界/异常：of(2,30) 抛 DateTimeException；parse 非法抛 DateTimeParseException。
 */

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class MonthDayTest {
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
        Class<?> c = Class.forName("java.time.MonthDay");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == Object.class, "superclass Object");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(java.time.temporal.TemporalAccessor.class.isAssignableFrom(c), "TemporalAccessor");
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
        check(MonthDay.parse(MonthDay.of(1, 1).toString()).equals(MonthDay.of(1, 1)),
                "round-trip Jan 1");
        check(MonthDay.parse(MonthDay.of(12, 31).toString()).equals(MonthDay.of(12, 31)),
                "round-trip Dec 31");
        check(MonthDay.parse(MonthDay.of(2, 29).toString()).equals(MonthDay.of(2, 29)),
                "round-trip Feb 29");

        // 每月最大天（月末）
        check(MonthDay.of(Month.FEBRUARY, 29).isValidYear(2024), "Feb 29 valid in leap");
        check(MonthDay.of(Month.DECEMBER, 31).isValidYear(2023), "Dec 31 valid");
        check(MonthDay.of(Month.APRIL, 30).isValidYear(2023), "Apr 30 valid");

        // atYear 钳制：非闰年 2/29 -> 2/28
        check(MonthDay.of(2, 29).atYear(2023).equals(LocalDate.of(2023, 2, 28)),
                "atYear Feb 29 non-leap clamps");

        // withDayOfMonth 修改并保持不可变
        MonthDay mdd = MonthDay.of(2, 29);
        check(mdd.withDayOfMonth(1).equals(MonthDay.of(2, 1)), "withDayOfMonth");
        check(mdd.equals(MonthDay.of(2, 29)), "immutability after withDayOfMonth");

        // compareTo 反对称
        MonthDay a = MonthDay.of(2, 29);
        MonthDay b = MonthDay.of(3, 1);
        check(a.compareTo(b) == -b.compareTo(a), "compareTo anti-symmetry");

        // 非法月 0 等价类
        expectThrows(DateTimeException.class, () -> MonthDay.of(0, 1), "of month 0");
    }

    static void testFactories() {
        MonthDay md = MonthDay.of(Month.FEBRUARY, 29);
        check(md.getMonth() == Month.FEBRUARY && md.getMonthValue() == 2 && md.getDayOfMonth() == 29,
                "of/getters");
        check(MonthDay.of(2, 29).equals(md), "of(int,int)");
        check(MonthDay.parse("--02-29").equals(md), "parse --MM-dd");
        check(md.toString().equals("--02-29"), "toString");
    }

    static void testQueries() {
        MonthDay md = MonthDay.of(2, 29);
        check(md.isValidYear(2024) && !md.isValidYear(2023), "isValidYear leap");
        check(MonthDay.of(1, 15).isValidYear(2023), "isValidYear normal");
    }

    static void testConversions() {
        MonthDay md = MonthDay.of(2, 29);
        check(md.atYear(2024).equals(LocalDate.of(2024, 2, 29)), "atYear leap");
        check(md.atYear(2023).equals(LocalDate.of(2023, 2, 28)), "atYear non-leap clamps to Feb 28");
        check(md.withMonth(1).equals(MonthDay.of(1, 29)), "withMonth");
        check(md.withDayOfMonth(15).equals(MonthDay.of(2, 15)), "withDayOfMonth");
        check(md.with(Month.MARCH).equals(MonthDay.of(3, 29)), "with(Month)");
    }

    static void testArithmeticImmutability() {
        MonthDay md = MonthDay.of(2, 29);
        check(md.withDayOfMonth(1).equals(MonthDay.of(2, 1)), "withDayOfMonth new instance");
        check(md.equals(MonthDay.of(2, 29)), "immutability");
    }

    static void testComparison() {
        MonthDay a = MonthDay.of(2, 29);
        MonthDay b = MonthDay.of(3, 1);
        MonthDay a2 = MonthDay.of(2, 29);
        check(a.isBefore(b) && b.isAfter(a), "isBefore/isAfter");
        check(!a.isBefore(a2) && !a.isAfter(a2), "equal not before/after");
        check(a.compareTo(b) < 0 && a.compareTo(a2) == 0, "compareTo");
        check(a.equals(a2) && a.hashCode() == a2.hashCode(), "equals/hashCode");
    }

    static void testFormat() {
        check(MonthDay.of(2, 29).format(DateTimeFormatter.ofPattern("MM-dd", Locale.US))
                .equals("02-29"), "format MM-dd");
    }

    static void testErrors() {
        expectThrows(DateTimeException.class, () -> MonthDay.of(2, 30), "of Feb 30");
        expectThrows(DateTimeException.class, () -> MonthDay.of(13, 1), "of month 13");
        expectThrows(DateTimeException.class, () -> MonthDay.of(4, 31), "of April 31");
        expectThrows(DateTimeParseException.class, () -> MonthDay.parse("02-30"), "parse Feb 30");
        expectThrows(DateTimeParseException.class, () -> MonthDay.parse("garbage"), "parse garbage");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
