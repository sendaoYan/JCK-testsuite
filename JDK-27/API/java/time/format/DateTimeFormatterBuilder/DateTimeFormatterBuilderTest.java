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
import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.time.format.SignStyle;
import java.time.temporal.TemporalQueries;
import java.time.temporal.UnsupportedTemporalTypeException;

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
        testDeepBuilder();
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


    static void testDeepBuilder() {
        LocalDate d = LocalDate.of(2024, 2, 29);
        LocalTime t = LocalTime.of(10, 15, 30);
        java.util.Locale en = java.util.Locale.ENGLISH;
        DateTimeFormatter f1 = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 2).toFormatter();
        check(f1.parse("89").getLong(ChronoField.YEAR) == 89L, "appendValue(2) parses raw 89");
        xact(() -> d.format(f1), DateTimeException.class,
            "Field Year cannot be printed as the value 2024 exceeds the maximum print width of 2", "appendValue 2 width overflow");
        DateTimeFormatter f2 = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD).toFormatter();
        xeq(d.format(f2), "2024", "appendValue range EXCEEDS_PAD 2024");
        DateTimeFormatter f3 = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4, 10, SignStyle.ALWAYS).toFormatter();
        xeq(d.format(f3), "+2024", "SignStyle.ALWAYS prints plus");
        DateTimeFormatter f4 = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR_OF_ERA, 4, 10, SignStyle.NEVER).toFormatter();
        xeq(LocalDate.of(-500, 1, 1).format(f4), "0501", "SignStyle.NEVER strips sign (era year)");
        DateTimeFormatter f5 = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 5, 10, SignStyle.EXCEEDS_PAD).toFormatter();
        check(f5.parse("12345").getLong(ChronoField.YEAR) == 12345L, "EXCEEDS_PAD accepts unsigned at pad width");
        xact(() -> f5.parse("+12345"), DateTimeParseException.class,
            "Text '+12345' could not be parsed at index 0", "EXCEEDS_PAD parse rejects sign at pad width");
        DateTimeFormatter fr = new DateTimeFormatterBuilder().appendFraction(ChronoField.NANO_OF_SECOND, 3, 3, true).toFormatter();
        xeq(LocalTime.of(10, 15, 30, 500000000).format(fr), ".500", "appendFraction decimal point");
        DateTimeFormatter frN = new DateTimeFormatterBuilder().appendFraction(ChronoField.NANO_OF_SECOND, 3, 9, false).toFormatter();
        xeq(LocalTime.of(10, 15, 30, 500000000).format(frN), "500", "appendFraction no decimal min 3 digits");
        xeq(t.format(frN), "000", "appendFraction zero nanos");
        xeq(LocalTime.of(10, 15, 30, 123456789).format(frN), "123456789", "appendFraction full nanos");
        check(fr.parse(".500").getLong(ChronoField.NANO_OF_SECOND) == 500000000L, "appendFraction parse .500");
        xact(() -> fr.parse(".5"), DateTimeParseException.class,
            "Text '.5' could not be parsed at index 1", "appendFraction parse min 3 digits");
        xact(() -> frN.parse("30.5"), DateTimeParseException.class,
            "Text '30.5' could not be parsed at index 0", "fraction no-decimal parse JDK8 vs 27 error index");
        DateTimeFormatter opt = new DateTimeFormatterBuilder().appendLiteral("2024").optionalStart()
            .appendPattern("-MM").optionalEnd().toFormatter();
        check(opt.parse("2024").query(TemporalQueries.localDate()) == null, "optional absent leaves nothing");
        xact(() -> opt.parse("2024").getLong(ChronoField.MONTH_OF_YEAR), UnsupportedTemporalTypeException.class,
            "Unsupported field: MonthOfYear", "optional absent getLong throws");
        check(opt.parse("2024-03").getLong(ChronoField.MONTH_OF_YEAR) == 3L, "optional present parses");
        DateTimeFormatter pad = new DateTimeFormatterBuilder().padNext(3).appendValue(ChronoField.DAY_OF_MONTH).toFormatter();
        xeq(d.format(pad), " 29", "padNext pads with space");
        DateTimeFormatter pad0 = new DateTimeFormatterBuilder().padNext(3, '0').appendValue(ChronoField.DAY_OF_MONTH).toFormatter();
        xeq(d.format(pad0), "029", "padNext with zero");
        DateTimeFormatter mapT = new DateTimeFormatterBuilder()
            .appendText(ChronoField.MONTH_OF_YEAR, java.util.Collections.singletonMap(2L, "FEB")).toFormatter();
        xeq(d.format(mapT), "FEB", "appendText map format");
        check(mapT.parse("FEB").getLong(ChronoField.MONTH_OF_YEAR) == 2L, "appendText map parse");
        xact(() -> mapT.parse("MAR"), DateTimeParseException.class,
            "Text 'MAR' could not be parsed at index 0", "appendText map unknown");
        ZoneOffset o1 = ZoneOffset.ofHours(1);
        xeq(d.atTime(LocalTime.NOON).atOffset(o1).format(new DateTimeFormatterBuilder().appendPattern("X").toFormatter()), "+01", "pattern X");
        xeq(d.atTime(LocalTime.NOON).atOffset(o1).format(new DateTimeFormatterBuilder().appendPattern("x").toFormatter()), "+01", "pattern x");
        xeq(d.atTime(LocalTime.NOON).atOffset(o1).format(new DateTimeFormatterBuilder().appendPattern("Z").toFormatter()), "+0100", "pattern Z");
        xeq(d.atTime(LocalTime.NOON).atOffset(o1).format(new DateTimeFormatterBuilder().appendPattern("ZZZZZ").toFormatter()), "+01:00", "pattern ZZZZZ");
        xeq(d.atTime(LocalTime.NOON).atOffset(o1).format(new DateTimeFormatterBuilder().appendPattern("O").toFormatter()), "GMT+1", "pattern O");
        xeq(d.atTime(LocalTime.NOON).atOffset(o1).format(new DateTimeFormatterBuilder().appendPattern("OOOO").toFormatter()), "GMT+01:00", "pattern OOOO");
        xeq(d.atTime(LocalTime.NOON).atOffset(ZoneOffset.UTC).format(new DateTimeFormatterBuilder().appendPattern("X").toFormatter()), "Z", "pattern X on UTC");
        xeq(d.atTime(LocalTime.NOON).atOffset(ZoneOffset.UTC).format(new DateTimeFormatterBuilder().appendPattern("Z").toFormatter()), "+0000", "pattern Z on UTC");
        xeq(d.atTime(LocalTime.NOON).atOffset(ZoneOffset.UTC).format(new DateTimeFormatterBuilder().appendPattern("O").toFormatter()), "GMT", "pattern O on UTC");
        xact(() -> new DateTimeFormatterBuilder().appendOffset("GMT", "GMT"), IllegalArgumentException.class,
            "Invalid zone offset pattern: GMT", "appendOffset GMT invalid");
        DateTimeFormatter ci = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("yyyy-MMM-dd").toFormatter(en);
        xeq(LocalDate.parse("2024-feb-29", ci).toString(), "2024-02-29", "case-insensitive month");
        DateTimeFormatter cs = new DateTimeFormatterBuilder().parseCaseSensitive().appendPattern("yyyy-MMM-dd").toFormatter(en);
        xact(() -> LocalDate.parse("2024-feb-29", cs), DateTimeParseException.class, null, "case-sensitive rejects");
        DateTimeFormatter def = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd")
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 12).toFormatter();
        check(def.parse("2024-02-15").getLong(ChronoField.HOUR_OF_DAY) == 12L, "parseDefaulting");
        DateTimeFormatter two = new DateTimeFormatterBuilder().appendPattern("yyyy").appendLiteral('/').appendValue(ChronoField.MONTH_OF_YEAR, 2).toFormatter();
        xeq(d.format(two), "2024/02", "mixed literal and value");
        xeq(DateTimeFormatterBuilder.getLocalizedDateTimePattern(FormatStyle.MEDIUM, FormatStyle.MEDIUM, IsoChronology.INSTANCE, en),
            "MMM d, y, h:mm:ss\u202Fa", "localized pattern JDK27 y + U+202F");
        xeq(DateTimeFormatterBuilder.getLocalizedDateTimePattern(FormatStyle.SHORT, null, IsoChronology.INSTANCE, en), "M/d/yy", "localized pattern short date only");
        xeq(DateTimeFormatterBuilder.getLocalizedDateTimePattern(null, FormatStyle.MEDIUM, IsoChronology.INSTANCE, en), "h:mm:ss\u202Fa", "localized time pattern JDK27 U+202F");
        DateTimeFormatter offs = new DateTimeFormatterBuilder().appendLocalizedOffset(TextStyle.SHORT).toFormatter();
        xeq(d.atTime(LocalTime.NOON).atOffset(o1).format(offs), "GMT+1", "appendLocalizedOffset short");
        xact(() -> new DateTimeFormatterBuilder().appendPattern("zzz").toFormatter().format(d), DateTimeException.class,
            "Unable to extract ZoneId from temporal 2024-02-29", "zzz needs zone JDK27 message differs");
        DateTimeFormatter yr = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4).toFormatter();
        xeq(d.format(yr), "2024", "appendValue 4");
        DateTimeFormatter dup = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR).appendValue(ChronoField.YEAR).toFormatter();
        xeq(d.format(dup), "20242024", "repeated field prints twice");
    }

}
