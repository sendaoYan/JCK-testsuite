/*
 * @test
 * @summary java.time.LocalDateTime conformance: white-box shape + black-box factories/parse,
 *          arithmetic with carry, comparisons, zone/offset conversions, and errors.
 * @run main LocalDateTimeTest
 *
 * 设计案（黑盒）：
 * 1. 契约：LocalDateTime 是 ISO-8601 日期+时间（无时区），值对象不可变，纳秒精度。
 * 2. 场景：of/of(LocalDate,LocalTime)/parse 构造，plus/minus/with 运算（跨日/月/年进位），toLocalDate/toLocalTime/atZone/atOffset 拆分与转换。
 * 3. 组合链路：of -> plusHours(进位) -> plusMonths -> toLocalDate/toLocalTime -> atZone(UTC).toInstant -> 比较。
 * 4. 不变量：运算返回新实例原实例不变；equals/hashCode 按日期+时间；比较按时间线（本地序）。
 * 5. 边界/异常：of(2,30) 等非法抛 DateTimeException；parse 非法抛 DateTimeParseException；月份末加月做钳制。
 */

import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.DateTimeException;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class LocalDateTimeTest {
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
        Class<?> c = Class.forName("java.time.LocalDateTime");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == Object.class, "superclass Object");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(java.time.temporal.Temporal.class.isAssignableFrom(c), "Temporal");
        check(java.time.temporal.TemporalAdjuster.class.isAssignableFrom(c), "TemporalAdjuster");
        check(java.time.chrono.ChronoLocalDateTime.class.isAssignableFrom(c), "ChronoLocalDateTime");
    }

    static void testBlackBox() {
        testFactories();
        testArithmeticImmutability();
        testComparison();
        testConversions();
        testErrors();
    }

    static void testFactories() {
        LocalDateTime dt = LocalDateTime.of(2024, 3, 1, 12, 30, 45, 123_456_789);
        check(dt.getYear() == 2024 && dt.getMonthValue() == 3 && dt.getDayOfMonth() == 1
                && dt.getHour() == 12 && dt.getMinute() == 30 && dt.getSecond() == 45
                && dt.getNano() == 123_456_789, "of/getters");
        check(dt.getMonth() == Month.MARCH, "getMonth");
        check(dt.getDayOfYear() == 61, "getDayOfYear");
        check(LocalDateTime.of(LocalDate.of(2024, 3, 1), LocalTime.of(12, 30))
                .equals(LocalDateTime.of(2024, 3, 1, 12, 30)), "of(LocalDate,LocalTime)");
        check(LocalDateTime.parse("2024-03-01T12:30:45.123456789").equals(dt), "parse ISO");
        check(LocalDateTime.parse("2024-03-01T12:30").equals(LocalDateTime.of(2024, 3, 1, 12, 30)),
                "parse ISO short");
        check(dt.toString().equals("2024-03-01T12:30:45.123456789"), "toString");
        check(LocalDateTime.MIN.equals(LocalDateTime.of(-999999999, 1, 1, 0, 0)), "MIN constant");
        check(LocalDateTime.MAX.equals(LocalDateTime.of(999999999, 12, 31, 23, 59, 59, 999_999_999)),
                "MAX constant");
    }

    static void testArithmeticImmutability() {
        LocalDateTime base = LocalDateTime.of(2024, 1, 31, 23, 30);
        check(base.plusHours(2).equals(LocalDateTime.of(2024, 2, 1, 1, 30)), "plusHours carries day");
        check(base.plusDays(1).equals(LocalDateTime.of(2024, 2, 1, 23, 30)), "plusDays");
        check(base.plusMonths(1).equals(LocalDateTime.of(2024, 2, 29, 23, 30)), "plusMonths clamps");
        check(base.plusYears(1).equals(LocalDateTime.of(2025, 1, 31, 23, 30)), "plusYears");
        check(base.plusWeeks(1).equals(LocalDateTime.of(2024, 2, 7, 23, 30)), "plusWeeks");
        check(base.plusMinutes(40).equals(LocalDateTime.of(2024, 2, 1, 0, 10)), "plusMinutes carries");
        check(base.minusHours(24).equals(LocalDateTime.of(2024, 1, 30, 23, 30)), "minusHours 24");
        check(base.minusDays(1).equals(LocalDateTime.of(2024, 1, 30, 23, 30)), "minusDays");
        check(base.withHour(0).equals(LocalDateTime.of(2024, 1, 31, 0, 30)), "withHour");
        check(base.withDayOfMonth(1).equals(LocalDateTime.of(2024, 1, 1, 23, 30)), "withDayOfMonth");
        check(base.withMonth(2).equals(LocalDateTime.of(2024, 2, 29, 23, 30)), "withMonth clamps");
        check(base.truncatedTo(ChronoUnit.HOURS).equals(LocalDateTime.of(2024, 1, 31, 23, 0)),
                "truncatedTo HOURS");
        check(base.equals(LocalDateTime.of(2024, 1, 31, 23, 30)), "immutability");
    }

    static void testComparison() {
        LocalDateTime a = LocalDateTime.of(2024, 3, 1, 12, 0);
        LocalDateTime b = LocalDateTime.of(2024, 3, 1, 13, 0);
        LocalDateTime a2 = LocalDateTime.of(2024, 3, 1, 12, 0);
        check(a.isBefore(b) && b.isAfter(a), "isBefore/isAfter");
        check(a.isEqual(a2) && !a.isAfter(a2) && !a.isBefore(a2), "isEqual");
        check(a.compareTo(b) < 0 && a.compareTo(a2) == 0, "compareTo");
        check(a.equals(a2) && a.hashCode() == a2.hashCode(), "equals/hashCode");
        check(!a.equals(b), "equals different");
    }

    static void testConversions() {
        LocalDateTime dt = LocalDateTime.of(2024, 3, 1, 12, 30);
        check(dt.toLocalDate().equals(LocalDate.of(2024, 3, 1)), "toLocalDate");
        check(dt.toLocalTime().equals(LocalTime.of(12, 30)), "toLocalTime");
        OffsetDateTime odt = dt.atOffset(ZoneOffset.ofHours(2));
        check(odt.equals(OffsetDateTime.of(2024, 3, 1, 12, 30, 0, 0, ZoneOffset.ofHours(2))),
                "atOffset");
        check(odt.toInstant().equals(Instant.parse("2024-03-01T10:30:00Z")), "atOffset instant");
        ZonedDateTime zdt = dt.atZone(ZoneId.of("UTC"));
        check(zdt.equals(ZonedDateTime.of(2024, 3, 1, 12, 30, 0, 0, ZoneId.of("UTC"))), "atZone");
        check(zdt.toInstant().equals(Instant.parse("2024-03-01T12:30:00Z")), "atZone UTC instant");
        ZonedDateTime zdt2 = dt.atZone(ZoneOffset.ofHours(-3));
        check(zdt2.toInstant().equals(Instant.parse("2024-03-01T15:30:00Z")), "atZone -03 instant");
    }

    static void testErrors() {
        expectThrows(DateTimeException.class, () -> LocalDateTime.of(2024, 2, 30, 0, 0), "of Feb 30");
        expectThrows(DateTimeException.class, () -> LocalDateTime.of(2024, 13, 1, 0, 0), "of month 13");
        expectThrows(DateTimeException.class, () -> LocalDateTime.of(2024, 1, 1, 24, 0), "of hour 24");
        expectThrows(DateTimeException.class, () -> LocalDateTime.of(2024, 1, 1, 0, 60), "of minute 60");
        expectThrows(DateTimeParseException.class, () -> LocalDateTime.parse("2024-02-30T00:00"),
                "parse Feb 30");
        expectThrows(DateTimeParseException.class, () -> LocalDateTime.parse("not-a-datetime"),
                "parse garbage");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
