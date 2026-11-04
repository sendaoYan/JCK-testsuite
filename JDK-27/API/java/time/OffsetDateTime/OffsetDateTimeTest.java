/*
 * @test
 * @summary java.time.OffsetDateTime conformance: white-box shape + black-box factories/parse,
 *          offset conversions, timeline comparison, arithmetic, and errors.
 * @run main OffsetDateTimeTest
 *
 * 设计案（黑盒）：
 * 1. 契约：OffsetDateTime 是带固定 ZoneOffset 的日期时间，值对象不可变，映射到时间线某点。
 * 2. 场景：of/ofInstant/parse 构造，withOffsetSameLocal/withOffsetSameInstant 换偏移，atZoneSameInstant/toZonedDateTime/toInstant 转换。
 * 3. 组合链路：of -> withOffsetSameInstant（同 Instant 偏移换算）-> toInstant 校验 -> atZoneSameInstant -> timeLineOrder 比较。
 * 4. 不变量：compareTo 先比 Instant 再比本地时间；isEqual 只比 Instant；timeLineOrder 只比 Instant；运算返回新实例。
 * 5. 边界/异常：of 非法日期抛 DateTimeException；parse 非法抛 DateTimeParseException。
 */

import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.DateTimeException;
import java.time.format.DateTimeParseException;

public class OffsetDateTimeTest {
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
        Class<?> c = Class.forName("java.time.OffsetDateTime");
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
        OffsetDateTime odd = OffsetDateTime.of(2024, 3, 1, 10, 30, 45, 123_456_789,
                ZoneOffset.ofHoursMinutes(5, 30));
        check(OffsetDateTime.parse(odd.toString()).equals(odd), "round-trip +05:30");

        // epoch 与时间线
        OffsetDateTime epoch = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        check(epoch.toInstant().equals(Instant.EPOCH), "epoch toInstant");
        check(epoch.toEpochSecond() == 0L, "epoch toEpochSecond");

        // withOffsetSameInstant 往返
        check(odd.withOffsetSameInstant(ZoneOffset.UTC).withOffsetSameInstant(odd.getOffset())
                .equals(odd), "withOffsetSameInstant round-trip");

        // 年末跨年进位
        check(OffsetDateTime.of(2024, 12, 31, 23, 0, 0, 0, ZoneOffset.UTC).plusHours(2)
                .toLocalDateTime().equals(LocalDateTime.of(2025, 1, 1, 1, 0)),
                "year-end plusHours rollover");

        // atZoneSameInstant 保持 instant
        check(odd.atZoneSameInstant(ZoneId.of("+05:00")).toInstant().equals(odd.toInstant()),
                "atZoneSameInstant keeps instant");

        // compareTo 反对称
        OffsetDateTime oa = OffsetDateTime.of(2024, 3, 1, 10, 30, 0, 0, ZoneOffset.ofHours(2));
        OffsetDateTime ob = OffsetDateTime.of(2024, 3, 1, 11, 30, 0, 0, ZoneOffset.ofHours(2));
        check(oa.compareTo(ob) == -ob.compareTo(oa), "compareTo anti-symmetry");
        check(oa.isBefore(ob) && !ob.isBefore(oa), "isBefore anti-symmetry");

