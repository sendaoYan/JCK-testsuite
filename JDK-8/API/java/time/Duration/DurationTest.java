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
import java.time.LocalTime;

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
        testDeepDuration();
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


    static void testDeepDuration() {
        xeq(Duration.ofSeconds(90).toString(), "PT1M30S", "ofSeconds 90");
        xeq(Duration.ofMinutes(90).toString(), "PT1H30M", "ofMinutes 90");
        xeq(Duration.ofHours(25).toString(), "PT25H", "ofHours 25");
        xeq(Duration.ofDays(2).toString(), "PT48H", "ofDays 2");
        xeq(Duration.ofMillis(1500).toString(), "PT1.5S", "ofMillis");
        xeq(Duration.ofNanos(1000000000).toString(), "PT1S", "ofNanos");
        xeq(Duration.ofSeconds(3, 500000000).toString(), "PT3.5S", "ofSeconds s,n");
        xeq(Duration.ofSeconds(1, -2000000001).toString(), "PT-1.000000001S", "ofSeconds neg nanos normalize");
        xeq(Duration.ofSeconds(59, 60).toString(), "PT59.00000006S", "nano 60 lazy carry");
        xeq(Duration.ofMillis(-1).toString(), "PT-0.001S", "ofMillis -1");
        String[] iso = {"PT20.345S", "PT15M", "PT51H4M5.5S", "PT0S", "PT0.5S"};
        for (String s : iso) check(Duration.parse(s).toString().equals(s), "parse rt " + s);
        xeq(Duration.parse("P2DT3H4M5.5S").toString(), "PT51H4M5.5S", "parse folds days into hours");
        xeq(Duration.parse("-PT6H3M").toString(), "PT-6H-3M", "parse negative");
        xact(() -> Duration.parse("PT"), DateTimeParseException.class, "Text cannot be parsed to a Duration", "parse PT");
        xact(() -> Duration.parse("bogus"), DateTimeParseException.class, "Text cannot be parsed to a Duration", "parse bogus");
        xact(() -> Duration.parse("PT1D"), DateTimeParseException.class, "Text cannot be parsed to a Duration", "parse PT1D");
        xact(() -> Duration.parse(null), NullPointerException.class, null, "parse null");
        check(Duration.ofHours(49).toDays() == 2, "toDays");
        check(Duration.ofHours(49).toHours() == 49, "toHours");
        check(Duration.ofMinutes(90).toMinutes() == 90, "toMinutes");
        check(Duration.ofSeconds(90).toMillis() == 90000L, "toMillis");
        check(Duration.ofSeconds(90).toNanos() == 90000000000L, "toNanos");
        xact(() -> Duration.ofDays(Long.MAX_VALUE / 86400 + 1).toNanos(), ArithmeticException.class, "long overflow", "toNanos overflow");
        xact(() -> Duration.ofDays(Long.MAX_VALUE / 24 + 1000), ArithmeticException.class, "long overflow", "ofDays overflow");
        Duration d = Duration.ofSeconds(90);
        xeq(d.withSeconds(100).toString(), "PT1M40S", "withSeconds");
        xeq(d.withNanos(5).toString(), "PT1M30.000000005S", "withNanos");
        xeq(Duration.ofSeconds(-3, -4).toString(), "PT-3.000000004S", "neg parts");
        xeq(Duration.ofSeconds(-3).abs().toString(), "PT3S", "abs");
        xeq(d.negated().toString(), "PT-1M-30S", "negated");
        xeq(Duration.ofSeconds(10).dividedBy(3).toString(), "PT3.333333333S", "dividedBy trunc");
        xeq(Duration.ofSeconds(10, 500000000).dividedBy(3).toString(), "PT3.5S", "dividedBy trunc nano");
        check(d.plus(Duration.ofSeconds(30)).minus(Duration.ofSeconds(30)).equals(d), "sym");
        check(d.plusDays(1).minusDays(1).equals(d), "days sym");
        check(d.plusHours(1).minusHours(1).equals(d), "hours sym");
        check(d.plusMinutes(1).minusMinutes(1).equals(d), "minutes sym");
        check(d.plusMillis(1).minusMillis(1).equals(d), "millis sym");
        check(d.plusNanos(1).minusNanos(1).equals(d), "nanos sym");
        check(Duration.ofSeconds(60).equals(Duration.ofMinutes(1)), "equals cross factory");
        check(Duration.ofSeconds(60).hashCode() == Duration.ofMinutes(1).hashCode(), "hashCode cross");
        check(Duration.ZERO.equals(Duration.ofSeconds(0)), "ZERO");
        check(d.compareTo(Duration.ofMinutes(2)) < 0, "compareTo");
        check(Duration.ofSeconds(-1).compareTo(Duration.ZERO) < 0, "compareTo neg");
        xeq(Duration.ofSeconds(1, 1).get(ChronoUnit.SECONDS), 1L, "get SECONDS");
        xeq(Duration.ofSeconds(1, 1).get(ChronoUnit.NANOS), 1L, "get NANOS");
        xact(() -> d.get(ChronoUnit.MINUTES), UnsupportedTemporalTypeException.class, "Unsupported unit: Minutes", "get MINUTES");
        xeq(d.getUnits().toString(), "[Seconds, Nanos]", "units");
        xeq(Duration.ofHours(1).addTo(LocalTime.NOON).toString(), "13:00", "addTo");
        xeq(Duration.ofHours(1).subtractFrom(LocalTime.MIDNIGHT).toString(), "23:00", "subtractFrom");
        xact(() -> Duration.ZERO.plus(null), NullPointerException.class, null, "plus null");
        xeq(Duration.from(Duration.ofDays(1)).toString(), "PT24H", "from");
    }

}
