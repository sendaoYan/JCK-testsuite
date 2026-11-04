/*
 * @test
 * @summary java.time.Month conformance: white-box shape + black-box values/order, of, length,
 *          firstDayOfYear/firstMonthOfQuarter, plus/minus wrap, display names, and errors.
 * @run main MonthTest
 *
 * 设计案（黑盒）：
 * 1. 契约：Month 是 1..12 的枚举，TemporalAccessor/TemporalAdjuster，值不可变。
 * 2. 场景：of/getValue/values 枚举遍历，length(leap) 查天数，plus/minus 回绕，getDisplayName 本地化。
 * 3. 组合链路：values 顺序校验 -> of 往返 -> length -> plus 回绕 -> getDisplayName(Locale.US)。
 * 4. 不变量：values 顺序 JANUARY..DECEMBER；plus/minus 模 12；FEBRUARY 长度随闰年。
 * 5. 边界/异常：of(0)/of(13) 抛 DateTimeException。
 */

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.DateTimeException;
import java.util.Locale;

public class MonthTest {
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
        Class<?> c = Class.forName("java.time.Month");
        check(c.isEnum(), "enum");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() == Enum.class, "superclass Enum");
        check(java.time.temporal.TemporalAccessor.class.isAssignableFrom(c), "TemporalAccessor");
        check(java.time.temporal.TemporalAdjuster.class.isAssignableFrom(c), "TemporalAdjuster");
    }

    static void testBlackBox() {
        testValuesAndOf();
        testLengths();
        testPlusMinus();
        testDisplayNames();
        testErrors();
    }

    static void testValuesAndOf() {
        Month[] months = Month.values();
        check(months.length == 12, "values length 12");
        check(months[0] == Month.JANUARY && months[11] == Month.DECEMBER, "values order");
        check(Month.JANUARY.getValue() == 1 && Month.DECEMBER.getValue() == 12, "getValue");
        for (int i = 0; i < 12; i++) {
            check(Month.of(i + 1) == months[i], "of roundtrip " + (i + 1));
            check(Month.of(i + 1).getValue() == i + 1, "getValue roundtrip " + (i + 1));
        }
        check(Month.from(LocalDate.of(2024, 3, 1)) == Month.MARCH, "from(LocalDate)");
    }

    static void testLengths() {
        check(Month.FEBRUARY.length(false) == 28 && Month.FEBRUARY.length(true) == 29, "FEBRUARY length");
        check(Month.JANUARY.length(false) == 31 && Month.JANUARY.length(true) == 31, "JANUARY length");
        check(Month.APRIL.length(false) == 30, "APRIL length");
        check(Month.FEBRUARY.minLength() == 28 && Month.FEBRUARY.maxLength() == 29, "FEBRUARY min/max");
        check(Month.JANUARY.minLength() == 31 && Month.JANUARY.maxLength() == 31, "JANUARY min/max");
        check(Month.FEBRUARY.firstDayOfYear(false) == 32 && Month.FEBRUARY.firstDayOfYear(true) == 32,
                "firstDayOfYear FEBRUARY");
        check(Month.MARCH.firstDayOfYear(false) == 60 && Month.MARCH.firstDayOfYear(true) == 61,
                "firstDayOfYear MARCH leap dependent");
        check(Month.JANUARY.firstDayOfYear(false) == 1, "firstDayOfYear JANUARY");
        check(Month.JANUARY.firstMonthOfQuarter() == Month.JANUARY, "firstMonthOfQuarter JAN");
        check(Month.FEBRUARY.firstMonthOfQuarter() == Month.JANUARY, "firstMonthOfQuarter FEB");
        check(Month.APRIL.firstMonthOfQuarter() == Month.APRIL, "firstMonthOfQuarter APR");
        check(Month.DECEMBER.firstMonthOfQuarter() == Month.OCTOBER, "firstMonthOfQuarter DEC");
    }

    static void testPlusMinus() {
        check(Month.JANUARY.plus(1) == Month.FEBRUARY, "plus");
        check(Month.DECEMBER.plus(1) == Month.JANUARY, "plus wraps");
        check(Month.JANUARY.plus(12) == Month.JANUARY, "plus 12 wraps full");
        check(Month.JANUARY.plus(13) == Month.FEBRUARY, "plus 13");
        check(Month.JANUARY.minus(1) == Month.DECEMBER, "minus wraps");
        check(Month.DECEMBER.minus(11) == Month.JANUARY, "minus 11");
        check(Month.JUNE.plus(-1) == Month.MAY, "plus negative");
    }

    static void testDisplayNames() {
        check(Month.JANUARY.getDisplayName(TextStyle.FULL, Locale.US).equals("January"), "FULL January");
        check(Month.DECEMBER.getDisplayName(TextStyle.FULL, Locale.US).equals("December"), "FULL December");
        check(Month.JANUARY.getDisplayName(TextStyle.SHORT, Locale.US).equals("Jan"), "SHORT Jan");
        check(Month.MARCH.getDisplayName(TextStyle.FULL, Locale.US).equals("March"), "FULL March");
    }

    static void testErrors() {
        expectThrows(DateTimeException.class, () -> Month.of(0), "of 0");
        expectThrows(DateTimeException.class, () -> Month.of(13), "of 13");
        expectThrows(DateTimeException.class, () -> Month.of(-1), "of -1");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
