/*
 * @test
 * @summary java.time.Duration conformance: white-box shape + black-box factories/parse,
 *          component getters, arithmetic, part getters, comparisons, and overflow.
 * @run main DurationTest
 *
 * 设计案（黑盒）：
 * 1. 契约：Duration 是基于秒+纳秒的时间量，值对象不可变，表示 ISO-8601 持续时间。
 * 2. 场景：ofSeconds/ofMillis/ofNanos/ofDays/parse 构造，between 求差，plus/minus/multipliedBy/dividedBy 运算，
 *          toDays/toHours/.../toNanos 与 *Part 拆解。
 * 3. 组合链路：ofSeconds -> plus -> negated/abs -> toDaysPart/toHoursPart -> getUnits 顺序 -> compareTo。
 * 4. 不变量：纳秒在 [0,999999999]，秒进位；负值用负秒+正纳秒表示；运算返回新实例。
 * 5. 边界/异常：plus 溢出抛 ArithmeticException；parse 非法抛 DateTimeParseException。
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.time.LocalTime;
import java.time.temporal.UnsupportedTemporalTypeException;

public class DurationTest {
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

    static Duration roundTrip(Duration v) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(v);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (Duration) ois.readObject();
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.Duration");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == Object.class, "superclass Object");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(java.time.temporal.TemporalAmount.class.isAssignableFrom(c), "TemporalAmount");
        check(java.lang.Comparable.class.isAssignableFrom(c), "Comparable");
    }

    static void testBlackBox() throws Exception {
        testFactories();
        testGettersAndParts();
        testArithmeticImmutability();
        testComparison();
        testBoundaries();
        testErrors();
        testSerialization();
        testNullSemantics();
    }

    static void testFactories() {
        Duration d = Duration.ofSeconds(90, 500_000_000);
        check(d.getSeconds() == 90 && d.getNano() == 500_000_000, "ofSeconds/getters");
        check(Duration.ofSeconds(90).getNano() == 0, "ofSeconds(sec)");
        check(Duration.ofMillis(1500).getSeconds() == 1 && Duration.ofMillis(1500).getNano() == 500_000_000,
                "ofMillis");
        check(Duration.ofNanos(1_500_000_000L).getSeconds() == 1
                && Duration.ofNanos(1_500_000_000L).getNano() == 500_000_000, "ofNanos carries");
        check(Duration.ofMinutes(2).getSeconds() == 120, "ofMinutes");
        check(Duration.ofHours(1).getSeconds() == 3600, "ofHours");
        check(Duration.ofDays(1).getSeconds() == 86400, "ofDays");
        check(Duration.of(5, ChronoUnit.MINUTES).getSeconds() == 300, "of(long,unit)");
        check(Duration.ofMillis(-500).getSeconds() == -1 && Duration.ofMillis(-500).getNano() == 500_000_000,
                "negative uses negative seconds + positive nanos");
        check(Duration.parse("PT1H30M").equals(Duration.ofMinutes(90)), "parse PT1H30M");
        check(Duration.parse("P2DT3H4M").equals(Duration.ofMinutes(2 * 24 * 60 + 3 * 60 + 4)),
                "parse P2DT3H4M");
        check(Duration.parse("PT1.5S").getSeconds() == 1 && Duration.parse("PT1.5S").getNano() == 500_000_000,
                "parse fractional");
        check(Duration.parse("PT-0.5S").getSeconds() == -1
                && Duration.parse("PT-0.5S").getNano() == 500_000_000
                && Duration.parse("PT-0.5S").toMillis() == -500, "parse negative fractional");
        check(Duration.between(Instant.ofEpochSecond(10), Instant.ofEpochSecond(25)).getSeconds() == 15,
                "between");
    }

    static void testGettersAndParts() {
        Duration d = Duration.ofSeconds(90061); // 1 day 1 hour 1 minute 1 second
        check(d.toDays() == 1 && d.toHours() == 25 && d.toMinutes() == 1501 && d.toSeconds() == 90061,
                "toDays/toHours/toMinutes/toSeconds");
        check(d.toDaysPart() == 1 && d.toHoursPart() == 1 && d.toMinutesPart() == 1
                && d.toSecondsPart() == 1, "to*Part");
        check(d.toMillis() == 90061_000L && d.toNanos() == 90061_000_000_000L, "toMillis/toNanos");
        Duration frac = Duration.ofMillis(1500);
        check(frac.toSeconds() == 1 && frac.toMillisPart() == 500, "toMillisPart");
        check(frac.getSeconds() == 1 && frac.getNano() == 500_000_000, "getSeconds/getNano");
        List<ChronoUnit> units = Duration.ofSeconds(1).getUnits().stream()
                .map(u -> (ChronoUnit) u).toList();
        check(units.size() == 2 && units.get(0) == ChronoUnit.SECONDS && units.get(1) == ChronoUnit.NANOS,
                "getUnits [SECONDS, NANOS]");
    }

    static void testArithmeticImmutability() {
        Duration d = Duration.ofSeconds(90, 500_000_000);
        check(d.plus(Duration.ofSeconds(10)).equals(Duration.ofSeconds(100, 500_000_000)), "plus");
        check(d.plusSeconds(10).equals(Duration.ofSeconds(100, 500_000_000)), "plusSeconds");
        check(d.plusMillis(1500).equals(Duration.ofSeconds(92, 0)), "plusMillis carries");
        check(d.plusNanos(600_000_000).equals(Duration.ofSeconds(91, 100_000_000)), "plusNanos carries");
        check(d.plusHours(1).equals(Duration.ofSeconds(3690, 500_000_000)), "plusHours");
        check(d.minus(Duration.ofSeconds(10)).equals(Duration.ofSeconds(80, 500_000_000)), "minus");
        check(d.minusSeconds(10).equals(Duration.ofSeconds(80, 500_000_000)), "minusSeconds");
        check(d.multipliedBy(2).equals(Duration.ofSeconds(181, 0)), "multipliedBy");
        check(Duration.ofSeconds(10).dividedBy(2).equals(Duration.ofSeconds(5)), "dividedBy(long)");
        check(d.negated().equals(Duration.ofSeconds(-91, 500_000_000)), "negated");
        check(Duration.ofSeconds(-5).abs().equals(Duration.ofSeconds(5)), "abs");
        check(Duration.ofSeconds(-5).negated().equals(Duration.ofSeconds(5)), "negated negative");
        check(d.truncatedTo(ChronoUnit.SECONDS).equals(Duration.ofSeconds(90)), "truncatedTo");
        check(d.equals(Duration.ofSeconds(90, 500_000_000)), "immutability");
    }

    static void testComparison() {
        Duration a = Duration.ofSeconds(10);
        Duration b = Duration.ofSeconds(20);
        Duration a2 = Duration.ofSeconds(10);
        check(a.compareTo(b) < 0 && a.compareTo(a2) == 0, "compareTo");
        check(a.equals(a2) && a.hashCode() == a2.hashCode(), "equals/hashCode");
        check(Duration.ZERO.isZero() && !a.isZero(), "isZero");
        check(a.isPositive() && !b.isNegative(), "isPositive");
        check(Duration.ofSeconds(-5).isNegative(), "isNegative");
    }

    static void testBoundaries() {
        check(Duration.ZERO.getSeconds() == 0 && Duration.ZERO.getNano() == 0, "ZERO components");
        check(Duration.ZERO.isZero() && !Duration.ZERO.isPositive() && !Duration.ZERO.isNegative(),
              "ZERO is neither positive nor negative");
        check(Duration.ofSeconds(Long.MAX_VALUE).getSeconds() == Long.MAX_VALUE,
              "ofSeconds(Long.MAX_VALUE) no overflow");
        check(Duration.ofSeconds(Long.MIN_VALUE).getSeconds() == Long.MIN_VALUE,
              "ofSeconds(Long.MIN_VALUE)");
        check(Duration.ofMillis(Long.MAX_VALUE).getSeconds() == Long.MAX_VALUE / 1000,
              "ofMillis(Long.MAX_VALUE) truncates");
        check(Duration.ofNanos(Long.MAX_VALUE).getNano() == Long.MAX_VALUE % 1_000_000_000,
              "ofNanos(Long.MAX_VALUE) normalizes");
        check(Duration.ofSeconds(10).multipliedBy(0).equals(Duration.ZERO), "multipliedBy(0) == ZERO");
        check(Duration.ofSeconds(10).multipliedBy(-1).equals(Duration.ofSeconds(-10)), "multipliedBy(-1) == negated");
        check(Duration.ofSeconds(10).dividedBy(-2).equals(Duration.ofSeconds(-5)),
              "dividedBy negative divisor");
        check(Duration.between(Instant.ofEpochSecond(5), Instant.ofEpochSecond(5)).equals(Duration.ZERO),
              "between identical instants == ZERO");
        check(Duration.parse("PT0S").equals(Duration.ZERO), "parse PT0S == ZERO");
        check(Duration.parse("P0D").equals(Duration.ZERO), "parse P0D == ZERO");
        expectThrows(ArithmeticException.class, () -> Duration.ofSeconds(Long.MAX_VALUE).toNanos(),
                "toNanos overflow");
        expectThrows(ArithmeticException.class, () -> Duration.ofSeconds(Long.MAX_VALUE).toMillis(),
                "toMillis overflow");
        expectThrows(ArithmeticException.class, () -> Duration.ofSeconds(10).dividedBy(0),
                "dividedBy(0)");
    }

    static void testErrors() {
        expectThrows(ArithmeticException.class, () -> Duration.ofSeconds(Long.MAX_VALUE).plusSeconds(1),
                "plusSeconds overflow");
        expectThrows(ArithmeticException.class, () -> Duration.ofSeconds(Long.MAX_VALUE).multipliedBy(2),
                "multipliedBy overflow");
        expectThrows(DateTimeParseException.class, () -> Duration.parse("1H"), "parse missing P/T");
        expectThrows(DateTimeParseException.class, () -> Duration.parse("garbage"), "parse garbage");
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

    static void testNullSemantics() {
        expectThrows(NullPointerException.class, () -> Duration.parse(null), "parse null");
        expectThrows(NullPointerException.class, () -> Duration.from(null), "from null");
        expectThrows(NullPointerException.class, () -> Duration.of(1, null), "of unit null");
        expectThrows(NullPointerException.class, () -> Duration.between(null, Instant.EPOCH), "between null");
        Duration d = Duration.ofSeconds(1);
        expectThrows(NullPointerException.class, () -> d.plus(null), "plus null");
        expectThrows(NullPointerException.class, () -> d.plus(1, null), "plus unit null");
        expectThrows(NullPointerException.class, () -> d.minus(null), "minus null");
        expectThrows(NullPointerException.class, () -> d.addTo(null), "addTo null");
        expectThrows(NullPointerException.class, () -> d.subtractFrom(null), "subtractFrom null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
        xeq(Duration.ofSeconds(185).truncatedTo(ChronoUnit.MINUTES).toString(), "PT3M", "truncatedTo MINUTES");
        xeq(Duration.ofSeconds(185).truncatedTo(ChronoUnit.HOURS).toString(), "PT0S", "truncatedTo HOURS zero");
        xact(() -> Duration.ofDays(400).truncatedTo(ChronoUnit.YEARS), UnsupportedTemporalTypeException.class,
            "Unit is too large to be used for truncation", "truncatedTo YEARS");
        xeq(Duration.ofMinutes(90).toSeconds(), 5400L, "toSeconds");
        check(Duration.ofSeconds(1, 5).toNanosPart() == 5, "toNanosPart");
        check(Duration.ofHours(25).toHoursPart() == 1, "toHoursPart pos");
        check(Duration.ofHours(25).toDaysPart() == 1, "toDaysPart pos");
        check(Duration.ofSeconds(3690, 5).toHoursPart() == 1 && Duration.ofSeconds(3690, 5).toMinutesPart() == 1 && Duration.ofSeconds(3690, 5).toSecondsPart() == 30, "parts 3690s");
        check(Duration.ofSeconds(-90).toMinutesPart() == -1, "toMinutesPart neg truncates toward zero");
        check(Duration.ofSeconds(-90).toSecondsPart() == -30, "toSecondsPart neg");
        check(Duration.ofSeconds(-90).toHoursPart() == 0, "toHoursPart neg 0");
        check(Duration.ofHours(-25).toDaysPart() == -1 && Duration.ofHours(-25).toHoursPart() == -1, "neg -25H parts");
        check(!Duration.ofSeconds(-1).isPositive(), "isPositive neg");
        check(Duration.ofSeconds(1).isPositive(), "isPositive pos");
        check(!Duration.ZERO.isPositive(), "isPositive zero");
        check(Duration.MIN.isNegative(), "MIN negative");
        check(!Duration.MAX.isNegative(), "MAX positive");
        xact(() -> Duration.MIN.negated(), ArithmeticException.class, "Exceeds capacity of Duration: 9223372036854775808000000000", "MIN.negated");
        check(Duration.ofHours(3).dividedBy(Duration.ofMinutes(90)) == 2, "dividedBy(Duration)");
    }

}
