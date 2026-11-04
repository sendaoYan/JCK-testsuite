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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
