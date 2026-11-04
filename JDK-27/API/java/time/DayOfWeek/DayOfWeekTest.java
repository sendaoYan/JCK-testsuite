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
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalQueries;

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

        // ---- 边界值/等价类/等价性（本棒补测） ----
        for (DayOfWeek d : DayOfWeek.values()) {
            check(DayOfWeek.valueOf(d.name()) == d, "valueOf round-trip " + d);
        }
        check(DayOfWeek.MONDAY.plus(0) == DayOfWeek.MONDAY, "plus(0) identity");
        check(DayOfWeek.MONDAY.minus(7) == DayOfWeek.MONDAY, "minus(7) identity");
        check(DayOfWeek.MONDAY.plus(-7) == DayOfWeek.MONDAY, "plus(-7) identity");
        check(DayOfWeek.from(LocalDate.of(2024, 1, 1)) == DayOfWeek.MONDAY, "from 2024-01-01 Monday");
        check(DayOfWeek.MONDAY.compareTo(DayOfWeek.TUESDAY) < 0
                && DayOfWeek.SUNDAY.compareTo(DayOfWeek.MONDAY) > 0, "compareTo ordinal order");
        check(DayOfWeek.FRIDAY.getDisplayName(TextStyle.FULL, Locale.US).equals("Friday"),
                "FULL Friday");
        check(DayOfWeek.MONDAY.getDisplayName(TextStyle.SHORT, Locale.US).equals("Mon"),
                "SHORT Monday");
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
        testDeepDayOfWeek();
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


    static void testDeepDayOfWeek() {
        DayOfWeek[] vals = DayOfWeek.values();
        check(vals.length == 7 && vals[0] == DayOfWeek.MONDAY && vals[6] == DayOfWeek.SUNDAY, "values order ISO");
        check(DayOfWeek.valueOf("MONDAY") == DayOfWeek.MONDAY, "valueOf");
        xact(() -> DayOfWeek.valueOf("bogus"), IllegalArgumentException.class,
            "No enum constant java.time.DayOfWeek.bogus", "valueOf bogus");
        check(DayOfWeek.of(1) == DayOfWeek.MONDAY && DayOfWeek.of(7) == DayOfWeek.SUNDAY, "of ISO");
        xact(() -> DayOfWeek.of(0), DateTimeException.class,
            "Invalid value for DayOfWeek: 0", "of 0");
        xact(() -> DayOfWeek.of(8), DateTimeException.class,
            "Invalid value for DayOfWeek: 8", "of 8");
        for (int i = 0; i < 7; i++) {
            final DayOfWeek d = vals[i];
            check(d.getValue() == i + 1, "getValue " + d);
            check(d.plus(7) == d, "plus 7 identity " + d);
            check(d.minus(7) == d, "minus 7 identity " + d);
        }
        check(DayOfWeek.MONDAY.plus(1) == DayOfWeek.TUESDAY, "plus 1");
        check(DayOfWeek.SUNDAY.plus(1) == DayOfWeek.MONDAY, "plus 1 wraps");
        check(DayOfWeek.MONDAY.minus(1) == DayOfWeek.SUNDAY, "minus 1 wraps");
        check(DayOfWeek.FRIDAY.plus(4) == DayOfWeek.TUESDAY, "plus 4");
        check(DayOfWeek.THURSDAY.getDisplayName(TextStyle.FULL, java.util.Locale.FRENCH).equals("jeudi"), "FR full");
        check(DayOfWeek.THURSDAY.getDisplayName(TextStyle.NARROW, java.util.Locale.FRENCH).equals("J"), "FR narrow");
        check(DayOfWeek.THURSDAY.getDisplayName(TextStyle.SHORT, java.util.Locale.ENGLISH).equals("Thu"), "EN short");
        check(DayOfWeek.THURSDAY.getDisplayName(TextStyle.FULL, java.util.Locale.ENGLISH).equals("Thursday"), "EN full");
        check(DayOfWeek.from(LocalDate.of(2024, 2, 29)) == DayOfWeek.THURSDAY, "from Feb29-2024");
        check(DayOfWeek.from(LocalDate.of(2024, 3, 1)) == DayOfWeek.FRIDAY, "from Mar1-2024");
        check(DayOfWeek.THURSDAY.query(TemporalQueries.precision()) == ChronoUnit.DAYS, "precision");
        xeq(DayOfWeek.THURSDAY.adjustInto(LocalDate.of(2024, 2, 29)).toString(), "2024-02-29", "adjustInto same");
        xeq(DayOfWeek.MONDAY.adjustInto(LocalDate.of(2024, 2, 29)).toString(), "2024-02-26", "adjustInto back");
        check(DayOfWeek.THURSDAY.getLong(ChronoField.DAY_OF_WEEK) == 4, "getLong");
        check(DayOfWeek.MONDAY.compareTo(DayOfWeek.TUESDAY) < 0, "compareTo");
        xeq(LocalDate.of(2024, 2, 29).getDayOfWeek().getDisplayName(TextStyle.FULL, java.util.Locale.FRENCH),
            "jeudi", "localized dow of date");
    }

}
