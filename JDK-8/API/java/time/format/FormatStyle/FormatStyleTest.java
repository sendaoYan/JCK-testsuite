/*
 * @test
 * @summary java.time.format.FormatStyle API conformance: constants and valueOf.
 * @run main FormatStyleTest
 */

import java.time.format.FormatStyle;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.format.SignStyle;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;

public class FormatStyleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFormatStyle() {
        check(FormatStyle.valueOf("FULL") == FormatStyle.FULL, "valueOf FULL");
        check(FormatStyle.valueOf("MEDIUM") == FormatStyle.MEDIUM, "valueOf MEDIUM");
        check(FormatStyle.values().length == 4, "four styles");
    }

    public static void main(String[] args) {
        testFormatStyle();
        testDeepFormatEnums();
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


    static void testDeepFormatEnums() {
        check(FormatStyle.values().length == 4 && FormatStyle.valueOf("FULL") == FormatStyle.FULL
            && FormatStyle.valueOf("LONG") == FormatStyle.LONG && FormatStyle.valueOf("MEDIUM") == FormatStyle.MEDIUM
            && FormatStyle.valueOf("SHORT") == FormatStyle.SHORT, "FormatStyle values");
        xeq(java.util.Arrays.toString(FormatStyle.values()), "[FULL, LONG, MEDIUM, SHORT]", "FormatStyle order");
        xeq(java.util.Arrays.toString(ResolverStyle.values()), "[STRICT, SMART, LENIENT]", "ResolverStyle order");
        check(ResolverStyle.STRICT.ordinal() == 0 && ResolverStyle.SMART.ordinal() == 1 && ResolverStyle.LENIENT.ordinal() == 2, "ResolverStyle ordinals");
        check(ResolverStyle.valueOf("SMART") == ResolverStyle.SMART, "ResolverStyle valueOf");
        xeq(java.util.Arrays.toString(SignStyle.values()), "[NORMAL, ALWAYS, NEVER, NOT_NEGATIVE, EXCEEDS_PAD]", "SignStyle order");
        check(SignStyle.valueOf("EXCEEDS_PAD") == SignStyle.EXCEEDS_PAD, "SignStyle valueOf");
        xeq(java.util.Arrays.toString(TextStyle.values()), "[FULL, FULL_STANDALONE, SHORT, SHORT_STANDALONE, NARROW, NARROW_STANDALONE]", "TextStyle order");
        check(TextStyle.valueOf("NARROW_STANDALONE") == TextStyle.NARROW_STANDALONE, "TextStyle valueOf");
        check(TextStyle.FULL.asStandalone() == TextStyle.FULL_STANDALONE, "asStandalone FULL");
        check(TextStyle.SHORT.asNormal() == TextStyle.SHORT, "asNormal SHORT");
        check(TextStyle.NARROW_STANDALONE.asNormal() == TextStyle.NARROW, "asNormal NARROW_STANDALONE");
        check(TextStyle.SHORT_STANDALONE.asStandalone() == TextStyle.SHORT_STANDALONE, "asStandalone self");
        xact(() -> FormatStyle.valueOf("BOGUS"), IllegalArgumentException.class,
            "No enum constant java.time.format.FormatStyle.BOGUS", "FormatStyle valueOf bogus");
        xact(() -> ResolverStyle.valueOf("BOGUS"), IllegalArgumentException.class,
            "No enum constant java.time.format.ResolverStyle.BOGUS", "ResolverStyle valueOf bogus");
        xact(() -> SignStyle.valueOf("BOGUS"), IllegalArgumentException.class,
            "No enum constant java.time.format.SignStyle.BOGUS", "SignStyle valueOf bogus");
        xact(() -> TextStyle.valueOf("BOGUS"), IllegalArgumentException.class,
            "No enum constant java.time.format.TextStyle.BOGUS", "TextStyle valueOf bogus");
        LocalDate d = LocalDate.of(2024, 2, 29);
        java.util.Locale en = java.util.Locale.ENGLISH;
        xeq(Month.FEBRUARY.getDisplayName(TextStyle.FULL, en), "February", "month FULL via TextStyle");
        xeq(Month.FEBRUARY.getDisplayName(TextStyle.SHORT, en), "Feb", "month SHORT");
        xeq(DayOfWeek.THURSDAY.getDisplayName(TextStyle.NARROW, en), "T", "dow NARROW");
        xeq(ZoneId.of("Europe/Berlin").getDisplayName(TextStyle.SHORT, en), "CET", "zone SHORT");
        DateTimeFormatter sAlways = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4, 6, SignStyle.ALWAYS).toFormatter();
        xeq(d.format(sAlways), "+2024", "SignStyle.ALWAYS behavior");
        DateTimeFormatter sNormal = new DateTimeFormatterBuilder().appendValue(ChronoField.YEAR, 4, 6, SignStyle.NORMAL).toFormatter();
        xeq(d.format(sNormal), "2024", "SignStyle.NORMAL no sign for positive");
    }

}
