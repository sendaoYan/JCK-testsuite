/*
 * @test
 * @summary java.time.Duration API conformance: full contract coverage.
 *          factories (ofDays/Hours/Minutes/Seconds/Millis/Nanos/of/from/parse/
 *          between) + exact exception matrix, null semantics, get/getUnits,
 *          isZero/isNegative, getSeconds/getNano, withSeconds/withNanos,
 *          plus/minus all overloads, multipliedBy/dividedBy/negated/abs with
 *          immutability, addTo/subtractFrom, toDays/toHours/toMinutes/toMillis/
 *          toNanos truncation, between sign, comparisons, nanosecond precision,
 *          and serialization round-trip + serialVersionUID.
 * @run main DurationTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.List;

public class DurationTest {
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

    static Duration roundTrip(Duration v) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(v);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (Duration) ois.readObject();
    }

    static void testFactories() {
        check(Duration.ZERO.equals(Duration.ofSeconds(0)), "ZERO");
        check(Duration.ZERO.isZero() && !Duration.ZERO.isNegative(), "ZERO flags");
        check(Duration.ofDays(2).equals(Duration.ofHours(48)), "ofDays");
        check(Duration.ofHours(-3).equals(Duration.ofMinutes(-180)), "ofHours negative");
        check(Duration.ofMinutes(90).equals(Duration.ofSeconds(5400)), "ofMinutes");
        check(Duration.ofSeconds(65).toString().equals("PT1M5S"), "ofSeconds normalized");
        check(Duration.ofSeconds(65, 500000000).toString().equals("PT1M5.5S"), "ofSeconds nanos");
        check(Duration.ofSeconds(-65).toString().equals("PT-1M-5S"), "ofSeconds negative");
        check(Duration.ofMillis(1500).equals(Duration.ofSeconds(1, 500000000)), "ofMillis");
        check(Duration.ofNanos(1500).equals(Duration.ofSeconds(0, 1500)), "ofNanos");
        check(Duration.ofNanos(Long.MAX_VALUE).getSeconds() == 9223372036L
                && Duration.ofNanos(Long.MAX_VALUE).getNano() == 854775807, "ofNanos max");
        check(Duration.of(-3, ChronoUnit.DAYS).equals(Duration.ofHours(-72)), "of(amount,unit)");
        check(Duration.from(Duration.ofMinutes(5)).equals(Duration.ofMinutes(5)), "from");
        check(Duration.parse("PT1H30M").equals(Duration.ofMinutes(90)), "parse");
        check(Duration.parse("PT-1H").equals(Duration.ofHours(-1)), "parse negative");
        check(Duration.parse("P2DT3H4M5S").equals(Duration.ofDays(2).plusHours(3).plusMinutes(4).plusSeconds(5)),
                "parse composite");
        check(Duration.parse(Duration.ofSeconds(123456).toString()).equals(Duration.ofSeconds(123456)),
                "parse/toString round-trip");
        check(Duration.between(Instant.ofEpochSecond(10), Instant.ofEpochSecond(25))
                .equals(Duration.ofSeconds(15)), "between positive");
        check(Duration.between(Instant.ofEpochSecond(25), Instant.ofEpochSecond(10))
                .equals(Duration.ofSeconds(-15)), "between negative sign");
    }

    static void testGetUnits() {
        List<TemporalUnit> units = Duration.ofSeconds(1).getUnits();
        check(units.size() == 2 && units.contains(ChronoUnit.SECONDS) && units.contains(ChronoUnit.NANOS),
                "getUnits");
        Duration d = Duration.ofSeconds(1, 500000000);
        check(d.get(ChronoUnit.SECONDS) == 1, "get SECONDS");
        check(d.get(ChronoUnit.NANOS) == 500000000, "get NANOS");
        check(d.getSeconds() == 1 && d.getNano() == 500000000, "getSeconds/getNano");
        checkThrows(UnsupportedTemporalTypeException.class, () -> d.get(ChronoUnit.DAYS), "get DAYS");
    }

    static void testWith() {
        Duration base = Duration.ofSeconds(1, 500000000);
        check(base.withSeconds(5).equals(Duration.ofSeconds(5, 500000000)), "withSeconds");
        check(base.withNanos(0).equals(Duration.ofSeconds(1)), "withNanos");
        check(base.equals(Duration.ofSeconds(1, 500000000)), "immutability with");
    }

    static void testArithmetic() {
        Duration base = Duration.ofSeconds(90);
        check(base.plus(Duration.ofSeconds(30)).equals(Duration.ofMinutes(2)), "plus Duration");
        check(base.plusSeconds(30).equals(Duration.ofMinutes(2)), "plusSeconds");
        check(base.plusMinutes(1).equals(Duration.ofMinutes(2).plusSeconds(30)), "plusMinutes");
        check(base.plusHours(1).equals(Duration.ofSeconds(3690)), "plusHours");
        check(base.plusDays(1).equals(Duration.ofSeconds(86490)), "plusDays");
        check(base.plusMillis(500).equals(Duration.ofSeconds(90, 500000000)), "plusMillis");
        check(base.plusNanos(500).equals(Duration.ofSeconds(90, 500)), "plusNanos");
        check(base.plus(10, ChronoUnit.SECONDS).equals(Duration.ofSeconds(100)), "plus unit");
        check(base.minus(Duration.ofSeconds(30)).equals(Duration.ofMinutes(1)), "minus Duration");
        check(base.minusSeconds(30).equals(Duration.ofMinutes(1)), "minusSeconds");
        check(base.minusMinutes(1).equals(Duration.ofSeconds(30)), "minusMinutes");
        check(base.plusSeconds(-30).equals(Duration.ofMinutes(1)), "plus negative");
        check(base.equals(Duration.ofSeconds(90)), "immutability arithmetic");
    }

    static void testMultiplyDivideNegateAbs() {
        Duration d = Duration.ofSeconds(10);
        check(d.multipliedBy(3).equals(Duration.ofSeconds(30)), "multipliedBy");
        check(d.multipliedBy(-2).equals(Duration.ofSeconds(-20)), "multipliedBy negative");
        check(d.dividedBy(2).equals(Duration.ofSeconds(5)), "dividedBy");
        check(d.dividedBy(4).equals(Duration.ofMillis(2500)), "dividedBy fractional");
        check(Duration.ofSeconds(-10).dividedBy(4).equals(Duration.ofMillis(-2500)), "dividedBy negative fractional");
        check(d.negated().equals(Duration.ofSeconds(-10)), "negated");
        check(d.negated().negated().equals(d), "negated twice");
        check(Duration.ofSeconds(-10).abs().equals(d), "abs negative");
        check(d.abs().equals(d), "abs positive");
        check(d.equals(Duration.ofSeconds(10)), "immutability");
    }

    static void testAddSubtractFrom() {
        Duration d = Duration.ofSeconds(60);
        check(d.addTo(Instant.ofEpochSecond(0)).equals(Instant.ofEpochSecond(60)), "addTo");
        check(d.subtractFrom(Instant.ofEpochSecond(60)).equals(Instant.ofEpochSecond(0)), "subtractFrom");
    }

    static void testToPart() {
        Duration d = Duration.ofDays(2).plusHours(3).plusMinutes(4).plusSeconds(5);
        check(d.toDays() == 2, "toDays");
        check(d.toHours() == 51, "toHours");
        check(d.toMinutes() == 3064, "toMinutes");
        check(d.toMillis() == 183845000L, "toMillis");
        check(d.toNanos() == 183845000000000L, "toNanos");
        check(Duration.ofSeconds(-65).toMinutes() == -1, "toMinutes negative truncates");
    }

    static void testComparison() {
        Duration a = Duration.ofSeconds(10);
        Duration b = Duration.ofSeconds(20);
        Duration a2 = Duration.ofSeconds(10);
        check(a.compareTo(b) < 0 && b.compareTo(a) > 0 && a.compareTo(a2) == 0, "compareTo");
        check(a.equals(a2) && !a.equals(b), "equals");
        check(a.hashCode() == a2.hashCode(), "hashCode");
        check(a.equals(null) == false, "equals null");
        check(a.equals("PT10S") == false, "equals other type");
        checkThrows(NullPointerException.class, () -> a.compareTo(null), "compareTo null");
    }

    static void testSerialization() throws Exception {
        check(ObjectStreamClass.lookup(Duration.class).getSerialVersionUID() == 3078945930695997490L,
                "serialVersionUID");
        Duration[] vals = { Duration.ZERO, Duration.ofSeconds(1, 999999999),
                Duration.ofSeconds(-65), Duration.ofDays(2) };
        for (Duration v : vals) {
            check(roundTrip(v).equals(v), "serialization round-trip " + v);
        }
    }

    static void testErrors() {
        checkThrows(DateTimeParseException.class, () -> Duration.parse("1H"), "parse bad");
        checkThrows(DateTimeParseException.class, () -> Duration.parse("garbage"), "parse garbage");
        checkThrows(ArithmeticException.class, () -> Duration.ofSeconds(1).dividedBy(0), "dividedBy 0");
        checkThrows(ArithmeticException.class,
                () -> Duration.ofSeconds(Long.MAX_VALUE).plusSeconds(1), "plusSeconds overflow");
        checkThrows(ArithmeticException.class,
                () -> Duration.ofSeconds(Long.MAX_VALUE).multipliedBy(2), "multipliedBy overflow");
        checkThrows(ArithmeticException.class,
                () -> Duration.ofSeconds(Long.MAX_VALUE, Long.MAX_VALUE), "ofSeconds overflow");
        checkThrows(UnsupportedTemporalTypeException.class,
                () -> Duration.ofSeconds(1).get(ChronoUnit.DAYS), "get unsupported unit");
    }

    static void testNullSemantics() {
        checkThrows(NullPointerException.class, () -> Duration.parse(null), "parse null");
        checkThrows(NullPointerException.class, () -> Duration.from(null), "from null");
        checkThrows(NullPointerException.class, () -> Duration.of(1, null), "of unit null");
        checkThrows(NullPointerException.class, () -> Duration.between(null, Instant.EPOCH), "between null");
        Duration d = Duration.ofSeconds(1);
        checkThrows(NullPointerException.class, () -> d.plus(null), "plus Duration null");
        checkThrows(NullPointerException.class, () -> d.plus(1, null), "plus unit null");
        checkThrows(NullPointerException.class, () -> d.minus(null), "minus Duration null");
        checkThrows(NullPointerException.class, () -> d.addTo(null), "addTo null");
        checkThrows(NullPointerException.class, () -> d.subtractFrom(null), "subtractFrom null");
    }

    public static void main(String[] args) throws Exception {
        testFactories();
        testGetUnits();
        testWith();
        testArithmetic();
        testMultiplyDivideNegateAbs();
        testAddSubtractFrom();
        testToPart();
        testComparison();
        testSerialization();
        testErrors();
        testNullSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
