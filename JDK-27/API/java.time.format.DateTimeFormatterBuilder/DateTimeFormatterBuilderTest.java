/*
 * @test
 * @summary java.time.format.DateTimeFormatterBuilder conformance: white-box structure + black-box behavior.
 * @run main DateTimeFormatterBuilderTest
 */

import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

public class DateTimeFormatterBuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.format.DateTimeFormatterBuilder");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface(), "not interface");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!c.isEnum(), "not enum");
        check(c.getSuperclass() == Object.class, "superclass Object");
        check(c.getConstructor() != null, "public no-arg constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        LocalDate date = LocalDate.of(2024, 6, 1);

        // Compose a yyyy-MM-dd formatter field by field and round-trip it.
        DateTimeFormatter composed =
            new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR)
                .appendLiteral('-')
                .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                .appendLiteral('-')
                .appendValue(ChronoField.DAY_OF_MONTH, 2)
                .toFormatter();
        check("2024-06-01".equals(date.format(composed)), "composed format");
        check(composed.parse("2024-06-01", LocalDate::from).equals(date), "composed parse");

        // appendText with explicit text style and locale.
        DateTimeFormatter dow =
            new DateTimeFormatterBuilder()
                .appendText(ChronoField.DAY_OF_WEEK, TextStyle.FULL)
                .toFormatter(Locale.US);
        check("Saturday".equals(date.format(dow)), "appendText day of week");

        // appendPattern mirrors DateTimeFormatter.ofPattern.
        DateTimeFormatter patterned =
            new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd").toFormatter();
        check("2024-06-01".equals(date.format(patterned)), "appendPattern format");
        check(patterned.parse("2024-06-01", LocalDate::from).equals(date), "appendPattern parse");

        // parseDefaulting fills in a missing field.
        TemporalAccessor defaulted =
            new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd")
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .toFormatter()
                .parse("2024-06-01");
        check(defaulted.get(ChronoField.HOUR_OF_DAY) == 0, "parseDefaulting hour");

        // padNext pads the following value to the given width.
        DateTimeFormatter padded =
            new DateTimeFormatterBuilder()
                .appendValue(ChronoField.MONTH_OF_YEAR, 1)
                .padNext(4)
                .appendValue(ChronoField.DAY_OF_MONTH, 1)
                .toFormatter();
        check("6   1".equals(date.format(padded)), "padNext width");

        // optionalStart/optionalEnd makes a trailing section optional.
        DateTimeFormatter optional =
            new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR)
                .appendLiteral('-')
                .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                .optionalStart()
                .appendLiteral('-')
                .appendValue(ChronoField.DAY_OF_MONTH, 2)
                .optionalEnd()
                .toFormatter();
        check(optional.parse("2024-06", YearMonth::from).equals(YearMonth.of(2024, 6)),
              "optional parse year-month");
        check(optional.parse("2024-06-01", LocalDate::from).equals(date),
              "optional parse full date");

        // parseCaseInsensitive accepts lower-case month text.
        DateTimeFormatter ci =
            new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("MMM")
                .toFormatter(Locale.US);
        check(ci.parse("jun", Month::from) == Month.JUNE, "parseCaseInsensitive");

        // appendOffsetId prints the offset in the canonical form.
        DateTimeFormatter offset =
            new DateTimeFormatterBuilder().appendOffsetId().toFormatter();
        OffsetDateTime odt = OffsetDateTime.of(2024, 6, 1, 0, 0, 0, 0, ZoneOffset.ofHours(2));
        check("+02:00".equals(offset.format(odt)), "appendOffsetId");

        // appendInstant prints an ISO-8601 instant.
        DateTimeFormatter instantF =
            new DateTimeFormatterBuilder().appendInstant().toFormatter();
        check("1970-01-01T00:00:00Z".equals(instantF.format(Instant.EPOCH)),
              "appendInstant epoch");

        // Fluent chaining returns the same builder.
        DateTimeFormatterBuilder b = new DateTimeFormatterBuilder();
        check(b.appendValue(ChronoField.YEAR) == b, "fluent appendValue returns this");
        check(Year.of(2024).format(b.toFormatter()).equals("2024"), "fluent result usable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
