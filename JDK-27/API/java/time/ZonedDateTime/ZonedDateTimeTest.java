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
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.DateTimeException;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.Duration;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalQueries;

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

    static ZonedDateTime roundTrip(ZonedDateTime v) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(v);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (ZonedDateTime) ois.readObject();
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

    static void testBlackBox() throws Exception {
        testFactories();
        testZoneConversions();
        testArithmeticImmutability();
        testComparison();
        testErrors();

        // ---- 边界值/等价类/等价性（本棒补测） ----
        ZonedDateTime odd = ZonedDateTime.of(2024, 3, 1, 12, 30, 45, 123_456_789,
                ZoneOffset.ofHoursMinutes(5, 30));
        check(ZonedDateTime.parse(odd.toString()).equals(odd), "round-trip +05:30");

        // epoch 与时间线
        ZonedDateTime epoch = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        check(epoch.toInstant().equals(Instant.EPOCH), "epoch toInstant");
        check(epoch.plusDays(1).toLocalDate().equals(LocalDate.of(1970, 1, 2)), "epoch plusDays");

        // 年末跨年进位
        ZonedDateTime yearEnd = ZonedDateTime.of(2024, 12, 31, 23, 0, 0, 0, ZoneOffset.UTC);
        check(yearEnd.plusHours(2).toLocalDateTime().equals(LocalDateTime.of(2025, 1, 1, 1, 0)),
                "year-end plusHours rollover");

        // withZoneSameInstant 往返保持本地时间
        check(odd.withZoneSameInstant(ZoneOffset.UTC).withZoneSameInstant(odd.getZone())
                .toLocalDateTime().equals(odd.toLocalDateTime()), "withZoneSameInstant round-trip");

        // toOffsetDateTime / toLocalDateTime 拆分
        check(odd.toOffsetDateTime().toInstant().equals(odd.toInstant()),
                "toOffsetDateTime keeps instant");

        // compareTo 反对称
        ZonedDateTime za = ZonedDateTime.of(2024, 3, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime zb = ZonedDateTime.of(2024, 3, 1, 13, 0, 0, 0, ZoneOffset.UTC);
        check(za.compareTo(zb) == -zb.compareTo(za), "compareTo anti-symmetry");
        check(za.isBefore(zb) && !zb.isBefore(za), "isBefore anti-symmetry");

        // 非法日 0 等价类
        expectThrows(DateTimeException.class,
                () -> ZonedDateTime.of(2024, 1, 0, 0, 0, 0, 0, ZoneOffset.UTC), "of day 0");

        testSerialization();
        testNullSemantics();
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

    static void testSerialization() throws Exception {
        check(ObjectStreamClass.lookup(ZonedDateTime.class).getSerialVersionUID() == -6260982410461394882L,
                "serialVersionUID");
        ZonedDateTime[] vals = { ZonedDateTime.of(2024, 2, 29, 13, 45, 20, 123456789, ZoneOffset.UTC),
                ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")),
                ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.ofHours(5)) };
        for (ZonedDateTime v : vals) {
            check(roundTrip(v).equals(v), "serialization round-trip " + v);
        }
    }

    static void testNullSemantics() {
        expectThrows(NullPointerException.class, () -> ZonedDateTime.of((LocalDateTime) null, ZoneOffset.UTC),
                "of LDT null");
        expectThrows(NullPointerException.class,
                () -> ZonedDateTime.of(LocalDateTime.of(2024, 1, 1, 0, 0), (ZoneId) null), "of zone null");
        expectThrows(NullPointerException.class, () -> ZonedDateTime.parse(null), "parse null");
        expectThrows(NullPointerException.class, () -> ZonedDateTime.from(null), "from null");
        expectThrows(NullPointerException.class, () -> ZonedDateTime.now((Clock) null), "now null");
        ZonedDateTime z = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        expectThrows(NullPointerException.class, () -> z.withZoneSameInstant(null), "withZoneSameInstant null");
        expectThrows(NullPointerException.class, () -> z.withZoneSameLocal(null), "withZoneSameLocal null");
        expectThrows(NullPointerException.class, () -> z.plus(null), "plus null");
        expectThrows(NullPointerException.class, () -> z.minus(null), "minus null");
        expectThrows(NullPointerException.class, () -> z.until(null, ChronoUnit.DAYS), "until null");
        expectThrows(NullPointerException.class, () -> z.format(null), "format null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepZonedDateTime();
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


    static void testDeepZonedDateTime() {
        ZoneId ber = ZoneId.of("Europe/Berlin");
        ZonedDateTime z = ZonedDateTime.of(2024, 2, 29, 10, 15, 30, 0, ber);
        xeq(z.toString(), "2024-02-29T10:15:30+01:00[Europe/Berlin]", "toString");
        check(z.getZone() == ber && z.getOffset().equals(ZoneOffset.ofHours(1)), "zone/offset");
        check(z.getDayOfYear() == 60, "dayOfYear leap");
        xeq(z.toLocalDate().toString(), "2024-02-29", "toLocalDate");
        xeq(z.toLocalTime().toString(), "10:15:30", "toLocalTime");
        xeq(z.toLocalDateTime().toString(), "2024-02-29T10:15:30", "toLocalDateTime");
        xeq(z.toInstant().toString(), "2024-02-29T09:15:30Z", "toInstant");
        check(z.toEpochSecond() == 1709198130L, "toEpochSecond");
        xeq(z.toOffsetDateTime().toString(), "2024-02-29T10:15:30+01:00", "toOffsetDateTime");
        xeq(ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.ofHours(1)).toString(), "1970-01-01T01:00+01:00", "ofInstant");
        xeq(ZonedDateTime.ofLocal(LocalDateTime.of(2024, 2, 29, 10, 15, 30), ber, null).toString(),
            "2024-02-29T10:15:30+01:00[Europe/Berlin]", "ofLocal");
        xeq(ZonedDateTime.ofStrict(LocalDateTime.of(2024, 2, 29, 10, 15, 30), ZoneOffset.ofHours(1), ber).toString(),
            "2024-02-29T10:15:30+01:00[Europe/Berlin]", "ofStrict normal");
        xact(() -> ZonedDateTime.ofStrict(LocalDateTime.of(2024, 3, 31, 2, 30), ZoneOffset.ofHours(1), ber),
            DateTimeException.class,
            "LocalDateTime '2024-03-31T02:30' does not exist in zone 'Europe/Berlin' due to a gap in the local time-line, typically caused by daylight savings",
            "ofStrict gap");
        xact(() -> ZonedDateTime.ofStrict(LocalDateTime.of(2024, 2, 29, 10, 15, 30), ZoneOffset.ofHours(5), ber),
            DateTimeException.class,
            "ZoneOffset '+05:00' is not valid for LocalDateTime '2024-02-29T10:15:30' in zone 'Europe/Berlin'",
            "ofStrict wrong offset");
        xact(() -> ZonedDateTime.ofStrict(LocalDateTime.of(2024, 10, 27, 2, 30), ZoneOffset.ofHours(6), ber),
            DateTimeException.class,
            "ZoneOffset '+06:00' is not valid for LocalDateTime '2024-10-27T02:30' in zone 'Europe/Berlin'",
            "ofStrict overlap wrong offset");
        // tzdata Berlin 2024: gap 2024-03-31 02:00+01:00 -> 03:00+02:00; overlap 2024-10-27 03:00+02:00 -> 02:00+01:00
        ZonedDateTime gap = ZonedDateTime.of(LocalDateTime.of(2024, 3, 31, 2, 30), ber);
        xeq(gap.toString(), "2024-03-31T03:30+02:00[Europe/Berlin]", "gap shifts forward +1h");
        check(gap.getOffset().equals(ZoneOffset.ofHours(2)), "gap offset");
        ZonedDateTime ovl = ZonedDateTime.of(LocalDateTime.of(2024, 10, 27, 2, 30), ber);
        xeq(ovl.toString(), "2024-10-27T02:30+02:00[Europe/Berlin]", "overlap picks earlier offset");
        xeq(ovl.withLaterOffsetAtOverlap().toString(), "2024-10-27T02:30+01:00[Europe/Berlin]", "withLaterOffsetAtOverlap");
        xeq(ovl.withEarlierOffsetAtOverlap().toString(), "2024-10-27T02:30+02:00[Europe/Berlin]", "withEarlierOffsetAtOverlap");
        xeq(z.withZoneSameInstant(ZoneOffset.UTC).toString(), "2024-02-29T09:15:30Z", "withZoneSameInstant UTC");
        xeq(z.withZoneSameLocal(ZoneId.of("America/New_York")).toString(),
            "2024-02-29T10:15:30-05:00[America/New_York]", "withZoneSameLocal NY");
        xeq(z.withZoneSameLocal(ber).withZoneSameInstant(ZoneId.of("America/New_York")).getOffset().toString(),
            "-05:00", "instant-correct NY offset");
        xeq(z.withDayOfMonth(1).toString(), "2024-02-01T10:15:30+01:00[Europe/Berlin]", "withDayOfMonth");
        xeq(z.withDayOfYear(60).toString(), "2024-02-29T10:15:30+01:00[Europe/Berlin]", "withDayOfYear 60");
        xeq(z.withMonth(1).toString(), "2024-01-29T10:15:30+01:00[Europe/Berlin]", "withMonth");
        xeq(z.withYear(2023).toString(), "2023-02-28T10:15:30+01:00[Europe/Berlin]", "withYear clamps Feb29");
        check(z.getLong(ChronoField.OFFSET_SECONDS) == 3600L, "getLong offsetSeconds");
        check(z.format(DateTimeFormatter.ISO_ZONED_DATE_TIME).equals("2024-02-29T10:15:30+01:00[Europe/Berlin]"), "ISO_ZONED");
        check(ZonedDateTime.parse("2024-02-29T10:15:30+01:00[Europe/Berlin]").equals(z), "parse rt");
        check(ZonedDateTime.parse(z.toString()).equals(z), "toString rt");
        check(z.query(TemporalQueries.zoneId()) == ber, "query zoneId");
        check(z.query(TemporalQueries.offset()).equals(ZoneOffset.ofHours(1)), "query offset");
        check(z.query(TemporalQueries.localDate()).toString().equals("2024-02-29"), "query localDate");
        check(z.isSupported(ChronoField.INSTANT_SECONDS), "support INSTANT_SECONDS");
        check(z.isSupported(ChronoField.OFFSET_SECONDS), "support OFFSET_SECONDS");
        check(z.plusDays(1).minusDays(1).equals(z), "days sym");
        check(z.plusMonths(1).minusMonths(1).equals(z), "months sym");
        check(z.plus(Duration.ofHours(3)).minus(Duration.ofHours(3)).equals(z), "dur sym");
        check(z.plus(Period.ofWeeks(1)).minus(Period.ofWeeks(1)).equals(z), "period sym");
        check(z.plus(1, ChronoUnit.HOURS).equals(ZonedDateTime.of(2024, 2, 29, 11, 15, 30, 0, ber)), "plus hours");
        xact(() -> ZonedDateTime.of(null, ber), NullPointerException.class, null, "of null ldt");
        xact(() -> ZonedDateTime.of(LocalDateTime.of(2024, 2, 29, 10, 15, 30), null), NullPointerException.class, "zone", "of null zone");
        check(z.truncatedTo(ChronoUnit.DAYS).toString().equals("2024-02-29T00:00+01:00[Europe/Berlin]"), "trunc DAYS");
        check(z.truncatedTo(ChronoUnit.HOURS).toString().equals("2024-02-29T10:00+01:00[Europe/Berlin]"), "trunc HOURS");
        check(z.compareTo(ZonedDateTime.of(2024, 2, 29, 10, 15, 31, 0, ber)) < 0, "compareTo");
        check(z.isBefore(ZonedDateTime.of(2024, 3, 1, 0, 0, 0, 0, ber)), "isBefore");
        check(z.isEqual(ZonedDateTime.of(2024, 2, 29, 4, 15, 30, 0, ZoneId.of("America/New_York"))), "isEqual across zones");
        check(!z.equals(ZonedDateTime.of(2024, 2, 29, 4, 15, 30, 0, ZoneId.of("America/New_York"))), "equals differs from isEqual");
        check(z.hashCode() == ZonedDateTime.parse(z.toString()).hashCode(), "hash rt");
        xeq(z.minusYears(24).toString(), "2000-02-29T10:15:30+01:00[Europe/Berlin]", "minusYears keeps Feb29 across 2 leaps");
    }

}
