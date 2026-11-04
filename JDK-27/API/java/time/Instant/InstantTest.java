/*
 * @test
 * @summary java.time.Instant conformance: white-box shape + black-box factories/parse,
 *          epoch getters, nano normalization, saturating plus, comparisons, and errors.
 * @run main InstantTest
 *
 * 设计案（黑盒）：
 * 1. 契约：Instant 是时间线上的单点（UTC 纳秒精度），值对象不可变，范围约 ±10 亿年。
 * 2. 场景：ofEpochSecond/ofEpochMilli/parse 构造，plus/minus/until 做时间运算，atZone/atOffset 转带时区类型。
 * 3. 组合链路：ofEpochSecond -> plusSeconds/plusMillis/plusNanos -> until(Instant)=Duration -> toEpochMilli -> atZone。
 * 4. 不变量：纳秒在 [0,999999999]，越界在构造时向秒进位；运算返回新实例；比较按时间线。
 * 5. 边界/异常：ofEpochSecond(0,-1) 归一化为 -1s+999999999ns；plus 溢出抛 ArithmeticException；plusSaturating 饱和到 MAX/MIN。
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Modifier;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.UnsupportedTemporalTypeException;

public class InstantTest {
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

    static Instant roundTrip(Instant v) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(v);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (Instant) ois.readObject();
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.Instant");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == Object.class, "superclass Object");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(java.time.temporal.Temporal.class.isAssignableFrom(c), "Temporal");
        check(java.lang.Comparable.class.isAssignableFrom(c), "Comparable");
    }

    static void testBlackBox() throws Exception {
        testFactories();
        testNanoNormalization();
        testArithmeticImmutability();
        testSaturatingPlus();
        testComparison();
        testConversions();
        testBoundaries();
        testErrors();
        testSerialization();
        testNullSemantics();
    }

    static void testFactories() {
        Instant i = Instant.ofEpochSecond(60, 500_000_000);
        check(i.getEpochSecond() == 60 && i.getNano() == 500_000_000, "ofEpochSecond/getters");
        check(Instant.ofEpochSecond(60).getNano() == 0, "ofEpochSecond(sec)");
        check(Instant.ofEpochMilli(1500).getEpochSecond() == 1
                && Instant.ofEpochMilli(1500).getNano() == 500_000_000, "ofEpochMilli");
        check(Instant.ofEpochMilli(-500).getEpochSecond() == -1
                && Instant.ofEpochMilli(-500).getNano() == 500_000_000, "ofEpochMilli negative");
        check(Instant.parse("1970-01-01T00:01:00.500Z").equals(i), "parse ISO");
        check(Instant.parse("2024-03-01T12:00:00Z").getEpochSecond() == 1709294400L,
                "parse known epoch second");
        check(Instant.EPOCH.equals(Instant.ofEpochSecond(0)), "EPOCH constant");
        check(i.toString().equals("1970-01-01T00:01:00.500Z"), "toString");
    }

    static void testNanoNormalization() {
        check(Instant.ofEpochSecond(0, -1).getEpochSecond() == -1
                && Instant.ofEpochSecond(0, -1).getNano() == 999_999_999, "nano negative carries");
        check(Instant.ofEpochSecond(0, 1_000_000_000).getEpochSecond() == 1
                && Instant.ofEpochSecond(0, 1_000_000_000).getNano() == 0, "nano overflow carries");
        check(Instant.ofEpochSecond(1, -1_000_000_000).getEpochSecond() == 0
                && Instant.ofEpochSecond(1, -1_000_000_000).getNano() == 0, "nano -1s carries");
    }

    static void testArithmeticImmutability() {
        Instant base = Instant.ofEpochSecond(100, 500_000_000);
        check(base.plusSeconds(25).equals(Instant.ofEpochSecond(125, 500_000_000)), "plusSeconds");
        check(base.plusMillis(1500).equals(Instant.ofEpochSecond(102, 0)), "plusMillis");
        check(base.plusNanos(600_000_000).equals(Instant.ofEpochSecond(101, 100_000_000)), "plusNanos carries");
        check(base.plus(Duration.ofSeconds(10)).equals(Instant.ofEpochSecond(110, 500_000_000)),
                "plus(Duration)");
        check(base.minusSeconds(50).equals(Instant.ofEpochSecond(50, 500_000_000)), "minusSeconds");
        check(base.minusMillis(500).equals(Instant.ofEpochSecond(100, 0)), "minusMillis");
        check(base.minusNanos(500_000_000).equals(Instant.ofEpochSecond(100, 0)), "minusNanos");
        check(base.truncatedTo(ChronoUnit.SECONDS).equals(Instant.ofEpochSecond(100)), "truncatedTo");
        check(base.until(Instant.ofEpochSecond(110, 500_000_000)).equals(Duration.ofSeconds(10)),
                "until(Instant)");
        check(base.equals(Instant.ofEpochSecond(100, 500_000_000)), "immutability");
    }

    static void testSaturatingPlus() {
        check(Instant.MAX.plusSaturating(Duration.ofSeconds(1)).equals(Instant.MAX),
                "plusSaturating saturates at MAX");
        check(Instant.MIN.plusSaturating(Duration.ofSeconds(-1)).equals(Instant.MIN),
                "plusSaturating saturates at MIN");
        check(Instant.ofEpochSecond(10).plusSaturating(Duration.ofSeconds(5))
                .equals(Instant.ofEpochSecond(15)), "plusSaturating normal");
        check(Instant.ofEpochSecond(10).plusSaturating(Duration.ofSeconds(-100))
                .equals(Instant.ofEpochSecond(-90)), "plusSaturating negative normal");
    }

    static void testComparison() {
        Instant a = Instant.ofEpochSecond(10);
        Instant b = Instant.ofEpochSecond(11);
        Instant a2 = Instant.ofEpochSecond(10);
        check(a.isBefore(b) && b.isAfter(a), "isBefore/isAfter");
        check(!a.isAfter(a2) && !a.isBefore(a2), "equal not before/after");
        check(a.compareTo(b) < 0 && a.compareTo(a2) == 0, "compareTo");
        check(a.equals(a2) && a.hashCode() == a2.hashCode(), "equals/hashCode");
    }

    static void testConversions() {
        Instant i = Instant.parse("2024-03-01T10:30:00Z");
        check(i.toEpochMilli() == i.getEpochSecond() * 1000L, "toEpochMilli");
        OffsetDateTime odt = i.atOffset(ZoneOffset.ofHours(2));
        check(odt.getHour() == 12 && odt.getOffset().equals(ZoneOffset.ofHours(2)), "atOffset");
        check(odt.toInstant().equals(i), "atOffset roundtrip instant");
        ZonedDateTime zdt = i.atZone(ZoneOffset.ofHours(-5));
        check(zdt.getHour() == 5 && zdt.toInstant().equals(i), "atZone preserves instant");
    }

    static void testBoundaries() {
        check(Instant.MAX.getEpochSecond() == 31556889864403199L && Instant.MAX.getNano() == 999_999_999,
              "MAX constant epoch second/nano");
        check(Instant.MIN.getEpochSecond() == -31557014167219200L, "MIN constant epoch second");
        check(Instant.EPOCH.toEpochMilli() == 0L, "EPOCH toEpochMilli == 0");
        check(Instant.parse("1970-01-01T00:00:00Z").equals(Instant.EPOCH), "parse epoch == EPOCH");
        check(Instant.parse("1970-01-01T00:00:00.000000001Z").getNano() == 1, "parse 1 ns");
        check(Instant.MAX.plus(Duration.ZERO).equals(Instant.MAX), "MAX plus ZERO stays MAX");
        check(Instant.MIN.plus(Duration.ZERO).equals(Instant.MIN), "MIN plus ZERO stays MIN");
        check(Instant.MAX.plusSaturating(Duration.ofSeconds(1)).equals(Instant.MAX), "MAX plusSaturating saturates");
        check(Instant.MIN.plusSaturating(Duration.ofSeconds(-1)).equals(Instant.MIN), "MIN plusSaturating saturates");
        check(Instant.ofEpochSecond(0, 1).compareTo(Instant.ofEpochSecond(0, 0)) > 0,
              "nano ordering in compareTo");
        check(Instant.ofEpochSecond(1, 999_999_999).toEpochMilli() == 1999L, "toEpochMilli truncates nanos");
        expectThrows(DateTimeException.class, () -> Instant.ofEpochSecond(Long.MIN_VALUE),
                "ofEpochSecond(Long.MIN_VALUE) exceeds range");
        expectThrows(ArithmeticException.class, () -> Instant.MAX.plus(Duration.ofSeconds(Long.MAX_VALUE)),
                "plus(Duration overflow)");
    }

    static void testErrors() {
        expectThrows(ArithmeticException.class, () -> Instant.ofEpochSecond(Long.MAX_VALUE, 1_000_000_000),
                "ofEpochSecond overflow");
        expectThrows(DateTimeException.class, () -> Instant.MAX.plusSeconds(1), "plusSeconds overflow");
        expectThrows(DateTimeException.class, () -> Instant.MIN.minusSeconds(1), "minusSeconds underflow");
        expectThrows(DateTimeParseException.class, () -> Instant.parse("2024-03-01T12:00:00"),
                "parse without zone");
        expectThrows(DateTimeParseException.class, () -> Instant.parse("garbage"), "parse garbage");
    }

    static void testSerialization() throws Exception {
        check(ObjectStreamClass.lookup(Instant.class).getSerialVersionUID() == -665713676816604388L,
                "serialVersionUID");
        Instant[] vals = { Instant.MIN, Instant.MAX, Instant.EPOCH, Instant.ofEpochSecond(-1, 999999999),
                Instant.ofEpochSecond(1234567890L, 123456789) };
        for (Instant v : vals) {
            check(roundTrip(v).equals(v), "serialization round-trip " + v);
        }
    }

    static void testNullSemantics() {
        expectThrows(NullPointerException.class, () -> Instant.parse(null), "parse null");
        expectThrows(NullPointerException.class, () -> Instant.from(null), "from null");
        expectThrows(NullPointerException.class, () -> Instant.now((Clock) null), "now null");
        Instant i = Instant.EPOCH;
        expectThrows(NullPointerException.class, () -> i.plus((Duration) null), "plus Duration null");
        expectThrows(NullPointerException.class, () -> i.plus(1, null), "plus unit null");
        expectThrows(NullPointerException.class, () -> i.minus((Duration) null), "minus Duration null");
        expectThrows(NullPointerException.class, () -> i.with((java.time.temporal.TemporalField) null, 1),
                "with field null");
        expectThrows(NullPointerException.class, () -> i.until(null, ChronoUnit.SECONDS), "until null");
        expectThrows(NullPointerException.class, () -> i.atOffset(null), "atOffset null");
        expectThrows(NullPointerException.class, () -> i.atZone(null), "atZone null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepInstant();
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


    static void testDeepInstant() {
        Instant i = Instant.ofEpochSecond(1709206530, 123456789);
        xeq(i.toString(), "2024-02-29T11:35:30.123456789Z", "toString");
        check(i.getEpochSecond() == 1709206530L, "epochSecond");
        check(i.getNano() == 123456789, "nano");
        xeq(Instant.ofEpochMilli(1709206530000L).toString(), "2024-02-29T11:35:30Z", "ofEpochMilli");
        xeq(Instant.ofEpochMilli(500).toString(), "1970-01-01T00:00:00.500Z", "milli fraction");
        xeq(Instant.ofEpochSecond(5, 500000).toString(), "1970-01-01T00:00:05.000500Z", "micro fraction");
        xeq(Instant.EPOCH.toString(), "1970-01-01T00:00:00Z", "EPOCH");
        xeq(Instant.MIN.toString(), "-1000000000-01-01T00:00:00Z", "MIN");
        xeq(Instant.MAX.toString(), "+1000000000-12-31T23:59:59.999999999Z", "MAX");
        xeq(Instant.ofEpochSecond(-1).toString(), "1969-12-31T23:59:59Z", "neg epoch");
        check(Instant.ofEpochSecond(0, -1).getEpochSecond() == -1, "nano neg borrow");
        check(Instant.ofEpochSecond(0, -1).getNano() == 999999999, "nano neg rem");
        check(Instant.ofEpochSecond(59, 60).getNano() == 60, "nano carry lazy");
        xact(() -> Instant.MAX.plusSeconds(1), DateTimeException.class, "Instant exceeds minimum or maximum instant", "MAX.plusSeconds");
        xact(() -> Instant.MIN.minusNanos(1), DateTimeException.class, "Instant exceeds minimum or maximum instant", "MIN.minusNanos");
        xact(() -> Instant.ofEpochSecond(Long.MAX_VALUE), DateTimeException.class, "Instant exceeds minimum or maximum instant", "ofEpochSecond MAX");
        xact(() -> Instant.ofEpochSecond(Long.MAX_VALUE, 1), DateTimeException.class, "Instant exceeds minimum or maximum instant", "ofEpochSecond MAX,n");
        xact(() -> Instant.ofEpochSecond(Long.MIN_VALUE / 2 - 1000), DateTimeException.class, "Instant exceeds minimum or maximum instant", "ofEpochSecond min mul");
        check(Instant.ofEpochMilli(Long.MAX_VALUE).toString().equals("+292278994-08-17T07:12:55.807Z"), "ofEpochMilli MAX ok");
        xeq(i.truncatedTo(ChronoUnit.SECONDS).toString(), "2024-02-29T11:35:30Z", "trunc SECONDS");
        xeq(i.truncatedTo(ChronoUnit.MINUTES).toString(), "2024-02-29T11:35:00Z", "trunc MINUTES");
        xeq(i.truncatedTo(ChronoUnit.MILLIS).toString(), "2024-02-29T11:35:30.123Z", "trunc MILLIS");
        xeq(i.truncatedTo(ChronoUnit.DAYS).toString(), "2024-02-29T00:00:00Z", "trunc DAYS ok on Instant");
        xact(() -> i.truncatedTo(ChronoUnit.FOREVER), UnsupportedTemporalTypeException.class,
            "Unit is too large to be used for truncation", "trunc FOREVER");
        xeq(i.plusSeconds(1).plusNanos(100).plusMillis(1).toString(), "2024-02-29T11:35:31.124456889Z", "plus chain");
        xeq(i.minusSeconds(1).minusNanos(1).minusMillis(1).toString(), "2024-02-29T11:35:29.122456788Z", "minus chain");
        check(i.plus(Duration.ofSeconds(1)).minus(Duration.ofSeconds(1)).equals(i), "dur sym");
        check(i.getLong(ChronoField.INSTANT_SECONDS) == 1709206530L, "getLong INSTANT_SECONDS");
        check(Instant.EPOCH.getLong(ChronoField.INSTANT_SECONDS) == 0, "EPOCH seconds");
        check(Instant.EPOCH.getLong(ChronoField.NANO_OF_SECOND) == 0, "getLong NOS at EPOCH");
        check(Instant.ofEpochSecond(0, 5).get(ChronoField.NANO_OF_SECOND) == 5, "get nano");
        check(Instant.ofEpochSecond(0, 5).get(ChronoField.MILLI_OF_SECOND) == 0, "milli of 5ns");
        xact(() -> Instant.EPOCH.getLong(ChronoField.DAY_OF_MONTH), UnsupportedTemporalTypeException.class,
            "Unsupported field: DayOfMonth", "getLong DOM");
        check(Instant.EPOCH.isSupported(ChronoField.INSTANT_SECONDS), "support INSTANT_SECONDS");
        check(!Instant.EPOCH.isSupported(ChronoField.DAY_OF_MONTH), "no DOM");
        check(Instant.EPOCH.isSupported(ChronoUnit.DAYS), "support unit DAYS");
        check(!Instant.EPOCH.isSupported(ChronoField.OFFSET_SECONDS), "no offset field");
        check(Instant.EPOCH.isBefore(i), "isBefore");
        check(i.isAfter(Instant.EPOCH), "isAfter");
        xeq(i.adjustInto(Instant.EPOCH).toString(), "2024-02-29T11:35:30.123456789Z", "adjustInto");
        check(i.query(TemporalQueries.precision()) == ChronoUnit.NANOS, "precision");
        check(i.query(TemporalQueries.zoneId()) == null, "zoneId null");
        check(Instant.parse("2024-02-29T10:15:30Z").equals(Instant.ofEpochSecond(1709201730)), "parse");
        check(Instant.parse("2024-02-29T10:15:30.123456789Z").getNano() == 123456789, "parse nano");
        xact(() -> Instant.parse("2024-02-29T10:15:30X"), DateTimeParseException.class,
            "Text '2024-02-29T10:15:30X' could not be parsed at index 19", "parse bad suffix");
        check(Instant.ofEpochSecond(100).until(Instant.ofEpochSecond(160), ChronoUnit.SECONDS) == 60, "until SECONDS");
        check(Instant.ofEpochSecond(100).until(Instant.ofEpochSecond(100, 5), ChronoUnit.NANOS) == 5, "until NANOS");
        check(Instant.ofEpochSecond(100).until(Instant.ofEpochSecond(160), ChronoUnit.MINUTES) == 1, "until MINUTES");
        xeq(Instant.EPOCH.atZone(ZoneOffset.ofHours(1)).toString(), "1970-01-01T01:00+01:00", "atZone");
        xeq(Instant.EPOCH.range(ChronoField.INSTANT_SECONDS).toString(), "-31557014167219200 - 31556889864403199", "range JDK27");
        xeq(Instant.ofEpochSecond(5).plusSaturating(Duration.ofSeconds(-10)).toString(), "1969-12-31T23:59:55Z", "plusSaturating");
        xeq(Instant.MAX.plusSaturating(Duration.ofSeconds(10)).toString(), Instant.MAX.toString(), "plusSaturating caps");
        xeq(Instant.ofEpochSecond(5).until(Instant.ofEpochSecond(8)), Duration.ofSeconds(3), "until Duration");
        xeq(Instant.ofEpochSecond(5).until(Instant.ofEpochSecond(8)).toString(), "PT3S", "until Duration str");
    }

}
