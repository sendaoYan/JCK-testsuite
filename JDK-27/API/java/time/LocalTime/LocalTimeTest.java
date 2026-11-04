/*
 * @test
 * @summary java.time.LocalTime conformance: white-box shape + black-box factories/parse,
 *          arithmetic with midnight wrap, comparisons, conversions, and errors.
 * @run main LocalTimeTest
 *
 * 设计案（黑盒）：
 * 1. 契约：LocalTime 是无时区时间（纳秒精度），值对象不可变，范围 00:00..23:59:59.999999999。
 * 2. 场景：of/ofSecondOfDay/ofNanoOfDay/parse 构造，plus/minus/with 做时间运算（跨午夜回绕），atDate/atOffset 转复合类型。
 * 3. 组合链路：of -> plusHours/plusMinutes/plusSeconds/plusNanos -> truncatedTo -> atDate -> toSecondOfDay/toNanoOfDay。
 * 4. 不变量：运算返回新实例原实例不变；equals/hashCode 按时分秒纳秒；跨午夜回绕正确。
 * 5. 边界/异常：of(24,0) 等越界抛 DateTimeException；parse 非法抛 DateTimeParseException。
 */

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.DateTimeException;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class LocalTimeTest {
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
        Class<?> c = Class.forName("java.time.LocalTime");
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
        testArithmeticImmutability();
        testComparison();
        testConversions();
        testErrors();
    }

    static void testFactories() {
        LocalTime t = LocalTime.of(13, 30, 45, 123_456_789);
        check(t.getHour() == 13 && t.getMinute() == 30 && t.getSecond() == 45
                && t.getNano() == 123_456_789, "of/getters");
        check(LocalTime.of(13, 30).equals(LocalTime.of(13, 30, 0, 0)), "of(int,int)");
        check(LocalTime.of(13, 30, 45).getNano() == 0, "of(int,int,int)");
        check(LocalTime.ofSecondOfDay(3725).equals(LocalTime.of(1, 2, 5)), "ofSecondOfDay");
        check(LocalTime.ofNanoOfDay(3725L * 1_000_000_000L).equals(LocalTime.of(1, 2, 5)),
                "ofNanoOfDay");
        check(LocalTime.parse("13:30:45.123456789").equals(t), "parse ISO");
        check(LocalTime.parse("13:30").equals(LocalTime.of(13, 30)), "parse ISO short");
        check(t.toString().equals("13:30:45.123456789"), "toString");
        check(LocalTime.MIDNIGHT.equals(LocalTime.of(0, 0)), "MIDNIGHT constant");
        check(LocalTime.NOON.equals(LocalTime.of(12, 0)), "NOON constant");
        check(LocalTime.MIN.equals(LocalTime.of(0, 0)), "MIN constant");
        check(LocalTime.MAX.equals(LocalTime.of(23, 59, 59, 999_999_999)), "MAX constant");
    }

    static void testArithmeticImmutability() {
        LocalTime t = LocalTime.of(23, 59, 30);
        check(t.plusHours(1).equals(LocalTime.of(0, 59, 30)), "plusHours wraps midnight");
        check(t.plusMinutes(40).equals(LocalTime.of(0, 39, 30)), "plusMinutes wraps");
        check(t.plusSeconds(31).equals(LocalTime.of(0, 0, 1)), "plusSeconds wraps");
        check(t.plusNanos(500_000_000).equals(LocalTime.of(23, 59, 30, 500_000_000)), "plusNanos");
        check(t.minusHours(24).equals(t), "minusHours 24 returns same time");
        check(t.minusMinutes(1).equals(LocalTime.of(23, 58, 30)), "minusMinutes");
        check(t.minusSeconds(31).equals(LocalTime.of(23, 58, 59)), "minusSeconds wraps");
        check(t.withHour(5).equals(LocalTime.of(5, 59, 30)), "withHour");
        check(t.withMinute(0).equals(LocalTime.of(23, 0, 30)), "withMinute");
        check(t.withSecond(0).equals(LocalTime.of(23, 59, 0)), "withSecond");
        check(t.withNano(7).equals(LocalTime.of(23, 59, 30, 7)), "withNano");
        check(t.truncatedTo(ChronoUnit.HOURS).equals(LocalTime.of(23, 0)), "truncatedTo HOURS");
        check(t.truncatedTo(ChronoUnit.MINUTES).equals(LocalTime.of(23, 59)), "truncatedTo MINUTES");
        check(t.equals(LocalTime.of(23, 59, 30)), "immutability");
    }

    static void testComparison() {
        LocalTime a = LocalTime.of(10, 0);
        LocalTime b = LocalTime.of(11, 0);
        LocalTime a2 = LocalTime.of(10, 0);
        check(a.isBefore(b) && b.isAfter(a), "isBefore/isAfter");
        check(!a.isBefore(a2) && !a.isAfter(a2), "equal not before/after");
        check(a.compareTo(b) < 0 && a.compareTo(a2) == 0, "compareTo");
        check(a.equals(a2) && a.hashCode() == a2.hashCode(), "equals/hashCode");
        check(!a.equals(b), "equals different");
    }

    static void testConversions() {
        LocalTime t = LocalTime.of(13, 30, 45);
        check(t.toSecondOfDay() == 13 * 3600 + 30 * 60 + 45, "toSecondOfDay");
        check(t.toNanoOfDay() == (13 * 3600 + 30 * 60 + 45) * 1_000_000_000L, "toNanoOfDay");
        check(t.atDate(LocalDate.of(2024, 3, 1))
                .equals(LocalDateTime.of(2024, 3, 1, 13, 30, 45)), "atDate");
        check(t.atOffset(ZoneOffset.ofHours(2))
                .equals(OffsetTime.of(13, 30, 45, 0, ZoneOffset.ofHours(2))), "atOffset");
    }

    static void testErrors() {
        expectThrows(DateTimeException.class, () -> LocalTime.of(24, 0), "of hour 24");
        expectThrows(DateTimeException.class, () -> LocalTime.of(-1, 0), "of hour negative");
        expectThrows(DateTimeException.class, () -> LocalTime.of(12, 60), "of minute 60");
        expectThrows(DateTimeException.class, () -> LocalTime.of(12, 0, 60), "of second 60");
        expectThrows(DateTimeException.class, () -> LocalTime.of(12, 0, 0, 1_000_000_000), "of nano overflow");
        expectThrows(DateTimeException.class, () -> LocalTime.ofSecondOfDay(86_400), "ofSecondOfDay 86400");
        expectThrows(DateTimeException.class, () -> LocalTime.ofNanoOfDay(86_400L * 1_000_000_000L),
                "ofNanoOfDay overflow");
        expectThrows(DateTimeParseException.class, () -> LocalTime.parse("25:00"), "parse hour 25");
        expectThrows(DateTimeParseException.class, () -> LocalTime.parse("garbage"), "parse garbage");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
