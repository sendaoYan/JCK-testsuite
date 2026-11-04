/*
 * @test
 * @summary java.time.OffsetTime conformance: white-box shape + black-box factories/parse,
 *          offset conversions, timeline comparison, arithmetic, and errors.
 * @run main OffsetTimeTest
 *
 * 设计案（黑盒）：
 * 1. 契约：OffsetTime 是带固定 ZoneOffset 的时间（纳秒精度），值对象不可变。
 * 2. 场景：of/of(LocalTime,offset)/parse 构造，withOffsetSameLocal/withOffsetSameInstant 换偏移，atDate 转 OffsetDateTime。
 * 3. 组合链路：of -> withOffsetSameInstant（同 Instant 偏移换算）-> toLocalTime 校验 -> atDate -> toEpochSecond。
 * 4. 不变量：compareTo 先比 Instant 再比本地时间；isEqual 只比 Instant；运算返回新实例。
 * 5. 边界/异常：of(24,0) 抛 DateTimeException；parse 非法抛 DateTimeParseException。
 */

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.DateTimeException;
import java.time.format.DateTimeParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalQueries;
import java.time.temporal.UnsupportedTemporalTypeException;

public class OffsetTimeTest {
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
        Class<?> c = Class.forName("java.time.OffsetTime");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == Object.class, "superclass Object");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(java.time.temporal.Temporal.class.isAssignableFrom(c), "Temporal");
        check(java.lang.Comparable.class.isAssignableFrom(c), "Comparable");
    }

    static void testBlackBox() {
        testFactories();
        testOffsetConversions();
        testComparison();
        testArithmeticImmutability();
        testErrors();

        // ---- 边界值/等价类/等价性（本棒补测） ----
        check(OffsetTime.parse(ot().toString()).equals(ot()), "round-trip basic");
        OffsetTime odd = OffsetTime.of(10, 30, 45, 123_456_789, ZoneOffset.ofHoursMinutes(5, 30));
        check(OffsetTime.parse(odd.toString()).equals(odd), "round-trip +05:30");

        // MIN/MAX 常量（offset 随极值而翻转）
        check(OffsetTime.MIN.equals(OffsetTime.of(LocalTime.MIDNIGHT, ZoneOffset.MAX)), "MIN constant");
        check(OffsetTime.MAX.equals(OffsetTime.of(LocalTime.MAX, ZoneOffset.MIN)), "MAX constant");

        // withOffsetSameInstant 往返
        check(odd.withOffsetSameInstant(ZoneOffset.UTC).withOffsetSameInstant(odd.getOffset())
                .equals(odd), "withOffsetSameInstant round-trip");

        // 纳秒边界回绕
        check(OffsetTime.of(LocalTime.MAX, ZoneOffset.UTC).plusNanos(1)
                .equals(OffsetTime.of(LocalTime.MIDNIGHT, ZoneOffset.UTC)), "MAX plusNanos(1) wraps");
        check(OffsetTime.of(LocalTime.MIDNIGHT, ZoneOffset.UTC).minusNanos(1)
                .equals(OffsetTime.of(LocalTime.MAX, ZoneOffset.UTC)), "MIDNIGHT minusNanos(1) wraps");

        // toEpochSecond 跨 offset 一致
        LocalDate d = LocalDate.of(2024, 3, 1);
        check(odd.toEpochSecond(d) == odd.withOffsetSameInstant(ZoneOffset.UTC).toEpochSecond(d),
                "toEpochSecond consistent across offsets");

        // compareTo 反对称
        OffsetTime ta = OffsetTime.of(10, 30, 0, 0, ZoneOffset.ofHours(2));
        OffsetTime tb = OffsetTime.of(11, 30, 0, 0, ZoneOffset.ofHours(2));
        check(ta.compareTo(tb) == -tb.compareTo(ta), "compareTo anti-symmetry");

        // 非法纳秒等价类
        expectThrows(DateTimeException.class,
                () -> OffsetTime.of(10, 0, 0, 1_000_000_000, ZoneOffset.UTC), "of nano 1e9");
    }

    static OffsetTime ot() {
        return OffsetTime.of(10, 30, 45, 123_456_789, ZoneOffset.ofHours(2));
    }

    static void testFactories() {
        OffsetTime ot = OffsetTime.of(10, 30, 45, 123_456_789, ZoneOffset.ofHours(2));
        check(ot.getHour() == 10 && ot.getMinute() == 30 && ot.getSecond() == 45
                && ot.getNano() == 123_456_789, "of/getters");
        check(ot.getOffset().equals(ZoneOffset.ofHours(2)), "getOffset");
        check(OffsetTime.of(LocalTime.of(10, 30), ZoneOffset.ofHours(2))
                .equals(OffsetTime.of(10, 30, 0, 0, ZoneOffset.ofHours(2))), "of(LocalTime,offset)");
        check(OffsetTime.parse("10:30:45.123456789+02:00").equals(ot), "parse");
        check(OffsetTime.parse("10:30+02:00").equals(OffsetTime.of(10, 30, 0, 0, ZoneOffset.ofHours(2))),
                "parse short");
        check(ot.toString().equals("10:30:45.123456789+02:00"), "toString");
    }

    static void testOffsetConversions() {
        OffsetTime plus2 = OffsetTime.of(10, 30, 0, 0, ZoneOffset.ofHours(2));
        OffsetTime utc = plus2.withOffsetSameInstant(ZoneOffset.UTC);
        check(utc.getHour() == 8 && utc.getOffset().equals(ZoneOffset.UTC), "withOffsetSameInstant");
        OffsetTime sameLocal = plus2.withOffsetSameLocal(ZoneOffset.UTC);
        check(sameLocal.getHour() == 10 && sameLocal.getOffset().equals(ZoneOffset.UTC),
                "withOffsetSameLocal");
        check(plus2.toLocalTime().equals(LocalTime.of(10, 30)), "toLocalTime");
        OffsetDateTime odt = plus2.atDate(LocalDate.of(2024, 3, 1));
        check(odt.equals(OffsetDateTime.of(2024, 3, 1, 10, 30, 0, 0, ZoneOffset.ofHours(2))),
                "atDate");
        check(plus2.toEpochSecond(LocalDate.of(2024, 3, 1)) == odt.toEpochSecond(),
                "toEpochSecond(date)");
    }

    static void testComparison() {
        OffsetTime a = OffsetTime.of(10, 30, 0, 0, ZoneOffset.ofHours(2)); // 08:30Z
        OffsetTime sameInstantUtc = OffsetTime.of(8, 30, 0, 0, ZoneOffset.UTC); // 08:30Z
        OffsetTime earlier = OffsetTime.of(8, 0, 0, 0, ZoneOffset.UTC); // 08:00Z
        OffsetTime later = OffsetTime.of(9, 0, 0, 0, ZoneOffset.UTC); // 09:00Z
        check(a.isEqual(sameInstantUtc), "isEqual same instant");
        check(!a.isBefore(sameInstantUtc) && !a.isAfter(sameInstantUtc), "not before/after same instant");
        check(a.isAfter(earlier), "isAfter earlier instant");
        check(a.isBefore(later), "isBefore later instant");
        check(a.compareTo(sameInstantUtc) > 0, "compareTo instant tie -> local time");
        check(a.compareTo(earlier) > 0, "compareTo different instant");
    }

    static void testArithmeticImmutability() {
        OffsetTime base = OffsetTime.of(23, 30, 0, 0, ZoneOffset.ofHours(2));
        check(base.plusHours(2).equals(OffsetTime.of(1, 30, 0, 0, ZoneOffset.ofHours(2))),
                "plusHours wraps");
        check(base.plusMinutes(40).equals(OffsetTime.of(0, 10, 0, 0, ZoneOffset.ofHours(2))),
                "plusMinutes wraps");
        check(base.minusHours(1).equals(OffsetTime.of(22, 30, 0, 0, ZoneOffset.ofHours(2))),
                "minusHours");
        check(base.withHour(0).equals(OffsetTime.of(0, 30, 0, 0, ZoneOffset.ofHours(2))), "withHour");
        check(base.equals(OffsetTime.of(23, 30, 0, 0, ZoneOffset.ofHours(2))), "immutability");
    }

    static void testErrors() {
        expectThrows(DateTimeException.class, () -> OffsetTime.of(24, 0, 0, 0, ZoneOffset.ofHours(2)),
                "of hour 24");
        expectThrows(DateTimeException.class, () -> OffsetTime.of(12, 60, 0, 0, ZoneOffset.ofHours(2)),
                "of minute 60");
        expectThrows(DateTimeParseException.class, () -> OffsetTime.parse("10:30"), "parse missing offset");
        expectThrows(DateTimeParseException.class, () -> OffsetTime.parse("garbage"), "parse garbage");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepOffsetTime();
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


    static void testDeepOffsetTime() {
        OffsetTime ot = OffsetTime.of(10, 15, 30, 0, ZoneOffset.ofHours(1));
        xeq(ot.toString(), "10:15:30+01:00", "toString");
        xeq(ot.toLocalTime().toString(), "10:15:30", "toLocalTime");
        check(ot.getOffset().equals(ZoneOffset.ofHours(1)), "getOffset");
        xeq(ot.withHour(5).toString(), "05:15:30+01:00", "withHour");
        xeq(ot.withOffsetSameLocal(ZoneOffset.ofHours(2)).toString(), "10:15:30+02:00", "withOffsetSameLocal");
        xeq(ot.withOffsetSameInstant(ZoneOffset.ofHours(2)).toString(), "11:15:30+02:00", "withOffsetSameInstant shifts clock");
        check(ot.getLong(ChronoField.OFFSET_SECONDS) == 3600L, "getLong offset");
        check(!ot.isSupported(ChronoField.DAY_OF_MONTH), "no DOM");
        check(ot.isSupported(ChronoField.OFFSET_SECONDS), "support offset");
        xact(() -> ot.plus(1, ChronoUnit.DAYS), UnsupportedTemporalTypeException.class,
            "Unsupported unit: Days", "plus DAYS");
        xact(() -> ot.getLong(ChronoField.DAY_OF_YEAR), UnsupportedTemporalTypeException.class,
            "Unsupported field: DayOfYear", "getLong DOY");
        check(ot.plusHours(1).minusHours(1).equals(ot), "hours sym");
        check(ot.plusMinutes(45).toString().equals("11:00:30+01:00"), "plusMinutes");
        check(OffsetTime.parse("10:15:30+01:00").equals(ot), "parse rt");
        check(ot.format(DateTimeFormatter.ISO_OFFSET_TIME).equals("10:15:30+01:00"), "ISO_OFFSET_TIME");
        check(ot.truncatedTo(ChronoUnit.MINUTES).toString().equals("10:15+01:00"), "trunc MINUTES");
        OffsetTime sameInstantOther = OffsetTime.of(5, 15, 30, 0, ZoneOffset.ofHours(-4));
        check(ot.isEqual(sameInstantOther), "isEqual across offsets");
        check(!ot.equals(sameInstantOther), "equals differs from isEqual");
        check(ot.compareTo(OffsetTime.of(10, 15, 30, 0, ZoneOffset.ofHours(2))) > 0, "compareTo equal wall time: later offset sorts first");
        check(OffsetTime.of(10, 15, 30, 0, ZoneOffset.ofHours(2)).compareTo(ot) < 0, "compareTo mirror");
        check(ot.query(TemporalQueries.offset()).equals(ZoneOffset.ofHours(1)), "query offset");
        check(ot.query(TemporalQueries.precision()) == ChronoUnit.NANOS, "precision");
        xact(() -> OffsetTime.of(24, 0, 0, 0, ZoneOffset.UTC), DateTimeException.class,
            "Invalid value for HourOfDay (valid values 0 - 23): 24", "hour 24");
        xact(() -> OffsetTime.of(10, 0, 0, 0, null), NullPointerException.class, "offset", "null offset");
        xeq(OffsetTime.of(LocalTime.of(10, 0), ZoneOffset.ofHours(-5)).atDate(LocalDate.of(2024, 2, 29)).toString(),
            "2024-02-29T10:00-05:00", "atDate");
        check(ot.equals(OffsetTime.of(LocalTime.of(10, 15, 30), ZoneOffset.ofHours(1))), "equals cross factory");
        check(ot.hashCode() == OffsetTime.parse(ot.toString()).hashCode(), "hash rt");
        xeq(OffsetTime.from(LocalDateTime.of(2024, 2, 29, 10, 15, 30).atOffset(ZoneOffset.ofHours(1))).toString(),
            "10:15:30+01:00", "from");
    }

}
