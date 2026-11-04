/*
 * @test
 * @summary java.time.format.TextStyle conformance: white-box structure + black-box behavior.
 * @run main TextStyleTest
 */

import java.lang.reflect.Modifier;
import java.time.DayOfWeek;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.time.ZoneId;
import java.time.chrono.IsoChronology;

public class TextStyleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.format.TextStyle");
        check(c.isEnum(), "is enum");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface(), "not interface");
        check(Enum.class.isAssignableFrom(c), "extends Enum");
        check(c.getEnumConstants().length == 6, "six constants");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(TextStyle.values().length == 6, "six styles");
        check(TextStyle.valueOf("FULL") == TextStyle.FULL, "valueOf FULL");
        check(TextStyle.valueOf("FULL_STANDALONE") == TextStyle.FULL_STANDALONE,
              "valueOf FULL_STANDALONE");
        check(TextStyle.valueOf("SHORT") == TextStyle.SHORT, "valueOf SHORT");
        check(TextStyle.valueOf("NARROW") == TextStyle.NARROW, "valueOf NARROW");
        check(TextStyle.valueOf("NARROW_STANDALONE") == TextStyle.NARROW_STANDALONE,
              "valueOf NARROW_STANDALONE");

        check(!TextStyle.FULL.isStandalone(), "FULL not standalone");
        check(TextStyle.FULL_STANDALONE.isStandalone(), "FULL_STANDALONE standalone");
        check(!TextStyle.SHORT.isStandalone(), "SHORT not standalone");
        check(TextStyle.SHORT_STANDALONE.isStandalone(), "SHORT_STANDALONE standalone");
        check(!TextStyle.NARROW.isStandalone(), "NARROW not standalone");
        check(TextStyle.NARROW_STANDALONE.isStandalone(), "NARROW_STANDALONE standalone");

        check(TextStyle.FULL.asStandalone() == TextStyle.FULL_STANDALONE,
              "FULL asStandalone");
        check(TextStyle.FULL_STANDALONE.asNormal() == TextStyle.FULL, "FULL_STANDALONE asNormal");
        check(TextStyle.NARROW.asStandalone() == TextStyle.NARROW_STANDALONE,
              "NARROW asStandalone");

        check("Jan".equals(Month.JANUARY.getDisplayName(TextStyle.SHORT, Locale.US)),
              "January short");
        check("January".equals(Month.JANUARY.getDisplayName(TextStyle.FULL, Locale.US)),
              "January full");
        check("J".equals(Month.JANUARY.getDisplayName(TextStyle.NARROW, Locale.US)),
              "January narrow");
        check("Monday".equals(DayOfWeek.MONDAY.getDisplayName(TextStyle.FULL, Locale.US)),
              "Monday full");
        check("Mon".equals(DayOfWeek.MONDAY.getDisplayName(TextStyle.SHORT, Locale.US)),
              "Monday short");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepTextStyle();
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


    static void testDeepTextStyle() {
        xeq(java.util.Arrays.toString(TextStyle.values()), "[FULL, FULL_STANDALONE, SHORT, SHORT_STANDALONE, NARROW, NARROW_STANDALONE]", "6 constants");
        check(TextStyle.FULL.asStandalone() == TextStyle.FULL_STANDALONE, "FULL asStandalone");
        check(TextStyle.SHORT.asStandalone() == TextStyle.SHORT_STANDALONE, "SHORT asStandalone");
        check(TextStyle.NARROW.asStandalone() == TextStyle.NARROW_STANDALONE, "NARROW asStandalone");
        check(TextStyle.FULL_STANDALONE.asNormal() == TextStyle.FULL, "FULL_STANDALONE asNormal");
        check(TextStyle.SHORT_STANDALONE.asNormal() == TextStyle.SHORT, "SHORT_STANDALONE asNormal");
        check(TextStyle.NARROW_STANDALONE.asNormal() == TextStyle.NARROW, "NARROW_STANDALONE asNormal");
        check(TextStyle.FULL.asNormal() == TextStyle.FULL, "asNormal normal self");
        check(TextStyle.FULL_STANDALONE.asStandalone() == TextStyle.FULL_STANDALONE, "asStandalone standalone self");
        java.util.Locale en = java.util.Locale.ENGLISH;
        xeq(Month.FEBRUARY.getDisplayName(TextStyle.FULL, en), "February", "FULL");
        xeq(Month.FEBRUARY.getDisplayName(TextStyle.SHORT, en), "Feb", "SHORT");
        xeq(Month.FEBRUARY.getDisplayName(TextStyle.NARROW, en), "F", "NARROW");
        xeq(DayOfWeek.THURSDAY.getDisplayName(TextStyle.FULL, en), "Thursday", "dow FULL");
        xeq(DayOfWeek.THURSDAY.getDisplayName(TextStyle.SHORT, en), "Thu", "dow SHORT");
        xeq(DayOfWeek.THURSDAY.getDisplayName(TextStyle.NARROW, en), "T", "dow NARROW");
        xeq(ZoneId.of("Europe/Berlin").getDisplayName(TextStyle.FULL, en), "Central European Time", "zone FULL");
        xeq(ZoneId.of("Europe/Berlin").getDisplayName(TextStyle.SHORT, en), "CET", "zone SHORT");
        xeq(ZoneId.of("Europe/Berlin").getDisplayName(TextStyle.NARROW, en), "Europe/Berlin", "zone NARROW id");
        xeq(IsoChronology.INSTANCE.getDisplayName(TextStyle.FULL, en), "ISO", "chrono FULL iso");
        java.util.Locale fr = java.util.Locale.FRENCH;
        xeq(Month.FEBRUARY.getDisplayName(TextStyle.FULL, fr), "février", "FR FULL");
        xeq(DayOfWeek.THURSDAY.getDisplayName(TextStyle.FULL, fr), "jeudi", "FR dow FULL");
        xeq(Month.FEBRUARY.getDisplayName(TextStyle.NARROW, fr), "F", "FR NARROW F");
        xeq(Month.FEBRUARY.getDisplayName(TextStyle.SHORT_STANDALONE, en), "Feb", "SHORT_STANDALONE JDK27");
        xeq(Month.FEBRUARY.getDisplayName(TextStyle.FULL_STANDALONE, en), "February", "FULL_STANDALONE JDK27");
        check(TextStyle.valueOf("NARROW") == TextStyle.NARROW, "valueOf");
        xact(() -> TextStyle.valueOf("BOGUS"), IllegalArgumentException.class,
            "No enum constant java.time.format.TextStyle.BOGUS", "valueOf bogus");
    }

}
