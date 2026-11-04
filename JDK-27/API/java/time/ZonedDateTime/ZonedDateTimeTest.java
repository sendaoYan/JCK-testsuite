/*
 * @test
 * @summary java.time.ZonedDateTime conformance: white-box shape + black-box factories/parse,
 *          zone conversions, arithmetic, comparisons, and errors.
 * @run main ZonedDateTimeTest
 *
 * 设计案（黑盒）：
 * 1. 契约：ZonedDateTime 是带 ZoneId 的日期时间，值对象不可变，把本地时间解析到时间线某点。
 * 2. 场景：of/of(LocalDateTime,zone)/ofInstant/parse 构造，withZoneSameInstant/withZoneSameLocal/withFixedOffsetZone 换时区，
 *          toInstant/toLocalDateTime/toOffsetDateTime 做转换，plus/minus 运算。
 * 3. 组合链路：ofInstant -> withZoneSameInstant（跨时区同一 Instant）-> toInstant 校验不变 -> toOffsetDateTime -> 比较。
 * 4. 不变量：withZoneSameInstant 保持 Instant 不变而本地时间变；withZoneSameLocal 保持本地时间而 Instant 变；运算返回新实例。
 * 5. 边界/异常：of 非法日期抛 DateTimeException；parse 非法抛 DateTimeParseException。
 */

import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.DateTimeException;
import java.time.format.DateTimeParseException;

