/*
 * @test
 * @summary java.time.Period conformance: white-box shape + black-box factories/parse,
 *          component getters, arithmetic, normalization, addTo/subtractFrom, and units order.
 * @run main PeriodTest
 *
 * 设计案（黑盒）：
 * 1. 契约：Period 是以年/月/日为单位的日期时间量，值对象不可变，ISO-8601 PnYnMnD 语义。
 * 2. 场景：of/ofYears/ofMonths/ofWeeks/ofDays/parse 构造，between 求差，plus/minus/multipliedBy/negated 运算，
 *          addTo/subtractFrom 作用于 LocalDate。
 * 3. 组合链路：of(1,2,3) -> plusDays -> normalized -> toTotalMonths -> addTo(LocalDate) -> getUnits 顺序。
 * 4. 不变量：运算返回新实例；normalized 把 12 月折为年；equals 按年月日；getUnits=[YEARS,MONTHS,DAYS]。
 * 5. 边界/异常：parse 非法抛 DateTimeParseException。
 */

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class PeriodTest {
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
        Class<?> c = Class.forName("java.time.Period");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == Object.class, "superclass Object");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(java.time.chrono.ChronoPeriod.class.isAssignableFrom(c), "ChronoPeriod");
    }

    static void testBlackBox() {
        testFactories();
        testArithmeticImmutability();
        testNormalization();
        testAddSubtract();
        testComparisonAndUnits();
        testBoundaries();
        testErrors();
    }

    static void testFactories() {
        Period p = Period.of(1, 2, 3);
        check(p.getYears() == 1 && p.getMonths() == 2 && p.getDays() == 3, "of/getters");
        check(Period.ofYears(2).getYears() == 2, "ofYears");
        check(Period.ofMonths(5).getMonths() == 5, "ofMonths");
        check(Period.ofWeeks(4).getDays() == 28, "ofWeeks = 28 days");
        check(Period.ofDays(10).getDays() == 10, "ofDays");
        check(Period.parse("P1Y2M3D").equals(p), "parse P1Y2M3D");
        check(Period.parse("P4W").getDays() == 28, "parse P4W");
        check(Period.parse("-P1Y").getYears() == -1, "parse negative");
        Period between = Period.between(LocalDate.of(2024, 1, 15), LocalDate.of(2024, 3, 20));
        check(between.getYears() == 0 && between.getMonths() == 2 && between.getDays() == 5,
                "between");
        check(p.toString().equals("P1Y2M3D"), "toString");
    }

    static void testArithmeticImmutability() {
        Period p = Period.of(1, 2, 3);
        check(p.plus(Period.ofDays(1)).equals(Period.of(1, 2, 4)), "plus");
        check(p.plusYears(1).equals(Period.of(2, 2, 3)), "plusYears");
        check(p.plusMonths(1).equals(Period.of(1, 3, 3)), "plusMonths");
        check(p.plusDays(1).equals(Period.of(1, 2, 4)), "plusDays");
        check(p.minus(Period.ofDays(1)).equals(Period.of(1, 2, 2)), "minus");
        check(p.minusYears(1).equals(Period.of(0, 2, 3)), "minusYears");
        check(p.minusMonths(1).equals(Period.of(1, 1, 3)), "minusMonths");
        check(p.minusDays(1).equals(Period.of(1, 2, 2)), "minusDays");
        check(p.multipliedBy(2).equals(Period.of(2, 4, 6)), "multipliedBy");
        check(p.negated().equals(Period.of(-1, -2, -3)), "negated");
        check(p.equals(Period.of(1, 2, 3)), "immutability");
    }

    static void testNormalization() {
        check(Period.of(0, 12, 0).normalized().equals(Period.of(1, 0, 0)),
                "normalized 12 months -> 1 year");
        check(Period.of(1, 12, 0).normalized().equals(Period.of(2, 0, 0)),
                "normalized years accumulate");
        check(Period.of(0, 13, 0).normalized().equals(Period.of(1, 1, 0)),
                "normalized 13 months -> 1y1m");
        check(Period.of(0, 12, 5).normalized().equals(Period.of(1, 0, 5)), "normalized keeps days");
        check(Period.of(1, 6, 0).toTotalMonths() == 18, "toTotalMonths");
    }

    static void testAddSubtract() {
        check(Period.of(0, 1, 0).addTo(LocalDate.of(2024, 1, 31)).equals(LocalDate.of(2024, 2, 29)),
                "addTo month clamps to month end");
        check(Period.of(1, 0, 0).addTo(LocalDate.of(2024, 2, 29)).equals(LocalDate.of(2025, 2, 28)),
                "addTo year clamps leap day");
        check(Period.ofDays(1).subtractFrom(LocalDate.of(2024, 3, 1)).equals(LocalDate.of(2024, 2, 29)),
                "subtractFrom");
    }

    static void testComparisonAndUnits() {
        Period a = Period.of(1, 2, 3);
        Period b = Period.of(1, 2, 3);
        Period c = Period.of(1, 2, 4);
        check(a.equals(b) && a.hashCode() == b.hashCode(), "equals/hashCode");
        check(!a.equals(c), "equals different");
        check(Period.ZERO.isZero() && !a.isZero(), "isZero");
        check(Period.ofMonths(-1).isNegative() && !a.isNegative(), "isNegative");
        check(a.get(ChronoUnit.YEARS) == 1 && a.get(ChronoUnit.MONTHS) == 2
                && a.get(ChronoUnit.DAYS) == 3, "get(ChronoUnit)");
        List<ChronoUnit> units = a.getUnits().stream().map(u -> (ChronoUnit) u).toList();
        check(units.size() == 3 && units.get(0) == ChronoUnit.YEARS
                && units.get(1) == ChronoUnit.MONTHS && units.get(2) == ChronoUnit.DAYS,
                "getUnits [YEARS, MONTHS, DAYS]");
    }

    static void testBoundaries() {
        check(Period.of(0, 0, 0).equals(Period.ZERO), "of(0,0,0) == ZERO");
        check(Period.ofDays(0).equals(Period.ZERO), "ofDays(0) == ZERO");
        check(Period.ZERO.isZero() && !Period.ZERO.isNegative(), "ZERO not negative");
        check(Period.of(1, 2, 3).multipliedBy(0).equals(Period.ZERO), "multipliedBy(0) == ZERO");
        check(Period.of(1, 2, 3).multipliedBy(-1).equals(Period.of(-1, -2, -3)), "multipliedBy(-1) == negated");
        check(Period.ofYears(Integer.MAX_VALUE).getYears() == Integer.MAX_VALUE, "ofYears(Integer.MAX_VALUE)");
        check(Period.between(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 1)).equals(Period.ZERO),
              "between identical dates == ZERO");
        check(Period.parse("P0D").equals(Period.ZERO), "parse P0D == ZERO");
        check(Period.parse("P0Y0M0D").equals(Period.ZERO), "parse P0Y0M0D == ZERO");
        check(Period.ZERO.addTo(LocalDate.of(2024, 5, 5)).equals(LocalDate.of(2024, 5, 5)),
              "ZERO.addTo identity");
        expectThrows(ArithmeticException.class, () -> Period.ofYears(Integer.MAX_VALUE).plusYears(1),
                "plusYears overflow");
        expectThrows(java.time.temporal.UnsupportedTemporalTypeException.class,
                () -> Period.ZERO.get(ChronoUnit.SECONDS), "get unsupported unit");
    }

    static void testErrors() {
        expectThrows(DateTimeParseException.class, () -> Period.parse("1Y"), "parse missing P");
        expectThrows(DateTimeParseException.class, () -> Period.parse("garbage"), "parse garbage");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