        // 非法月 0 等价类
        expectThrows(DateTimeException.class,
                () -> OffsetDateTime.of(2024, 0, 1, 0, 0, 0, 0, ZoneOffset.UTC), "of month 0");
    }

    static void testFactories() {
        OffsetDateTime odt = OffsetDateTime.of(2024, 3, 1, 10, 30, 45, 123_456_789, ZoneOffset.ofHours(2));
        check(odt.getYear() == 2024 && odt.getMonthValue() == 3 && odt.getDayOfMonth() == 1
                && odt.getHour() == 10 && odt.getMinute() == 30 && odt.getSecond() == 45
                && odt.getNano() == 123_456_789, "of/getters");
        check(odt.getOffset().equals(ZoneOffset.ofHours(2)), "getOffset");
        check(OffsetDateTime.of(LocalDateTime.of(2024, 3, 1, 10, 30), ZoneOffset.ofHours(2))
                .equals(OffsetDateTime.of(2024, 3, 1, 10, 30, 0, 0, ZoneOffset.ofHours(2))),
                "of(LocalDateTime,offset)");
        check(OffsetDateTime.ofInstant(Instant.parse("2024-03-01T08:30:00Z"), ZoneId.of("UTC"))
                .equals(OffsetDateTime.of(2024, 3, 1, 8, 30, 0, 0, ZoneOffset.UTC)),
                "ofInstant");
        check(OffsetDateTime.parse("2024-03-01T10:30:45.123456789+02:00").equals(odt), "parse");
        check(odt.toString().equals("2024-03-01T10:30:45.123456789+02:00"), "toString");
    }

    static void testOffsetConversions() {
        OffsetDateTime plus2 = OffsetDateTime.of(2024, 3, 1, 10, 30, 0, 0, ZoneOffset.ofHours(2));
        Instant instant = plus2.toInstant();
        check(instant.equals(Instant.parse("2024-03-01T08:30:00Z")), "toInstant");
        OffsetDateTime utc = plus2.withOffsetSameInstant(ZoneOffset.UTC);
        check(utc.getHour() == 8 && utc.getOffset().equals(ZoneOffset.UTC), "withOffsetSameInstant");
        check(utc.toInstant().equals(instant), "withOffsetSameInstant preserves instant");
        OffsetDateTime sameLocal = plus2.withOffsetSameLocal(ZoneOffset.UTC);
        check(sameLocal.getHour() == 10 && sameLocal.toInstant()
                .equals(Instant.parse("2024-03-01T10:30:00Z")), "withOffsetSameLocal keeps local");
        check(plus2.toLocalDateTime().equals(LocalDateTime.of(2024, 3, 1, 10, 30)), "toLocalDateTime");
        check(plus2.toLocalDate().equals(LocalDate.of(2024, 3, 1)), "toLocalDate");
        check(plus2.toLocalTime().equals(LocalTime.of(10, 30)), "toLocalTime");
        check(plus2.toOffsetTime().equals(OffsetTime.of(10, 30, 0, 0, ZoneOffset.ofHours(2))),
                "toOffsetTime");
        check(plus2.toEpochSecond() == instant.getEpochSecond(), "toEpochSecond");
        ZonedDateTime zdt = plus2.atZoneSameInstant(ZoneId.of("+05:00"));
        check(zdt.getHour() == 13 && zdt.toInstant().equals(instant), "atZoneSameInstant");
        ZonedDateTime zdtLocal = plus2.atZoneSimilarLocal(ZoneId.of("UTC"));
        check(zdtLocal.getHour() == 10, "atZoneSimilarLocal keeps local when no gap/overlap");
        check(plus2.toZonedDateTime().getZone().equals(ZoneOffset.ofHours(2)), "toZonedDateTime");
    }

    static void testComparison() {
        OffsetDateTime a = OffsetDateTime.of(2024, 3, 1, 10, 30, 0, 0, ZoneOffset.ofHours(2)); // 08:30Z
        OffsetDateTime sameInstantUtc = OffsetDateTime.of(2024, 3, 1, 8, 30, 0, 0, ZoneOffset.UTC); // 08:30Z
        OffsetDateTime earlier = OffsetDateTime.of(2024, 3, 1, 8, 0, 0, 0, ZoneOffset.UTC); // 08:00Z
        OffsetDateTime later = OffsetDateTime.of(2024, 3, 1, 9, 30, 0, 0, ZoneOffset.UTC); // 09:30Z
        check(a.isEqual(sameInstantUtc), "isEqual same instant");
        check(!a.isBefore(sameInstantUtc) && !a.isAfter(sameInstantUtc), "not before/after same instant");
        check(a.isAfter(earlier), "isAfter earlier instant");
        check(a.isBefore(later), "isBefore later instant");
        check(a.compareTo(sameInstantUtc) > 0, "compareTo instant tie -> local time");
        check(a.compareTo(earlier) > 0, "compareTo different instant");
        check(OffsetDateTime.timeLineOrder().compare(a, sameInstantUtc) == 0,
                "timeLineOrder ignores local on same instant");
        check(OffsetDateTime.timeLineOrder().compare(a, earlier) > 0, "timeLineOrder by instant only");
    }

    static void testArithmeticImmutability() {
        OffsetDateTime base = OffsetDateTime.of(2024, 1, 31, 23, 30, 0, 0, ZoneOffset.ofHours(2));
        check(base.plusHours(2).equals(OffsetDateTime.of(2024, 2, 1, 1, 30, 0, 0, ZoneOffset.ofHours(2))),
                "plusHours carries day");
        check(base.plusDays(1).equals(OffsetDateTime.of(2024, 2, 1, 23, 30, 0, 0, ZoneOffset.ofHours(2))),
                "plusDays");
        check(base.plusMonths(1).equals(OffsetDateTime.of(2024, 2, 29, 23, 30, 0, 0, ZoneOffset.ofHours(2))),
                "plusMonths clamps");
        check(base.minusDays(1).equals(OffsetDateTime.of(2024, 1, 30, 23, 30, 0, 0, ZoneOffset.ofHours(2))),
                "minusDays");
        check(base.equals(OffsetDateTime.of(2024, 1, 31, 23, 30, 0, 0, ZoneOffset.ofHours(2))),
                "immutability");
    }

    static void testErrors() {
        expectThrows(DateTimeException.class,
                () -> OffsetDateTime.of(2024, 2, 30, 0, 0, 0, 0, ZoneOffset.ofHours(2)), "of Feb 30");
        expectThrows(DateTimeException.class,
                () -> OffsetDateTime.of(2024, 1, 1, 24, 0, 0, 0, ZoneOffset.ofHours(2)), "of hour 24");
        expectThrows(DateTimeParseException.class, () -> OffsetDateTime.parse("2024-03-01T10:30"),
                "parse missing offset");
        expectThrows(DateTimeParseException.class, () -> OffsetDateTime.parse("garbage"), "parse garbage");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
