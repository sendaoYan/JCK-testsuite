/*
 * @test
 * @summary java.time.format.TextStyle API conformance: standalone flag and names.
 * @run main TextStyleTest
 */

import java.time.format.TextStyle;
import java.time.DayOfWeek;
import java.time.Month;
import java.time.ZoneId;
import java.time.chrono.IsoChronology;

public class TextStyleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTextStyle() {
        check(!TextStyle.FULL.isStandalone(), "FULL not standalone");
        check(TextStyle.FULL_STANDALONE.isStandalone(), "FULL_STANDALONE standalone");
        check(TextStyle.SHORT_STANDALONE.isStandalone(), "SHORT_STANDALONE standalone");
        check("NARROW".equals(TextStyle.NARROW.name()), "NARROW name");
    }

    public static void main(String[] args) {
        testTextStyle();
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
        xeq(Month.FEBRUARY.getDisplayName(TextStyle.FULL, fr), "f\u00E9vrier", "FR FULL");
        xeq(DayOfWeek.THURSDAY.getDisplayName(TextStyle.FULL, fr), "jeudi", "FR dow FULL");
        xeq(Month.FEBRUARY.getDisplayName(TextStyle.NARROW, fr), "F", "FR NARROW F");
         xeq(Month.FEBRUARY.getDisplayName(TextStyle.SHORT_STANDALONE, en), "2", "SHORT_STANDALONE JDK8 numeric");
         xeq(Month.FEBRUARY.getDisplayName(TextStyle.FULL_STANDALONE, en), "2", "FULL_STANDALONE JDK8 numeric");
        check(TextStyle.valueOf("NARROW") == TextStyle.NARROW, "valueOf");
        xact(() -> TextStyle.valueOf("BOGUS"), IllegalArgumentException.class,
            "No enum constant java.time.format.TextStyle.BOGUS", "valueOf bogus");
    }

}
