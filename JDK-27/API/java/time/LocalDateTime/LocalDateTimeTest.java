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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Modifier;
import java.time.Clock;
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
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalQueries;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.time.Duration;
import java.time.Period;
import java.time.format.DateTimeFormatter;

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

    static LocalDateTime roundTrip(LocalDateTime v) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(v);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (LocalDateTime) ois.readObject();
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

    static void testBlackBox() throws Exception {
        testFactories();
        testArithmeticImmutability();
        testComparison();
        testConversions();
        testErrors();

        // ---- 边界值/等价类/等价性（本棒补测） ----
        check(LocalDateTime.parse(LocalDateTime.MIN.toString()).equals(LocalDateTime.MIN),
                "round-trip MIN");
        check(LocalDateTime.parse(LocalDateTime.MAX.toString()).equals(LocalDateTime.MAX),
                "round-trip MAX");
        check(LocalDateTime.parse(LocalDateTime.of(-1, 1, 1, 0, 0).toString())
                .equals(LocalDateTime.of(-1, 1, 1, 0, 0)), "round-trip negative year");

        // 极值邻域
        check(LocalDateTime.MIN.plusNanos(1)
                .equals(LocalDateTime.of(-999999999, 1, 1, 0, 0, 0, 1)), "MIN plusNanos(1)");
        check(LocalDateTime.MAX.minusNanos(1)
                .equals(LocalDateTime.of(999999999, 12, 31, 23, 59, 59, 999_999_998)),
                "MAX minusNanos(1)");

        // 年末跨年进位
        check(LocalDateTime.of(2024, 12, 31, 23, 0).plusHours(2)
                .equals(LocalDateTime.of(2025, 1, 1, 1, 0)), "year-end plusHours rollover");

        // epoch 与 toEpochSecond 一致性（经 UTC 偏移换算）
        check(LocalDateTime.of(1970, 1, 1, 0, 0).toEpochSecond(ZoneOffset.UTC) == 0,
                "toEpochSecond epoch");
        check(LocalDateTime.of(1970, 1, 1, 0, 0).toInstant(ZoneOffset.UTC).equals(Instant.EPOCH),
                "toInstant epoch");

        // compareTo 反对称
        LocalDateTime da = LocalDateTime.of(2024, 3, 1, 12, 0);
        LocalDateTime db = LocalDateTime.of(2024, 3, 1, 13, 0);
        check(da.compareTo(db) == -db.compareTo(da), "compareTo anti-symmetry");

        // 非法月 0 等价类
        expectThrows(DateTimeException.class, () -> LocalDateTime.of(2024, 0, 1, 0, 0),
                "of month 0");

        testSerialization();
        testNullSemantics();
        testTemporalAccessors();
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

    static void testSerialization() throws Exception {
        check(ObjectStreamClass.lookup(LocalDateTime.class).getSerialVersionUID() == 6207766400415563566L,
                "serialVersionUID");
        LocalDateTime[] vals = { LocalDateTime.MIN, LocalDateTime.MAX,
                LocalDateTime.of(2024, 2, 29, 13, 45, 20, 123456789) };
        for (LocalDateTime v : vals) {
            check(roundTrip(v).equals(v), "serialization round-trip " + v);
        }
    }

    static void testNullSemantics() {
        expectThrows(NullPointerException.class, () -> LocalDateTime.of((LocalDate) null, LocalTime.NOON),
                "of LocalDate null");
        expectThrows(NullPointerException.class,
                () -> LocalDateTime.of(LocalDate.of(2024, 2, 29), (LocalTime) null),
                "of LocalTime null");
        expectThrows(NullPointerException.class, () -> LocalDateTime.parse(null), "parse null");
        expectThrows(NullPointerException.class, () -> LocalDateTime.from(null), "from null");
        expectThrows(NullPointerException.class, () -> LocalDateTime.now((Clock) null), "now null");
        LocalDateTime d = LocalDateTime.of(2024, 1, 1, 0, 0);
        expectThrows(NullPointerException.class, () -> d.plus(null), "plus null");
        expectThrows(NullPointerException.class, () -> d.plus(1, null), "plus unit null");
        expectThrows(NullPointerException.class, () -> d.minus(null), "minus null");
        expectThrows(NullPointerException.class, () -> d.with((java.time.temporal.TemporalField) null, 1),
                "with field null");
        expectThrows(NullPointerException.class, () -> d.until(null, ChronoUnit.DAYS), "until null");
        expectThrows(NullPointerException.class, () -> d.atOffset(null), "atOffset null");
        expectThrows(NullPointerException.class, () -> d.atZone(null), "atZone null");
        expectThrows(NullPointerException.class, () -> d.format(null), "format null");
    }

    static void testTemporalAccessors() {
        LocalDateTime d = LocalDateTime.of(2024, 2, 29, 13, 45, 20);
        check(d.isSupported(ChronoField.DAY_OF_MONTH) && d.isSupported(ChronoField.HOUR_OF_DAY)
                && d.isSupported(ChronoUnit.DAYS), "isSupported");
        check(!d.isSupported(ChronoField.OFFSET_SECONDS), "isSupported OFFSET false");
        check(d.get(ChronoField.HOUR_OF_DAY) == 13, "get HOUR_OF_DAY");
        check(d.getLong(ChronoField.EPOCH_DAY) == LocalDate.of(2024, 2, 29).toEpochDay(),
                "getLong EPOCH_DAY");
        check(d.getLong(ChronoField.NANO_OF_DAY) == 49520000000000L, "getLong NANO_OF_DAY");
        ValueRange r = d.range(ChronoField.DAY_OF_MONTH);
        check(r.getMaximum() == 29, "range DAY_OF_MONTH");
        check(d.query(TemporalQueries.localDate()).equals(LocalDate.of(2024, 2, 29)), "query localDate");
        check(d.query(TemporalQueries.localTime()).equals(LocalTime.of(13, 45, 20)), "query localTime");
        check(d.query(TemporalQueries.precision()) == ChronoUnit.NANOS, "query precision");
        expectThrows(UnsupportedTemporalTypeException.class, () -> d.get(ChronoField.OFFSET_SECONDS),
                "get OFFSET_SECONDS");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepLocalDateTime();
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


    static void testDeepLocalDateTime() {
        LocalDateTime ldt = LocalDateTime.of(2024, 2, 29, 10, 15, 30, 123);
        xeq(ldt.toString(), "2024-02-29T10:15:30.000000123", "toString nano");
        xeq(LocalDateTime.of(2024, 2, 29, 10, 15).toString(), "2024-02-29T10:15", "toString hm");
        xeq(LocalDateTime.of(LocalDate.of(2024, 2, 29), LocalTime.of(10, 15)).toString(), "2024-02-29T10:15", "of(date,time)");
        xact(() -> LocalDateTime.of(2024, 2, 30, 10, 15), DateTimeException.class,
            "Invalid date 'FEBRUARY 30'", "invalid date");
        xact(() -> LocalDateTime.of(2024, 2, 29, 24, 0), DateTimeException.class,
            "Invalid value for HourOfDay (valid values 0 - 23): 24", "hour 24");
        xeq(ldt.toLocalDate().toString(), "2024-02-29", "toLocalDate");
        xeq(ldt.toLocalTime().toString(), "10:15:30.000000123", "toLocalTime");
        xeq(ldt.toInstant(ZoneOffset.ofHours(1)).toString(), "2024-02-29T09:15:30.000000123Z", "toInstant");
        check(ldt.toEpochSecond(ZoneOffset.ofHours(1)) == 1709198130L, "toEpochSecond");
        xeq(ldt.atZone(ZoneOffset.ofHours(2)).toString(), "2024-02-29T10:15:30.000000123+02:00", "atZone");
        check(LocalDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneOffset.ofHours(1)).toString().equals("1970-01-01T01:00"), "ofInstant");
        xeq(ldt.truncatedTo(ChronoUnit.HOURS).toString(), "2024-02-29T10:00", "trunc HOURS");
        xeq(ldt.truncatedTo(ChronoUnit.DAYS).toString(), "2024-02-29T00:00", "trunc DAYS");
        xeq(ldt.truncatedTo(ChronoUnit.MINUTES).toString(), "2024-02-29T10:15", "trunc MINUTES");
        xact(() -> ldt.truncatedTo(ChronoUnit.FOREVER), UnsupportedTemporalTypeException.class,
            "Unit is too large to be used for truncation", "trunc FOREVER");
        xeq(ldt.withNano(5).toString(), "2024-02-29T10:15:30.000000005", "withNano");
        xeq(ldt.withDayOfYear(60).toString(), "2024-02-29T10:15:30.000000123", "withDayOfYear 60");
        xact(() -> ldt.withDayOfYear(367), DateTimeException.class,
            "Invalid value for DayOfYear (valid values 1 - 365/366): 367", "withDayOfYear 367");
        xeq(ldt.withYear(2023).toString(), "2023-02-28T10:15:30.000000123", "withYear clamp");
        xeq(ldt.plusWeeks(1).toString(), "2024-03-07T10:15:30.000000123", "plusWeeks");
        xeq(LocalDateTime.of(2024, 1, 31, 0, 0).plusMonths(1).toString(), "2024-02-29T00:00", "plusMonths clamp");
        check(ldt.isSupported(ChronoField.NANO_OF_SECOND), "support nano");
        check(ldt.isSupported(ChronoUnit.HOURS), "support hours");
        check(!ldt.isSupported(ChronoField.OFFSET_SECONDS), "no offset field");
        xact(() -> ldt.getLong(ChronoField.OFFSET_SECONDS), UnsupportedTemporalTypeException.class,
            "Unsupported field: OffsetSeconds", "getLong offset");
        xact(() -> ldt.getLong(ChronoField.INSTANT_SECONDS), UnsupportedTemporalTypeException.class,
            "Unsupported field: InstantSeconds", "getLong instant");
        check(LocalDateTime.parse("2024-02-29T10:15:30").equals(LocalDateTime.of(2024, 2, 29, 10, 15, 30)), "parse equals of");
        String[] rts = {"2024-02-29T10:15:30", "2024-02-29T10:15", "1970-01-01T00:00:00.000000001"};
        for (String s : rts) check(LocalDateTime.parse(s).toString().equals(s), "rt " + s);
        xeq(ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), "2024-02-29T10:15:30.000000123", "format ISO");
        check(ldt.plus(Duration.ofHours(25)).minus(Duration.ofHours(25)).equals(ldt), "dur sym");
        check(ldt.plus(Period.ofMonths(1)).minus(Period.ofMonths(1)).equals(ldt), "period sym");
        check(ldt.plusDays(1).minusDays(1).equals(ldt), "days sym");
        check(LocalDateTime.of(2024, 1, 1, 0, 0).compareTo(ldt) < 0, "compareTo");
        check(ldt.isAfter(LocalDateTime.of(2024, 2, 29, 10, 15, 30, 122)), "isAfter nano");
        check(LocalDateTime.of(2024, 2, 29, 10, 15, 30, 123).hashCode()
                == LocalDateTime.of(LocalDate.of(2024, 2, 29), LocalTime.of(10, 15, 30, 123)).hashCode(), "hash path eq");
    }

}
