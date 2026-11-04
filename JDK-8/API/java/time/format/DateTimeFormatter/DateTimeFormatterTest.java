/*
 * @test
 * @summary java.time.format.DateTimeFormatter API conformance: full contract
 *          coverage. ISO/pattern/localized factories, fixed Locale formatting,
 *          withLocale/withZone/withResolverStyle/withChronology/withDecimalStyle/
 *          withResolverFields, getters, format/formatTo, parse (CharSequence /
 *          ParsePosition / TemporalQuery / parseBest / parseUnresolved),
 *          toFormat, parsedExcessDays/parsedLeapSecond, toString, and exact
 *          exception matrix (DateTimeParseException / DateTimeException / NPE).
 * @run main DateTimeFormatterTest
 */

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.DecimalStyle;
import java.time.format.FormatStyle;
import java.time.format.ResolverStyle;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.text.ParsePosition;
import java.util.Locale;

public class DateTimeFormatterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void testIsoConstants() {
        check(DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.of(2024, 3, 1)).equals("2024-03-01"),
                "ISO_LOCAL_DATE");
        check(DateTimeFormatter.ISO_LOCAL_TIME.format(LocalTime.of(13, 45)).equals("13:45:00"),
                "ISO_LOCAL_TIME");
        check(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.of(2024, 3, 1, 13, 45))
                .equals("2024-03-01T13:45:00"), "ISO_LOCAL_DATE_TIME");
        check(DateTimeFormatter.ISO_ZONED_DATE_TIME
                .format(ZonedDateTime.of(2024, 3, 1, 13, 45, 0, 0, ZoneOffset.ofHours(1)))
                .equals("2024-03-01T13:45:00+01:00"), "ISO_ZONED_DATE_TIME");
        check(DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(0))
                .equals("1970-01-01T00:00:00Z"), "ISO_INSTANT");
        check(DateTimeFormatter.BASIC_ISO_DATE.format(LocalDate.of(2024, 3, 1)).equals("20240301"),
                "BASIC_ISO_DATE");
        check(DateTimeFormatter.ISO_ORDINAL_DATE.format(LocalDate.of(2024, 3, 1)).equals("2024-061"),
                "ISO_ORDINAL_DATE");
        check(DateTimeFormatter.ISO_WEEK_DATE.format(LocalDate.of(2024, 3, 1)).equals("2024-W09-5"),
                "ISO_WEEK_DATE");
        check(DateTimeFormatter.RFC_1123_DATE_TIME != null, "RFC_1123_DATE_TIME");
        check(DateTimeFormatter.ISO_DATE != null && DateTimeFormatter.ISO_TIME != null
                && DateTimeFormatter.ISO_OFFSET_DATE != null && DateTimeFormatter.ISO_OFFSET_TIME != null
                && DateTimeFormatter.ISO_OFFSET_DATE_TIME != null && DateTimeFormatter.ISO_DATE_TIME != null,
                "remaining ISO constants");
    }

    static void testOfPattern() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US);
        check(f.format(LocalDate.of(2024, 1, 5)).equals("05 Jan 2024"), "ofPattern with locale");
        check(f.getLocale().equals(Locale.US), "getLocale");
        check(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.of(2024, 3, 1))
                .equals("2024-03-01"), "ofPattern");
        // round-trip
        LocalDate d = LocalDate.of(2024, 3, 1);
        check(DateTimeFormatter.ofPattern("yyyy-MM-dd").parse("2024-03-01", LocalDate::from).equals(d),
                "ofPattern parse round-trip");
        check(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC)
                .format(Instant.ofEpochSecond(0)).equals("1970-01-01 00:00:00"), "withZone format Instant");
    }

    static void testWithMethods() {
        DateTimeFormatter base = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        check(base.withLocale(Locale.US).getLocale().equals(Locale.US), "withLocale");
        check(base.withZone(ZoneOffset.UTC).getZone().equals(ZoneOffset.UTC), "withZone");
        check(base.withResolverStyle(ResolverStyle.STRICT).getResolverStyle() == ResolverStyle.STRICT,
                "withResolverStyle");
        check(base.getResolverStyle() == ResolverStyle.SMART, "default resolver SMART");
        check(base.withChronology(java.time.chrono.IsoChronology.INSTANCE).getChronology()
                == java.time.chrono.IsoChronology.INSTANCE, "withChronology");
        check(base.withDecimalStyle(DecimalStyle.STANDARD).getDecimalStyle() == DecimalStyle.STANDARD,
                "withDecimalStyle");
        check(base.withResolverFields(java.time.temporal.ChronoField.YEAR,
                java.time.temporal.ChronoField.MONTH_OF_YEAR).getResolverFields() != null,
                "withResolverFields varargs");
        check(base.toString().contains("YearOfEra") && base.toString().contains("MonthOfYear"), "toString");
    }

    static void testFormatAndFormatTo() {
        DateTimeFormatter f = DateTimeFormatter.ISO_LOCAL_DATE;
        check(f.format(LocalDate.of(2024, 3, 1)).equals("2024-03-01"), "format");
        StringBuilder sb = new StringBuilder();
        f.formatTo(LocalDate.of(2024, 3, 1), sb);
        check(sb.toString().equals("2024-03-01"), "formatTo");
        checkThrows(DateTimeParseException.class, () -> f.parse("bad"), "parse bad");
        checkThrows(DateTimeParseException.class, () -> f.parse("2024/03/01"), "parse wrong format");
        checkThrows(DateTimeParseException.class,
                () -> DateTimeFormatter.ofPattern("yyyy").parse("abc", LocalDate::from), "parse query bad");
    }

    static void testParse() {
        DateTimeFormatter f = DateTimeFormatter.ISO_LOCAL_DATE;
        check(f.parse("2024-03-01", LocalDate::from).equals(LocalDate.of(2024, 3, 1)), "parse query");
        TemporalAccessor ta = f.parse("2024-03-01");
        check(ta.query(LocalDate::from).equals(LocalDate.of(2024, 3, 1)), "parse accessor query");
        check(f.parse("2024-03-01", TemporalQueries.localDate()).equals(LocalDate.of(2024, 3, 1)),
                "parse TemporalQuery");
        // ParsePosition
        ParsePosition pos = new ParsePosition(0);
        f.parse("2024-03-01junk", pos);
        check(pos.getIndex() == 10, "parse ParsePosition index");
        check(pos.getErrorIndex() == -1, "parse ParsePosition no error");
        checkThrows(DateTimeParseException.class, () -> f.parse("bad", new ParsePosition(0)),
                "parse ParsePosition bad input");
        // parseBest
        TemporalAccessor best = f.parseBest("2024-03-01", LocalDate::from, LocalDateTime::from);
        check(LocalDate.from(best).equals(LocalDate.of(2024, 3, 1)), "parseBest");
        // parseUnresolved
        check(f.parseUnresolved("2024-03-01", new ParsePosition(0)) != null, "parseUnresolved");
    }

    static void testToFormat() {
        DateTimeFormatter f = DateTimeFormatter.ISO_LOCAL_DATE;
        java.text.Format jf = f.toFormat();
        check(jf.format(LocalDate.of(2024, 3, 1)).equals("2024-03-01"), "toFormat format");
        java.text.Format jfq = f.toFormat(LocalDate::from);
        check(jfq.format(LocalDate.of(2024, 3, 1)).equals("2024-03-01"), "toFormat query format");
    }

    static void testLocalized() {
        check(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.US)
                .format(LocalDate.of(2024, 3, 1)).contains("2024"), "ofLocalizedDate");
        check(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) != null, "ofLocalizedTime");
        check(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM) != null, "ofLocalizedDateTime");
        check(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM) != null,
                "ofLocalizedDateTime 2 styles");
    }

    static void testParsedQueries() {
        check(DateTimeFormatter.parsedExcessDays() != null, "parsedExcessDays");
        check(DateTimeFormatter.parsedLeapSecond() != null, "parsedLeapSecond");
    }

    static void testErrors() {
        // format with a type lacking the field
        checkThrows(DateTimeException.class, () -> DateTimeFormatter.ISO_LOCAL_DATE.format(Instant.EPOCH),
                "format wrong type");
        checkThrows(DateTimeParseException.class, () -> DateTimeFormatter.ISO_LOCAL_DATE.parse("2024-02-30"),
                "parse invalid date");
        checkThrows(NullPointerException.class, () -> DateTimeFormatter.ofPattern(null), "ofPattern null");
        checkThrows(NullPointerException.class, () -> DateTimeFormatter.ofPattern("yyyy", null),
                "ofPattern null locale");
        checkThrows(NullPointerException.class, () -> DateTimeFormatter.ISO_LOCAL_DATE.format(null),
                "format null");
        checkThrows(NullPointerException.class, () -> DateTimeFormatter.ISO_LOCAL_DATE.parse(null),
                "parse null");
        checkThrows(NullPointerException.class, () -> DateTimeFormatter.ISO_LOCAL_DATE.withLocale(null),
                "withLocale null");
        check(DateTimeFormatter.ISO_LOCAL_DATE.withZone(null) != null, "withZone null returns formatter");
    }

    public static void main(String[] args) {
        testIsoConstants();
        testOfPattern();
        testWithMethods();
        testFormatAndFormatTo();
        testParse();
        testToFormat();
        testLocalized();
        testParsedQueries();
        testErrors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
