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

import java.lang.reflect.Modifier;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

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

    static void testBlackBox() {
        testFactories();
        testGettersAndParts();
        testArithmeticImmutability();
        testComparison();
        testBoundaries();
        testErrors();
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
