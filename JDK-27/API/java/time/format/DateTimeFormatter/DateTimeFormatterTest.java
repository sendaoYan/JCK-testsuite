/*
 * @test
 * @summary java.time.format.DateTimeFormatter conformance: white-box shape + black-box
 *          ofPattern/ISO formatters, format/parse round-trips, locale, and error contracts.
 * @run main DateTimeFormatterTest
 *
 * 设计案（黑盒）：
 * 1. 契约：DateTimeFormatter 是日期时间的格式化器，不可变、线程安全，支持 pattern 与 ISO 预定义。
 * 2. 场景：ofPattern/ISO_* 构造，format(TemporalAccessor) 输出字符串，parse(text, query) 反解，withLocale/withZone 定制。
 * 3. 组合链路：ofPattern -> format -> parse(LocalDate::from) 往返 -> ISO_INSTANT 格式化 Instant -> withLocale。
 * 4. 不变量：format/parse 往返一致；locale 默认 FORMAT；ISO_LOCAL_DATE 严格解析。
 * 5. 边界/异常：parse 非法文本/非法日期抛 DateTimeParseException；format 缺字段抛 UnsupportedTemporalTypeException。
 */

import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.text.ParsePosition;
import java.util.Locale;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.IsoChronology;
import java.time.format.DecimalStyle;
import java.time.format.FormatStyle;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

