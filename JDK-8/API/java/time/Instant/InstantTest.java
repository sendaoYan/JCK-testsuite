/*
 * @test
 * @summary java.time.Instant API conformance: full contract coverage.
 *          factories (EPOCH/MIN/MAX, ofEpochSecond/ofEpochMilli/from/parse/now
 *          fixed Clock) + exact exception matrix, null semantics, getters
 *          (getEpochSecond/getNano), temporal accessors (isSupported/range/get/
 *          getLong/query), with/truncatedTo/plus/minus with immutability and
 *          nanosecond precision, until, atOffset/atZone/toEpochMilli,
 *          comparisons, epoch extremes, and serialization round-trip +
 *          serialVersionUID.
 * @run main InstantTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalQueries;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;

public class InstantTest {
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

    static Instant roundTrip(Instant v) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(v);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (Instant) ois.readObject();
    }

    static void testFactories() {
        check(Instant.EPOCH.equals(Instant.ofEpochSecond(0)), "EPOCH");
        check(Instant.EPOCH.getEpochSecond() == 0 && Instant.EPOCH.getNano() == 0, "EPOCH fields");
        check(Instant.MIN.getEpochSecond() == -31557014167219200L, "MIN epochSecond");
        check(Instant.MAX.getEpochSecond() == 31556889864403199L, "MAX epochSecond");
        check(Instant.ofEpochSecond(1).getEpochSecond() == 1, "ofEpochSecond(long)");
        check(Instant.ofEpochSecond(0, 999999999).equals(Instant.parse("1970-01-01T00:00:00.999999999Z")),
                "ofEpochSecond(long,long) nanos");
        check(Instant.ofEpochSecond(-1, 999999999).equals(Instant.parse("1969-12-31T23:59:59.999999999Z")),
                "ofEpochSecond negative + nanos");
        check(Instant.ofEpochMilli(1000).equals(Instant.ofEpochSecond(1)), "ofEpochMilli");
        check(Instant.ofEpochMilli(-1).equals(Instant.parse("1969-12-31T23:59:59.999Z")), "ofEpochMilli -1");
        check(Instant.parse("1970-01-01T00:00:00Z").equals(Instant.EPOCH), "parse");
        check(Instant.parse("1970-01-01T00:00:00.000000001Z").getNano() == 1, "parse nanos");
        check(Instant.from(Instant.EPOCH).equals(Instant.EPOCH), "from");
        check(Instant.now(Clock.fixed(Instant.ofEpochSecond(42), ZoneOffset.UTC))
                .equals(Instant.ofEpochSecond(42)), "now fixed Clock");
        check(Instant.EPOCH.toString().equals("1970-01-01T00:00:00Z"), "toString");
    }

    static void testTemporalAccessors() {
        Instant i = Instant.ofEpochSecond(1, 123456789);
        check(i.isSupported(ChronoField.NANO_OF_SECOND), "isSupported NANO_OF_SECOND");
        check(i.isSupported(ChronoField.INSTANT_SECONDS), "isSupported INSTANT_SECONDS");
        check(i.isSupported(ChronoUnit.SECONDS), "isSupported SECONDS");
        check(!i.isSupported(ChronoField.DAY_OF_MONTH), "isSupported DAY_OF_MONTH false");
        check(i.get(ChronoField.NANO_OF_SECOND) == 123456789, "get NANO_OF_SECOND");
        check(i.getLong(ChronoField.INSTANT_SECONDS) == 1, "getLong INSTANT_SECONDS");
        check(i.getLong(ChronoField.MICRO_OF_SECOND) == 123456, "getLong MICRO_OF_SECOND");
        check(i.getLong(ChronoField.MILLI_OF_SECOND) == 123, "getLong MILLI_OF_SECOND");
        ValueRange nr = i.range(ChronoField.NANO_OF_SECOND);
        check(nr.getMinimum() == 0 && nr.getMaximum() == 999999999, "range NANO_OF_SECOND");
        ValueRange ir = i.range(ChronoField.INSTANT_SECONDS);
        check(ir.getMinimum() == Long.MIN_VALUE && ir.getMaximum() == Long.MAX_VALUE,
                "range INSTANT_SECONDS");
        check(i.query(TemporalQueries.precision()) == ChronoUnit.NANOS, "query precision");
        check(i.query(TemporalQueries.zone()) == null, "query zone null");
        checkThrows(DateTimeException.class, () -> i.get(ChronoField.INSTANT_SECONDS),
                "get INSTANT_SECONDS too big for int");
        checkThrows(UnsupportedTemporalTypeException.class, () -> i.range(ChronoField.DAY_OF_MONTH),
                "range DAY_OF_MONTH");
        checkThrows(UnsupportedTemporalTypeException.class, () -> i.getLong(ChronoField.DAY_OF_MONTH),
                "getLong DAY_OF_MONTH");
    }

    static void testWithTruncateImmutable() {
        Instant base = Instant.ofEpochSecond(100, 500000000);
        check(base.with(ChronoField.NANO_OF_SECOND, 0).equals(Instant.ofEpochSecond(100)),
                "with NANO_OF_SECOND");
        check(base.with(ChronoField.INSTANT_SECONDS, 200).getEpochSecond() == 200,
                "with INSTANT_SECONDS");
        check(base.truncatedTo(ChronoUnit.SECONDS).equals(Instant.ofEpochSecond(100)),
                "truncatedTo SECONDS");
        check(Instant.ofEpochSecond(61).truncatedTo(ChronoUnit.MINUTES).equals(Instant.ofEpochSecond(60)),
                "truncatedTo MINUTES");
        check(Instant.ofEpochSecond(3601).truncatedTo(ChronoUnit.HOURS).equals(Instant.ofEpochSecond(3600)),
                "truncatedTo HOURS");
        check(base.truncatedTo(ChronoUnit.DAYS).equals(Instant.ofEpochSecond(0)), "truncatedTo DAYS");
        check(base.equals(Instant.ofEpochSecond(100, 500000000)), "immutability");
    }

    static void testArithmetic() {
        Instant base = Instant.ofEpochSecond(100, 999999999);
        check(base.plusSeconds(1).equals(Instant.ofEpochSecond(101, 999999999)), "plusSeconds");
        check(base.plusMillis(1).equals(Instant.ofEpochSecond(101, 999999)), "plusMillis");
        check(base.plusNanos(1).equals(Instant.ofEpochSecond(101)), "plusNanos roll");
        check(base.plus(Duration.ofSeconds(1)).equals(Instant.ofEpochSecond(101, 999999999)),
                "plus Duration");
        check(base.plus(1, ChronoUnit.SECONDS).equals(Instant.ofEpochSecond(101, 999999999)),
                "plus unit");
        check(base.minusSeconds(1).equals(Instant.ofEpochSecond(99, 999999999)), "minusSeconds");
        check(base.minusMillis(1).equals(Instant.ofEpochSecond(100, 998999999)), "minusMillis");
        check(base.minusNanos(1).equals(Instant.ofEpochSecond(100, 999999998)), "minusNanos");
        check(base.minus(Duration.ofNanos(1)).equals(Instant.ofEpochSecond(100, 999999998)),
                "minus Duration");
        check(base.plusSeconds(-1).equals(Instant.ofEpochSecond(99, 999999999)), "plus negative");
        check(base.equals(Instant.ofEpochSecond(100, 999999999)), "immutability arithmetic");
    }

    static void testUntil() {
        Instant a = Instant.ofEpochSecond(10);
        Instant b = Instant.ofEpochSecond(25);
        check(a.until(b, ChronoUnit.SECONDS) == 15, "until SECONDS");
        check(b.until(a, ChronoUnit.SECONDS) == -15, "until negative");
        check(a.until(b, ChronoUnit.MILLIS) == 15000, "until MILLIS");
        check(a.until(b, ChronoUnit.NANOS) == 15000000000L, "until NANOS");
        check(a.until(a, ChronoUnit.SECONDS) == 0, "until same");
    }

    static void testConversions() {
        Instant i = Instant.ofEpochSecond(100, 500000000);
        OffsetDateTime odt = i.atOffset(ZoneOffset.UTC);
        check(odt.getOffset().equals(ZoneOffset.UTC), "atOffset offset");
        check(odt.toInstant().equals(i), "atOffset round-trip");
        ZonedDateTime zdt = i.atZone(ZoneId.of("UTC"));
        check(zdt.getZone().equals(ZoneId.of("UTC")), "atZone zone");
        check(zdt.toInstant().equals(i), "atZone round-trip");
        check(i.toEpochMilli() == 100500, "toEpochMilli");
        check(Instant.EPOCH.toEpochMilli() == 0, "toEpochMilli epoch");
    }

    static void testComparison() {
        Instant a = Instant.ofEpochSecond(10);
        Instant b = Instant.ofEpochSecond(20);
        Instant a2 = Instant.ofEpochSecond(10);
        check(a.isBefore(b) && b.isAfter(a), "isBefore/isAfter");
        check(a.compareTo(b) < 0 && b.compareTo(a) > 0 && a.compareTo(a2) == 0, "compareTo");
        check(a.equals(a2) && !a.equals(b), "equals");
        check(a.hashCode() == a2.hashCode(), "hashCode");
        check(a.equals(null) == false, "equals null");
        check(a.equals("10") == false, "equals other type");
        checkThrows(NullPointerException.class, () -> a.compareTo(null), "compareTo null");
        checkThrows(NullPointerException.class, () -> a.isAfter(null), "isAfter null");
    }

    static void testSerialization() throws Exception {
        check(ObjectStreamClass.lookup(Instant.class).getSerialVersionUID() == -665713676816604388L,
                "serialVersionUID");
        Instant[] vals = { Instant.MIN, Instant.MAX, Instant.EPOCH, Instant.ofEpochSecond(-1, 999999999),
                Instant.ofEpochSecond(1234567890L, 123456789) };
        for (Instant v : vals) {
            Instant r = roundTrip(v);
            check(r.equals(v), "serialization round-trip " + v);
        }
    }

    static void testErrors() {
        checkThrows(DateTimeException.class, () -> Instant.ofEpochSecond(Long.MAX_VALUE, 999999999),
                "ofEpochSecond overflow");
        checkThrows(DateTimeException.class, () -> Instant.parse("1970-01-01"), "parse missing Z");
        checkThrows(DateTimeParseException.class, () -> Instant.parse("garbage"), "parse garbage");
        checkThrows(ArithmeticException.class, () -> Instant.ofEpochSecond(1).plusSeconds(Long.MAX_VALUE),
                "plusSeconds overflow");
        checkThrows(DateTimeException.class, () -> Instant.ofEpochSecond(1).minusSeconds(Long.MAX_VALUE),
                "minusSeconds overflow");
        checkThrows(UnsupportedTemporalTypeException.class,
                () -> Instant.EPOCH.truncatedTo(ChronoUnit.MONTHS), "truncatedTo MONTHS");
    }

    static void testNullSemantics() {
        checkThrows(NullPointerException.class, () -> Instant.parse(null), "parse null");
        checkThrows(NullPointerException.class, () -> Instant.from(null), "from null");
        checkThrows(NullPointerException.class, () -> Instant.now((Clock) null), "now null");
        Instant i = Instant.EPOCH;
        checkThrows(NullPointerException.class, () -> i.plus((Duration) null), "plus Duration null");
        checkThrows(NullPointerException.class, () -> i.plus(1, null), "plus unit null");
        checkThrows(NullPointerException.class, () -> i.minus((Duration) null), "minus Duration null");
        checkThrows(NullPointerException.class, () -> i.with((java.time.temporal.TemporalField) null, 1),
                "with field null");
        checkThrows(NullPointerException.class, () -> i.until(null, ChronoUnit.SECONDS), "until null");
        checkThrows(NullPointerException.class, () -> i.atOffset(null), "atOffset null");
        checkThrows(NullPointerException.class, () -> i.atZone(null), "atZone null");
    }

    public static void main(String[] args) throws Exception {
        testFactories();
        testTemporalAccessors();
        testWithTruncateImmutable();
        testArithmetic();
        testUntil();
        testConversions();
        testComparison();
        testSerialization();
        testErrors();
        testNullSemantics();
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
         xeq(Instant.EPOCH.range(ChronoField.INSTANT_SECONDS).toString(), "-9223372036854775808 - 9223372036854775807", "range JDK8");
    }

}
