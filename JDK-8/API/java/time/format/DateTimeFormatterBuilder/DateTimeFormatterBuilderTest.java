/*
 * @test
 * @summary java.time.format.DateTimeFormatterBuilder API conformance: compose a
 *          year-month formatter.
 * @run main DateTimeFormatterBuilderTest
 */

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.time.format.SignStyle;
import java.time.format.TextStyle;
import java.time.temporal.TemporalQueries;
import java.time.temporal.UnsupportedTemporalTypeException;

public class DateTimeFormatterBuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDateTimeFormatterBuilder() {
        DateTimeFormatter f =
            new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR)
                .appendLiteral('-')
                .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                .toFormatter();
        check("2024-06".equals(LocalDate.of(2024, 6, 1).format(f)), "builder format");
    }

    public static void main(String[] args) {
        testDateTimeFormatterBuilder();
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
             "Text '30.5' could not be parsed, unparsed text found at index 2", "fraction no-decimal parse JDK8 leftover");
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
             "MMM d, yyyy h:mm:ss a", "localized pattern JDK8");
        xeq(DateTimeFormatterBuilder.getLocalizedDateTimePattern(FormatStyle.SHORT, null, IsoChronology.INSTANCE, en), "M/d/yy", "localized pattern short date only");
         xeq(DateTimeFormatterBuilder.getLocalizedDateTimePattern(null, FormatStyle.MEDIUM, IsoChronology.INSTANCE, en), "h:mm:ss a", "localized time pattern JDK8");
        DateTimeFormatter offs = new DateTimeFormatterBuilder().appendLocalizedOffset(TextStyle.SHORT).toFormatter();
        xeq(d.atTime(LocalTime.NOON).atOffset(o1).format(offs), "GMT+1", "appendLocalizedOffset short");
         xact(() -> new DateTimeFormatterBuilder().appendPattern("zzz").toFormatter().format(d), DateTimeException.class,
             "Unable to extract value: class java.time.LocalDate", "zzz needs zone JDK8");
        DateTimeFormatter yr = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4).toFormatter();
        xeq(d.format(yr), "2024", "appendValue 4");
        DateTimeFormatter dup = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR).appendValue(ChronoField.YEAR).toFormatter();
        xeq(d.format(dup), "20242024", "repeated field prints twice");
    }

}
