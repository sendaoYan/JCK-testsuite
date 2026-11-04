/*
 * @test
 * @summary java.time.Period API conformance: full contract coverage.
 *          factories (ofYears/ofMonths/ofWeeks/ofDays/of/from/parse/between) +
 *          exact exception matrix, null semantics, get/getUnits/getChronology,
 *          isZero/isNegative, getYears/getMonths/getDays, with/plus/minus
 *          with immutability, multipliedBy/negated/normalized/toTotalMonths,
 *          addTo/subtractFrom (month-end clamping), equals/hashCode/toString,
 *          and serialization round-trip + serialVersionUID.
 * @run main PeriodTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.List;
import java.time.DateTimeException;
import java.time.Duration;

public class PeriodTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static Period roundTrip(Period v) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(v);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (Period) ois.readObject();
    }

    static void testFactories() {
        check(Period.ZERO.equals(Period.of(0, 0, 0)), "ZERO");
        check(Period.ZERO.isZero() && !Period.ZERO.isNegative(), "ZERO flags");
        check(Period.of(1, 2, 3).toString().equals("P1Y2M3D"), "of");
        check(Period.ofYears(1).toString().equals("P1Y"), "ofYears");
        check(Period.ofMonths(12).toString().equals("P12M"), "ofMonths");
        check(Period.ofWeeks(3).toString().equals("P21D"), "ofWeeks");
        check(Period.ofDays(5).toString().equals("P5D"), "ofDays");
        check(Period.from(Period.of(1, 2, 3)).equals(Period.of(1, 2, 3)), "from");
        check(Period.parse("P1Y2M3D").equals(Period.of(1, 2, 3)), "parse");
        check(Period.parse("P-1Y").equals(Period.ofYears(-1)), "parse negative");
        check(Period.parse("P2W").equals(Period.ofDays(14)), "parse weeks");
        check(Period.parse(Period.of(3, 4, 5).toString()).equals(Period.of(3, 4, 5)),
                "parse/toString round-trip");
        Period between = Period.between(LocalDate.of(2024, 1, 15), LocalDate.of(2024, 3, 20));
        check(between.equals(Period.of(0, 2, 5)), "between");
        check(Period.between(LocalDate.of(2024, 3, 20), LocalDate.of(2024, 1, 15))
                .equals(Period.of(0, -2, -5)), "between negative");
    }

    static void testGet() {
        Period p = Period.of(1, 2, 3);
        List<java.time.temporal.TemporalUnit> units = p.getUnits();
        check(units.size() == 3 && units.contains(ChronoUnit.YEARS)
                && units.contains(ChronoUnit.MONTHS) && units.contains(ChronoUnit.DAYS), "getUnits");
        check(p.get(ChronoUnit.YEARS) == 1, "get YEARS");
        check(p.get(ChronoUnit.MONTHS) == 2, "get MONTHS");
        check(p.get(ChronoUnit.DAYS) == 3, "get DAYS");
        check(p.getYears() == 1 && p.getMonths() == 2 && p.getDays() == 3, "getYears/Months/Days");
        check(p.getChronology() == java.time.chrono.IsoChronology.INSTANCE, "getChronology");
        checkThrows(UnsupportedTemporalTypeException.class, () -> p.get(ChronoUnit.HOURS), "get HOURS");
    }

    static void testWith() {
        Period p = Period.of(1, 2, 3);
        check(p.withYears(5).equals(Period.of(5, 2, 3)), "withYears");
        check(p.withMonths(0).equals(Period.of(1, 0, 3)), "withMonths");
        check(p.withDays(10).equals(Period.of(1, 2, 10)), "withDays");
        check(p.equals(Period.of(1, 2, 3)), "immutability with");
    }

    static void testArithmetic() {
        Period p = Period.of(1, 2, 3);
        check(p.plus(Period.ofDays(4)).equals(Period.of(1, 2, 7)), "plus Period");
        check(p.plusYears(1).equals(Period.of(2, 2, 3)), "plusYears");
        check(p.plusMonths(1).equals(Period.of(1, 3, 3)), "plusMonths");
        check(p.plusDays(1).equals(Period.of(1, 2, 4)), "plusDays");
        check(p.minus(Period.ofDays(1)).equals(Period.of(1, 2, 2)), "minus Period");
        check(p.minusYears(1).equals(Period.of(0, 2, 3)), "minusYears");
        check(p.minusMonths(1).equals(Period.of(1, 1, 3)), "minusMonths");
        check(p.minusDays(1).equals(Period.of(1, 2, 2)), "minusDays");
        check(p.plusDays(-1).equals(Period.of(1, 2, 2)), "plus negative");
        check(p.equals(Period.of(1, 2, 3)), "immutability arithmetic");
    }

    static void testMultiplyNegateNormalize() {
        check(Period.of(1, 2, 0).multipliedBy(2).equals(Period.of(2, 4, 0)), "multipliedBy");
        check(Period.of(1, 2, 0).multipliedBy(-1).equals(Period.of(-1, -2, 0)), "multipliedBy negative");
        check(Period.of(1, 2, 0).negated().equals(Period.of(-1, -2, 0)), "negated");
        check(Period.ZERO.negated().equals(Period.ZERO), "negated ZERO");
        check(Period.ofMonths(12).normalized().equals(Period.ofYears(1)), "normalized P12M");
        check(Period.of(1, 13, 0).normalized().equals(Period.of(2, 1, 0)), "normalized P1Y13M");
        check(Period.of(0, -12, 0).normalized().equals(Period.ofYears(-1)), "normalized negative");
        check(Period.of(1, 2, 0).toTotalMonths() == 14, "toTotalMonths");
        check(Period.of(0, 6, 0).toTotalMonths() == 6, "toTotalMonths");
    }

    static void testAddSubtractFrom() {
        check(Period.ofDays(1).addTo(LocalDate.of(2024, 1, 31)).equals(LocalDate.of(2024, 2, 1)),
                "addTo day");
        check(Period.ofMonths(1).addTo(LocalDate.of(2024, 1, 31)).equals(LocalDate.of(2024, 2, 29)),
                "addTo month clamps");
        check(Period.ofYears(1).subtractFrom(LocalDate.of(2024, 2, 29)).equals(LocalDate.of(2023, 2, 28)),
                "subtractFrom year clamps");
        check(Period.of(1, 2, 3).addTo(LocalDate.of(2024, 1, 1)).equals(LocalDate.of(2025, 3, 4)),
                "addTo composite");
    }

    static void testComparison() {
        Period a = Period.of(1, 2, 3);
        Period b = Period.of(1, 2, 4);
        Period a2 = Period.of(1, 2, 3);
        check(a.equals(a2) && !a.equals(b), "equals");
        check(a.hashCode() == a2.hashCode(), "hashCode");
        check(a.equals(null) == false, "equals null");
        check(a.equals("P1Y2M3D") == false, "equals other type");
    }

    static void testSerialization() throws Exception {
        check(ObjectStreamClass.lookup(Period.class).getSerialVersionUID() == -3587258372562876L,
                "serialVersionUID");
        Period[] vals = { Period.ZERO, Period.of(1, 2, 3), Period.of(-1, -2, -3), Period.ofWeeks(3) };
        for (Period v : vals) {
            check(roundTrip(v).equals(v), "serialization round-trip " + v);
        }
    }

    static void testErrors() {
        checkThrows(DateTimeParseException.class, () -> Period.parse("P1X"), "parse bad");
        checkThrows(DateTimeParseException.class, () -> Period.parse("1Y"), "parse no P");
        checkThrows(ArithmeticException.class,
                () -> Period.ofYears(2).multipliedBy(Integer.MAX_VALUE), "multipliedBy overflow");
        checkThrows(UnsupportedTemporalTypeException.class,
                () -> Period.of(1, 0, 0).get(ChronoUnit.HOURS), "get unsupported");
    }

    static void testNullSemantics() {
        checkThrows(NullPointerException.class, () -> Period.parse(null), "parse null");
        checkThrows(NullPointerException.class, () -> Period.from(null), "from null");
        checkThrows(NullPointerException.class, () -> Period.between(null, LocalDate.now()), "between null");
        Period p = Period.of(1, 2, 3);
        checkThrows(NullPointerException.class, () -> p.plus(null), "plus null");
        checkThrows(NullPointerException.class, () -> p.minus(null), "minus null");
        checkThrows(NullPointerException.class, () -> p.addTo(null), "addTo null");
        checkThrows(NullPointerException.class, () -> p.subtractFrom(null), "subtractFrom null");
    }

    public static void main(String[] args) throws Exception {
        testFactories();
        testGet();
        testWith();
        testArithmetic();
        testMultiplyNegateNormalize();
        testAddSubtractFrom();
        testComparison();
        testSerialization();
        testErrors();
        testNullSemantics();
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