public class DateTimeFormatterTest {
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
        Class<?> c = Class.forName("java.time.format.DateTimeFormatter");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == Object.class, "superclass Object");
    }

    static void testBlackBox() {
        testOfPattern();
        testISOPredefined();
        testLocaleAndZone();
        testFormatTo();
        testErrors();
        testNullSemantics();
        testParsePosition();
    }

    static void testOfPattern() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate d = LocalDate.of(2024, 3, 1);
        check("2024-03-01".equals(d.format(f)), "format yyyy-MM-dd");
        check(f.parse("2024-03-01", LocalDate::from).equals(d), "parse with LocalDate::from");
        check(f.parse("2024-03-01").query(LocalDate::from).equals(d), "parse then query");
        DateTimeFormatter slash = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.US);
        check(slash.format(d).equals("01/03/2024"), "format dd/MM/yyyy");
        check(slash.parse("01/03/2024", LocalDate::from).equals(d), "parse dd/MM/yyyy");
    }

    static void testISOPredefined() {
        LocalDate d = LocalDate.of(2024, 3, 1);
        check(DateTimeFormatter.ISO_LOCAL_DATE.format(d).equals("2024-03-01"), "ISO_LOCAL_DATE format");
        check(DateTimeFormatter.BASIC_ISO_DATE.format(d).equals("20240301"), "BASIC_ISO_DATE format");
        check(DateTimeFormatter.ISO_LOCAL_DATE.parse("2024-03-01", LocalDate::from).equals(d),
                "ISO_LOCAL_DATE parse");
        Instant i = Instant.parse("2024-03-01T12:00:00Z");
        check(DateTimeFormatter.ISO_INSTANT.format(i).equals("2024-03-01T12:00:00Z"),
                "ISO_INSTANT format");
        OffsetDateTime odt = OffsetDateTime.of(2024, 3, 1, 10, 30, 0, 0, ZoneOffset.ofHours(2));
        check(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(odt).equals("2024-03-01T10:30:00+02:00"),
                "ISO_OFFSET_DATE_TIME format");
        check(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2024-03-01T10:30:00+02:00",
                OffsetDateTime::from).equals(odt), "ISO_OFFSET_DATE_TIME parse");
    }

    static void testLocaleAndZone() {
        check(DateTimeFormatter.ofPattern("yyyy-MM-dd").getLocale()
                .equals(Locale.getDefault(Locale.Category.FORMAT)), "default locale FORMAT");
        DateTimeFormatter us = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US);
        check(us.format(LocalDate.of(2024, 1, 15)).equals("January 15, 2024"),
                "month name Locale.US");
        check(us.getLocale().equals(Locale.US), "getLocale US");
        check(us.withLocale(Locale.GERMAN).getLocale().equals(Locale.GERMAN), "withLocale");
        DateTimeFormatter zoned = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneOffset.UTC);
        check(zoned.getZone().equals(ZoneOffset.UTC), "withZone/getZone");
    }

    static void testFormatTo() {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter.ISO_LOCAL_DATE.formatTo(LocalDate.of(2024, 3, 1), sb);
        check(sb.toString().equals("2024-03-01"), "formatTo Appendable");
    }

    static void testErrors() {
        expectThrows(DateTimeParseException.class, () -> DateTimeFormatter.ISO_LOCAL_DATE
                .parse("2024-02-30", LocalDate::from), "parse invalid date");
        expectThrows(DateTimeParseException.class, () -> DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .parse("garbage", LocalDate::from), "parse garbage");
        expectThrows(DateTimeParseException.class, () -> DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .parse("2024-13-01", LocalDate::from), "parse invalid month");
        expectThrows(UnsupportedTemporalTypeException.class,
                () -> DateTimeFormatter.ofPattern("HH:mm").format(LocalDate.of(2024, 3, 1)),
                "format LocalDate with time pattern");
    }

    static void testNullSemantics() {
        expectThrows(NullPointerException.class, () -> DateTimeFormatter.ofPattern(null), "ofPattern null");
        expectThrows(NullPointerException.class, () -> DateTimeFormatter.ofPattern("yyyy", null),
                "ofPattern null locale");
        expectThrows(NullPointerException.class, () -> DateTimeFormatter.ISO_LOCAL_DATE.format(null),
                "format null");
        expectThrows(NullPointerException.class, () -> DateTimeFormatter.ISO_LOCAL_DATE.parse(null),
                "parse null");
        expectThrows(NullPointerException.class, () -> DateTimeFormatter.ISO_LOCAL_DATE.withLocale(null),
                "withLocale null");
        check(DateTimeFormatter.ISO_LOCAL_DATE.withZone(null) != null, "withZone null returns formatter");
    }

    static void testParsePosition() {
        DateTimeFormatter f = DateTimeFormatter.ISO_LOCAL_DATE;
        ParsePosition pos = new ParsePosition(0);
        f.parse("2024-03-01junk", pos);
        check(pos.getIndex() == 10, "parse ParsePosition index");
        check(pos.getErrorIndex() == -1, "parse ParsePosition no error");
        expectThrows(DateTimeParseException.class, () -> f.parse("bad", new ParsePosition(0)),
                "parse ParsePosition bad input");
    }

    public static void main(String[] args) throws Exception {
        // Pattern text (era/ampm/month names) is localized; the JDK-8 twin pins
        // ENGLISH for the same reason.  Keep the two trees deterministic and
        // identical instead of depending on the host default locale.
        java.util.Locale.setDefault(java.util.Locale.ENGLISH);
        testWhiteBox();
        testBlackBox();
        testDeepFormatter();
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


    static void testDeepFormatter() {
        LocalDate d = LocalDate.of(2024, 2, 29);
        LocalTime t = LocalTime.of(10, 15, 30);
        LocalDateTime ldt = LocalDateTime.of(d, t);
        ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.of("Europe/Berlin"));
        Instant inst = Instant.ofEpochSecond(1709206530);
        // exact-string ISO family (probe-locked identical both sides)
        xeq(d.format(DateTimeFormatter.ISO_LOCAL_DATE), "2024-02-29", "ISO_LOCAL_DATE");
        xeq(d.format(DateTimeFormatter.BASIC_ISO_DATE), "20240229", "BASIC_ISO_DATE");
        xeq(d.format(DateTimeFormatter.ISO_WEEK_DATE), "2024-W09-4", "ISO_WEEK_DATE");
        xeq(d.format(DateTimeFormatter.ISO_ORDINAL_DATE), "2024-060", "ISO_ORDINAL_DATE");
        xeq(d.format(DateTimeFormatter.ISO_DATE), "2024-02-29", "ISO_DATE");
        xeq(t.format(DateTimeFormatter.ISO_LOCAL_TIME), "10:15:30", "ISO_LOCAL_TIME");
        xeq(t.format(DateTimeFormatter.ISO_TIME), "10:15:30", "ISO_TIME");
        xeq(ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), "2024-02-29T10:15:30", "ISO_LOCAL_DATE_TIME");
        xeq(ldt.atOffset(ZoneOffset.ofHours(1)).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), "2024-02-29T10:15:30+01:00", "ISO_OFFSET_DATE_TIME");
        xeq(zdt.format(DateTimeFormatter.ISO_ZONED_DATE_TIME), "2024-02-29T10:15:30+01:00[Europe/Berlin]", "ISO_ZONED_DATE_TIME");
        xeq(DateTimeFormatter.ISO_INSTANT.format(inst), "2024-02-29T11:35:30Z", "ISO_INSTANT formats Instant");
        xeq(DateTimeFormatter.ISO_INSTANT.format(Instant.EPOCH), "1970-01-01T00:00:00Z", "ISO_INSTANT epoch");
        xeq(zdt.format(DateTimeFormatter.RFC_1123_DATE_TIME), "Thu, 29 Feb 2024 10:15:30 +0100", "RFC_1123 with offset");
        xeq(Instant.ofEpochSecond(1709201730).atZone(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME),
            "Thu, 29 Feb 2024 10:15:30 GMT", "RFC_1123 GMT");
        // parse round-trips equals
        check(LocalDate.parse("2024-02-29", DateTimeFormatter.ISO_LOCAL_DATE).equals(d), "parse ISO_LOCAL_DATE");
        check(LocalDate.parse("20240229", DateTimeFormatter.BASIC_ISO_DATE).equals(d), "parse BASIC");
        check(LocalDate.parse("2024-W09-4", DateTimeFormatter.ISO_WEEK_DATE).equals(d), "parse WEEK");
        check(LocalDate.parse("2024-060", DateTimeFormatter.ISO_ORDINAL_DATE).equals(d), "parse ORDINAL");
        check(LocalTime.parse("10:15:30", DateTimeFormatter.ISO_LOCAL_TIME).equals(t), "parse TIME");
        check(LocalDateTime.parse("2024-02-29T10:15:30", DateTimeFormatter.ISO_LOCAL_DATE_TIME).equals(ldt), "parse LDT");
        check(ZonedDateTime.parse("2024-02-29T10:15:30+01:00[Europe/Berlin]", DateTimeFormatter.ISO_ZONED_DATE_TIME).equals(zdt), "parse ZDT");
        check(DateTimeFormatter.ISO_INSTANT.parse("2024-02-29T11:35:30Z", Instant::from).equals(inst), "parse INSTANT via fmt");
        // localized EN, per-side locked
        java.util.Locale en = java.util.Locale.ENGLISH;
        xeq(d.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(en)), "Thursday, February 29, 2024", "loc date FULL");
        xeq(d.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(en)), "February 29, 2024", "loc date LONG");
        xeq(d.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(en)), "Feb 29, 2024", "loc date MEDIUM");
        xeq(d.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(en)), "2/29/24", "loc date SHORT");
        xeq(t.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withLocale(en)), "10:15:30 AM", "loc time MEDIUM JDK27 U+202F");
        xeq(ldt.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(en)), "Feb 29, 2024, 10:15:30 AM", "loc dt MEDIUM JDK27 CLDR comma + U+202F");
        xeq(ldt.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT).withLocale(en)), "2/29/24, 10:15 AM", "loc dt SHORT SHORT JDK27 comma + U+202F");
        xeq(zdt.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.FULL).withLocale(en)), "Thursday, February 29, 2024, 10:15:30 AM Central European Standard Time", "loc dt FULL FULL JDK27 CLDR long zone name");
        // with* combinations
        DateTimeFormatter f = DateTimeFormatter.ISO_LOCAL_DATE;
        check(f.getLocale() != null && f.getResolverStyle() == ResolverStyle.STRICT
            && f.getChronology() == IsoChronology.INSTANCE, "ISO defaults");
        check(f.getDecimalStyle().equals(DecimalStyle.STANDARD), "decimal standard");
        check(f.getZone() == null, "ISO_LOCAL_DATE zone null");
        check(DateTimeFormatter.ISO_ZONED_DATE_TIME.getZone() == null, "ISO_ZONED zone null (needs temporal zone)");
        DateTimeFormatter fZ = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.of("Europe/Berlin"));
        check(fZ.getZone().equals(ZoneId.of("Europe/Berlin")), "withZone");
        xeq(Instant.ofEpochSecond(1709198130).atZone(ZoneId.of("Europe/Berlin")).toLocalDateTime().format(fZ), "2024-02-29T10:15:30", "instant overriden by zone");
        DateTimeFormatter smart = DateTimeFormatter.ISO_LOCAL_DATE.withResolverStyle(ResolverStyle.SMART);
        xeq(LocalDate.parse("2023-02-28", smart).toString(), "2023-02-28", "smart ok");
        DateTimeFormatter lenient = DateTimeFormatter.ISO_LOCAL_DATE.withResolverStyle(ResolverStyle.LENIENT);
        xeq(LocalDate.parse("2023-02-29", lenient).toString(), "2023-03-01", "lenient rolls over");
        xact(() -> LocalDate.parse("2023-02-29", DateTimeFormatter.ISO_LOCAL_DATE), DateTimeParseException.class,
            "Text '2023-02-29' could not be parsed: Invalid date 'February 29' as '2023' is not a leap year", "strict default resolver");
        xact(() -> LocalDate.parse("2024-11-31", DateTimeFormatter.ISO_LOCAL_DATE), DateTimeParseException.class,
            "Text '2024-11-31' could not be parsed: Invalid date 'NOVEMBER 31'", "strict Nov 31");
        xact(() -> LocalDate.parse("2024-13-01", DateTimeFormatter.ISO_LOCAL_DATE), DateTimeParseException.class,
            "Text '2024-13-01' could not be parsed: Invalid value for MonthOfYear (valid values 1 - 12): 13", "month 13 parse");
        xact(() -> LocalDate.parse("", DateTimeFormatter.ISO_LOCAL_DATE), DateTimeParseException.class, null, "empty parse");
        xact(() -> LocalDate.parse(null, DateTimeFormatter.ISO_LOCAL_DATE), NullPointerException.class, "text", "parse null text");
        xact(() -> LocalDate.parse("2024-02-29", (DateTimeFormatter) null), NullPointerException.class, "formatter", "parse null fmt");
        xact(() -> d.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), DateTimeException.class,
            "Unsupported field: HourOfDay", "format date with datetime fmt");
        xact(() -> t.format(DateTimeFormatter.ISO_LOCAL_DATE), DateTimeException.class,
            "Unsupported field: Year", "format time with date fmt");
        // parsed accessor
        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
        TemporalAccessor pa = tf.parse("10:15");
        check(pa.getLong(ChronoField.HOUR_OF_DAY) == 10L, "parsed accessor hour");
        check(pa.getLong(ChronoField.MINUTE_OF_HOUR) == 15L, "parsed accessor minute");
        xact(() -> pa.getLong(ChronoField.YEAR), UnsupportedTemporalTypeException.class, "Unsupported field: Year", "parsed accessor missing field");
        // ofPattern behaviors
        xeq(d.format(DateTimeFormatter.ofPattern("EEE d MMM yyyy", en)), "Thu 29 Feb 2024", "pattern EEE d MMM yyyy");
        xeq(ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), "2024-02-29 10:15:30", "pattern datetime");
        xeq(d.format(DateTimeFormatter.ofPattern("uuuu-MM-dd")), "2024-02-29", "pattern u");
        xeq(d.format(DateTimeFormatter.ofPattern("yyyy 'Q'Q")), "2024 Q1", "pattern literal quote + quarter");
        xeq(d.format(DateTimeFormatter.ofPattern("''yyyy''")), "'2024'", "pattern escaped quotes");
        // era text is locale-sensitive (zh_CN renders 公元), so pin the
        // formatter's locale instead of relying on the host default.
        xeq(d.format(DateTimeFormatter.ofPattern("G yyyy", en)), "AD 2024", "pattern G iso");
        xeq(d.format(DateTimeFormatter.ofPattern("D w W F")), "60 9 5 5", "pattern F JDK27 day-of-week-in-month=5");
        xeq(LocalDate.of(2024, 2, 1).format(DateTimeFormatter.ofPattern("F")), "1", "pattern F first of month both agree");
        xact(() -> DateTimeFormatter.ofPattern("WW"), IllegalArgumentException.class,
            "Too many pattern letters: W", "pattern W single letter only");
        xeq(ldt.format(DateTimeFormatter.ofPattern("a h K H k")), "AM 10 10 10 10", "pattern a h K H k");
        xact(() -> DateTimeFormatter.ofPattern("yyyy-ii"), IllegalArgumentException.class,
            "Unknown pattern letter: i", "unknown pattern letter");
        xact(() -> DateTimeFormatter.ofPattern(null), NullPointerException.class, "pattern", "ofPattern null");
        xact(() -> DateTimeFormatter.ofPattern("yyyy'abc"), IllegalArgumentException.class,
            "Pattern ends with an incomplete string literal: yyyy'abc", "unclosed quote");
        // formatTo / appendable path
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter.ISO_LOCAL_DATE.formatTo(d, sb);
        xeq(sb.toString(), "2024-02-29", "formatTo");
        DateTimeFormatter fr = DateTimeFormatter.ofPattern("EEEE", java.util.Locale.FRENCH);
        xeq(d.format(fr), "jeudi", "french EEEE");
        DateTimeFormatter frCh = fr.withLocale(java.util.Locale.FRENCH).withChronology(IsoChronology.INSTANCE);
        xeq(d.format(frCh), "jeudi", "withChronology ISO keeps text");
        check(DateTimeFormatter.ofPattern("yyyy").toString().length() > 0, "toString nonempty");
        xeq(ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS")), "2024-02-29T10:15:30.000000000", "9-digit fraction pads");
    }

}
