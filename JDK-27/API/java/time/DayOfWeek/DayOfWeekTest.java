/*
 * @test
 * @summary java.time.DayOfWeek conformance: white-box shape + black-box values/order, of,
 *          plus/minus wrap, display names, and errors.
 * @run main DayOfWeekTest
 *
 * 设计案（黑盒）：
 * 1. 契约：DayOfWeek 是 MONDAY(1)..SUNDAY(7) 的枚举，TemporalAccessor/TemporalAdjuster，值不可变。
 * 2. 场景：of/getValue/values 枚举遍历，plus/minus 回绕，getDisplayName 本地化，from(LocalDate) 查询。
 * 3. 组合链路：values 顺序校验 -> of 往返 -> plus 回绕（含负数）-> getDisplayName(Locale.US)。
 * 4. 不变量：values 顺序 MONDAY..SUNDAY；plus/minus 模 7；getValue 1..7。
 * 5. 边界/异常：of(0)/of(8) 抛 DateTimeException。
 */

import java.lang.reflect.Modifier;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.DateTimeException;
import java.util.Locale;

public class DayOfWeekTest {
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
        Class<?> c = Class.forName("java.time.DayOfWeek");
        check(c.isEnum(), "enum");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() == Enum.class, "superclass Enum");
        check(java.time.temporal.TemporalAccessor.class.isAssignableFrom(c), "TemporalAccessor");
        check(java.time.temporal.TemporalAdjuster.class.isAssignableFrom(c), "TemporalAdjuster");
    }

    static void testBlackBox() {
        testValuesAndOf();
        testPlusMinus();
        testDisplayNames();
        testErrors();
    }

    static void testValuesAndOf() {
        DayOfWeek[] days = DayOfWeek.values();
        check(days.length == 7, "values length 7");
        check(days[0] == DayOfWeek.MONDAY && days[6] == DayOfWeek.SUNDAY, "values order");
        check(DayOfWeek.MONDAY.getValue() == 1 && DayOfWeek.SUNDAY.getValue() == 7, "getValue");
        for (int i = 0; i < 7; i++) {
            check(DayOfWeek.of(i + 1) == days[i], "of roundtrip " + (i + 1));
            check(DayOfWeek.of(i + 1).getValue() == i + 1, "getValue roundtrip " + (i + 1));
        }
        check(DayOfWeek.from(LocalDate.of(2024, 3, 1)) == DayOfWeek.FRIDAY, "from Friday");
        check(DayOfWeek.from(LocalDate.of(2024, 2, 29)) == DayOfWeek.THURSDAY, "from Thursday");
    }

    static void testPlusMinus() {
        check(DayOfWeek.MONDAY.plus(1) == DayOfWeek.TUESDAY, "plus");
        check(DayOfWeek.SUNDAY.plus(1) == DayOfWeek.MONDAY, "plus wraps");
        check(DayOfWeek.MONDAY.plus(7) == DayOfWeek.MONDAY, "plus 7 wraps full");
        check(DayOfWeek.MONDAY.plus(8) == DayOfWeek.TUESDAY, "plus 8");
        check(DayOfWeek.MONDAY.minus(1) == DayOfWeek.SUNDAY, "minus wraps");
        check(DayOfWeek.TUESDAY.minus(8) == DayOfWeek.MONDAY, "minus 8");
        check(DayOfWeek.FRIDAY.plus(-1) == DayOfWeek.THURSDAY, "plus negative");
        check(DayOfWeek.MONDAY.plus(14) == DayOfWeek.MONDAY, "plus 14");
    }

    static void testDisplayNames() {
        check(DayOfWeek.MONDAY.getDisplayName(TextStyle.FULL, Locale.US).equals("Monday"), "FULL Monday");
        check(DayOfWeek.SUNDAY.getDisplayName(TextStyle.FULL, Locale.US).equals("Sunday"), "FULL Sunday");
        check(DayOfWeek.WEDNESDAY.getDisplayName(TextStyle.SHORT, Locale.US).equals("Wed"), "SHORT Wed");
    }

    static void testErrors() {
        expectThrows(DateTimeException.class, () -> DayOfWeek.of(0), "of 0");
        expectThrows(DateTimeException.class, () -> DayOfWeek.of(8), "of 8");
        expectThrows(DateTimeException.class, () -> DayOfWeek.of(-1), "of -1");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
