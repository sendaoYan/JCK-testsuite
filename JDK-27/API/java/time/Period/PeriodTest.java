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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.temporal.UnsupportedTemporalTypeException;

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

    static Period roundTrip(Period v) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(v);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (Period) ois.readObject();
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

    static void testBlackBox() throws Exception {
        testFactories();
        testArithmeticImmutability();
        testNormalization();
        testAddSubtract();
        testComparisonAndUnits();
        testBoundaries();
        testErrors();
        testSerialization();
        testNullSemantics();
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

    static void testSerialization() throws Exception {
        check(ObjectStreamClass.lookup(Period.class).getSerialVersionUID() == -3587258372562876L,
                "serialVersionUID");
        Period[] vals = { Period.ZERO, Period.of(1, 2, 3), Period.of(-1, -2, -3), Period.ofWeeks(3) };
        for (Period v : vals) {
            check(roundTrip(v).equals(v), "serialization round-trip " + v);
        }
    }

    static void testNullSemantics() {
        expectThrows(NullPointerException.class, () -> Period.parse(null), "parse null");
        expectThrows(NullPointerException.class, () -> Period.from(null), "from null");
        expectThrows(NullPointerException.class, () -> Period.between(null, LocalDate.of(2024, 1, 1)),
                "between null");
        Period p = Period.of(1, 2, 3);
        expectThrows(NullPointerException.class, () -> p.plus(null), "plus null");
        expectThrows(NullPointerException.class, () -> p.minus(null), "minus null");
        expectThrows(NullPointerException.class, () -> p.addTo(null), "addTo null");
        expectThrows(NullPointerException.class, () -> p.subtractFrom(null), "subtractFrom null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepPeriod();
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


    static void testDeepPeriod() {
        Period pd = Period.of(1, 2, 3);
        xeq(pd.toString(), "P1Y2M3D", "toString");
        check(pd.getYears() == 1 && pd.getMonths() == 2 && pd.getDays() == 3, "getters");
        xeq(Period.ofYears(5).toString(), "P5Y", "ofYears");
        xeq(Period.ofMonths(5).toString(), "P5M", "ofMonths");
        xeq(Period.ofDays(5).toString(), "P5D", "ofDays");
        xeq(Period.ofWeeks(2).toString(), "P14D", "ofWeeks");
        xeq(Period.ZERO.toString(), "P0D", "ZERO");
        check(Period.parse("P1Y2M3D").equals(pd), "parse P1Y2M3D");
        xeq(Period.parse("P1W2D").toString(), "P9D", "parse weeks folded");
        xeq(Period.parse("P2Y").toString(), "P2Y", "parse P2Y");
        xeq(Period.parse("-P1Y2M").toString(), "P-1Y-2M", "parse neg prefix");
        xeq(Period.parse("P-1Y").toString(), "P-1Y", "parse neg element");
        xact(() -> Period.parse("P"), DateTimeParseException.class, "Text cannot be parsed to a Period", "parse P");
        xact(() -> Period.parse("P1X"), DateTimeParseException.class, "Text cannot be parsed to a Period", "parse P1X");
        xact(() -> Period.parse(""), DateTimeParseException.class, null, "parse empty");
        xact(() -> Period.parse(null), NullPointerException.class, null, "parse null");
        xeq(Period.ofMonths(13).normalized().toString(), "P1Y1M", "normalized 13M");
        xeq(Period.ofMonths(-13).normalized().toString(), "P-1Y-1M", "normalized -13M");
        xeq(Period.of(1, 25, 0).normalized().toString(), "P3Y1M", "normalized 1Y25M");
        xeq(Period.of(0, 0, 40).normalized().toString(), "P40D", "normalized days untouched");
        check(pd.toTotalMonths() == 14, "toTotalMonths");
        check(Period.of(1, 2, 100).toTotalMonths() == 14, "toTotalMonths ignores days");
        xeq(Period.between(LocalDate.of(2023, 1, 31), LocalDate.of(2024, 3, 1)).toString(), "P1Y1M1D", "between clamp");
        xeq(Period.between(LocalDate.of(2023, 2, 28), LocalDate.of(2023, 3, 1)).toString(), "P1D", "between 1 day");
        xeq(Period.between(LocalDate.of(2024, 2, 29), LocalDate.of(2024, 3, 30)).toString(), "P1M1D", "between leap");
        xeq(Period.between(LocalDate.of(2024, 1, 15), LocalDate.of(2024, 3, 10)).toString(), "P1M24D", "between mid");
        xeq(Period.between(LocalDate.of(2024, 2, 29), LocalDate.of(2025, 2, 28)).toString(), "P11M30D", "between 364d");
        xeq(Period.between(LocalDate.of(2024, 3, 31), LocalDate.of(2024, 4, 30)).toString(), "P30D", "between 30d");
        xeq(Period.between(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 1)).toString(), "P0D", "between self");
        xact(() -> Period.between(LocalDate.of(2024, 1, 1), null), NullPointerException.class, null, "between null");
        xeq(pd.withYears(5).toString(), "P5Y2M3D", "withYears");
        xeq(pd.withMonths(0).toString(), "P1Y3D", "withMonths");
        xeq(pd.withDays(-1).toString(), "P1Y2M-1D", "withDays");
        xeq(pd.plus(Period.of(1, 1, 1)).toString(), "P2Y3M4D", "plus");
        xeq(pd.minus(Period.of(0, 1, 0)).toString(), "P1Y1M3D", "minus");
        xeq(pd.plusDays(1).toString(), "P1Y2M4D", "plusDays");
        xeq(pd.plusMonths(1).toString(), "P1Y3M3D", "plusMonths");
        xeq(pd.plusYears(1).toString(), "P2Y2M3D", "plusYears");
        xeq(pd.minusDays(1).toString(), "P1Y2M2D", "minusDays");
        xeq(pd.minusMonths(1).toString(), "P1Y1M3D", "minusMonths");
        xeq(pd.minusYears(1).toString(), "P2M3D", "minusYears");
        xeq(pd.multipliedBy(2).toString(), "P2Y4M6D", "multipliedBy");
        xeq(pd.multipliedBy(0).toString(), "P0D", "multipliedBy 0");
        xeq(pd.negated().toString(), "P-1Y-2M-3D", "negated");
        check(!pd.isZero(), "isZero false");
        check(Period.ZERO.isZero(), "isZero true");
        check(!pd.isNegative(), "isNegative false");
        check(Period.of(-1, 0, 0).isNegative(), "isNegative true");
        xeq(pd.getUnits().toString(), "[Years, Months, Days]", "units");
        xeq(pd.get(ChronoUnit.YEARS), 1L, "get YEARS");
        xeq(pd.get(ChronoUnit.MONTHS), 2L, "get MONTHS");
        xeq(pd.get(ChronoUnit.DAYS), 3L, "get DAYS");
        xact(() -> pd.get(ChronoUnit.WEEKS), UnsupportedTemporalTypeException.class, "Unsupported unit: Weeks", "get WEEKS");
        xact(() -> pd.get(null), UnsupportedTemporalTypeException.class, "Unsupported unit: null", "get null unit -> UTE not NPE");
        xact(() -> pd.plus(Duration.ofDays(1)), DateTimeException.class,
            "Unit must be Years, Months or Days, but was Seconds", "plus Duration");
        xeq(pd.addTo(LocalDate.of(2024, 1, 28)).toString(), "2025-03-31", "addTo");
        xeq(pd.subtractFrom(LocalDate.of(2025, 3, 31)).toString(), "2024-01-28", "subtractFrom");
        check(pd.equals(Period.ofYears(1).withMonths(2).withDays(3)), "equals paths");
        check(pd.hashCode() == Period.of(1, 2, 3).hashCode(), "hashCode");
        check(!pd.equals("P1Y2M3D"), "equals non-Period");
    }

}