public class ZonedDateTimeTest {
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
        Class<?> c = Class.forName("java.time.ZonedDateTime");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == Object.class, "superclass Object");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(java.time.temporal.Temporal.class.isAssignableFrom(c), "Temporal");
        check(java.time.chrono.ChronoZonedDateTime.class.isAssignableFrom(c), "ChronoZonedDateTime");
    }

    static void testBlackBox() {
        testFactories();
        testZoneConversions();
        testArithmeticImmutability();
        testComparison();
        testErrors();
    }

    static void testFactories() {
        ZonedDateTime zdt = ZonedDateTime.of(2024, 3, 1, 12, 30, 45, 123_456_789, ZoneOffset.UTC);
        check(zdt.getYear() == 2024 && zdt.getMonthValue() == 3 && zdt.getDayOfMonth() == 1
                && zdt.getHour() == 12 && zdt.getMinute() == 30 && zdt.getSecond() == 45
                && zdt.getNano() == 123_456_789, "of/getters");
        check(zdt.getOffset().equals(ZoneOffset.UTC), "getOffset");
        check(zdt.getZone().equals(ZoneOffset.UTC), "getZone");
        check(ZonedDateTime.of(LocalDateTime.of(2024, 3, 1, 12, 30), ZoneId.of("UTC"))
                .equals(ZonedDateTime.of(2024, 3, 1, 12, 30, 0, 0, ZoneId.of("UTC"))),
                "of(LocalDateTime,zone)");
        check(ZonedDateTime.ofInstant(Instant.parse("2024-03-01T12:00:00Z"), ZoneId.of("UTC"))
                .equals(ZonedDateTime.of(2024, 3, 1, 12, 0, 0, 0, ZoneId.of("UTC"))),
                "ofInstant");
        check(ZonedDateTime.parse("2024-03-01T12:30:45.123456789Z").equals(zdt), "parse Z");
        check(ZonedDateTime.parse("2024-03-01T12:30:45.123456789+00:00").equals(zdt), "parse +00:00");
        check(zdt.toString().equals("2024-03-01T12:30:45.123456789Z"), "toString UTC");
    }

    static void testZoneConversions() {
        Instant instant = Instant.parse("2024-03-01T12:00:00Z");
        ZonedDateTime utc = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
        ZonedDateTime plus2 = utc.withZoneSameInstant(ZoneId.of("+02:00"));
        check(plus2.getHour() == 14 && plus2.getOffset().equals(ZoneOffset.ofHours(2)),
                "withZoneSameInstant local shifts");
        check(plus2.toInstant().equals(instant), "withZoneSameInstant preserves instant");
        ZonedDateTime sameLocal = utc.withZoneSameLocal(ZoneOffset.ofHours(2));
        check(sameLocal.getHour() == 12 && sameLocal.getOffset().equals(ZoneOffset.ofHours(2)),
                "withZoneSameLocal keeps local");
        check(sameLocal.toInstant().equals(Instant.parse("2024-03-01T10:00:00Z")),
                "withZoneSameLocal shifts instant");
        ZonedDateTime fixed = plus2.withFixedOffsetZone();
        check(fixed.getZone().equals(ZoneOffset.ofHours(2)) && fixed.toInstant().equals(instant),
                "withFixedOffsetZone collapses to fixed offset");
        check(utc.toLocalDateTime().equals(LocalDateTime.of(2024, 3, 1, 12, 0)), "toLocalDateTime");
        check(utc.toLocalDate().equals(LocalDate.of(2024, 3, 1)), "toLocalDate");
        check(utc.toLocalTime().equals(LocalTime.of(12, 0)), "toLocalTime");
        OffsetDateTime odt = plus2.toOffsetDateTime();
        check(odt.equals(OffsetDateTime.of(2024, 3, 1, 14, 0, 0, 0, ZoneOffset.ofHours(2))),
                "toOffsetDateTime");
    }

    static void testArithmeticImmutability() {
        ZonedDateTime base = ZonedDateTime.of(2024, 1, 31, 23, 30, 0, 0, ZoneId.of("UTC"));
        check(base.plusHours(2).equals(ZonedDateTime.of(2024, 2, 1, 1, 30, 0, 0, ZoneId.of("UTC"))),
                "plusHours carries day");
        check(base.plusDays(1).equals(ZonedDateTime.of(2024, 2, 1, 23, 30, 0, 0, ZoneId.of("UTC"))),
                "plusDays");
        check(base.plusMonths(1).equals(ZonedDateTime.of(2024, 2, 29, 23, 30, 0, 0, ZoneId.of("UTC"))),
                "plusMonths clamps");
        check(base.plusYears(1).equals(ZonedDateTime.of(2025, 1, 31, 23, 30, 0, 0, ZoneId.of("UTC"))),
                "plusYears");
        check(base.minusDays(1).equals(ZonedDateTime.of(2024, 1, 30, 23, 30, 0, 0, ZoneId.of("UTC"))),
                "minusDays");
        check(base.withHour(0).equals(ZonedDateTime.of(2024, 1, 31, 0, 30, 0, 0, ZoneId.of("UTC"))),
                "withHour");
        check(base.equals(ZonedDateTime.of(2024, 1, 31, 23, 30, 0, 0, ZoneId.of("UTC"))),
                "immutability");
    }

    static void testComparison() {
        ZonedDateTime a = ZonedDateTime.of(2024, 3, 1, 12, 0, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime b = ZonedDateTime.of(2024, 3, 1, 13, 0, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime sameInstant = ZonedDateTime.ofInstant(a.toInstant(), ZoneOffset.ofHours(2));
        check(a.isBefore(b) && b.isAfter(a), "isBefore/isAfter");
        check(a.isEqual(sameInstant) && !a.isBefore(sameInstant) && !a.isAfter(sameInstant),
                "isEqual by instant across zones");
        check(a.compareTo(b) < 0, "compareTo");
        check(a.equals(sameInstant) == false, "equals false across zones (zone included)");
    }

    static void testErrors() {
        expectThrows(DateTimeException.class, () -> ZonedDateTime.of(2024, 2, 30, 0, 0, 0, 0, ZoneId.of("UTC")),
                "of Feb 30");
        expectThrows(DateTimeException.class, () -> ZonedDateTime.of(2024, 1, 1, 24, 0, 0, 0, ZoneId.of("UTC")),
                "of hour 24");
        expectThrows(DateTimeParseException.class, () -> ZonedDateTime.parse("2024-03-01T12:00"),
                "parse missing zone");
        expectThrows(DateTimeParseException.class, () -> ZonedDateTime.parse("garbage"), "parse garbage");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
