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
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
