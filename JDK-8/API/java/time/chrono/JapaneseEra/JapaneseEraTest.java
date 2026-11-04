/*
 * @test
 * @summary java.time.chrono.JapaneseEra conformance: era values and valueOf.
 * @run main JapaneseEraTest
 */

import java.time.chrono.JapaneseEra;
import java.time.chrono.JapaneseDate;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.UnsupportedTemporalTypeException;

public class JapaneseEraTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testJapaneseEra() {
        check(JapaneseEra.MEIJI.getValue() == -1, "MEIJI value");
        check(JapaneseEra.TAISHO.getValue() == 0, "TAISHO value");
        check(JapaneseEra.SHOWA.getValue() == 1, "SHOWA value");
        check(JapaneseEra.HEISEI.getValue() == 2, "HEISEI value");
        check(JapaneseEra.of(1) == JapaneseEra.SHOWA, "of(1) SHOWA");
    }

    public static void main(String[] args) {
        testJapaneseEra();
        testDeepJapaneseEra();
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


    static void testDeepJapaneseEra() {
        xeq(java.util.Arrays.toString(JapaneseEra.values()), "[Meiji, Taisho, Showa, Heisei, Reiwa]", "values order");
        check(JapaneseEra.MEIJI.getValue() == -1, "meiji value");
        check(JapaneseEra.TAISHO.getValue() == 0, "taisho value");
        check(JapaneseEra.SHOWA.getValue() == 1, "showa value");
        check(JapaneseEra.HEISEI.getValue() == 2, "heisei value");
        check(JapaneseEra.of(1) == JapaneseEra.SHOWA, "of(1)");
        check(JapaneseEra.of(3).toString().equals("Reiwa"), "of(3) Reiwa");
        xeq(JapaneseEra.valueOf("Reiwa").toString(), "Reiwa", "valueOf Reiwa");
        xeq(JapaneseEra.valueOf("Heisei").toString(), "Heisei", "valueOf Heisei");
        xeq(JapaneseEra.MEIJI.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH),
            "Meiji", "meiji full");
        xeq(JapaneseEra.SHOWA.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH),
             "Showa", "showa short");
        xeq(JapaneseEra.HEISEI.getDisplayName(java.time.format.TextStyle.NARROW, java.util.Locale.ENGLISH),
            "H", "heisei narrow");
        xeq(JapaneseEra.valueOf("Reiwa").getDisplayName(java.time.format.TextStyle.NARROW, java.util.Locale.ENGLISH),
            "R", "reiwa narrow");
        xact(() -> JapaneseEra.valueOf("REIWA"), IllegalArgumentException.class,
            "japaneseEra is invalid", "valueOf UPPERCASE rejected");
        xact(() -> JapaneseEra.valueOf("heisei"), IllegalArgumentException.class,
            "japaneseEra is invalid", "valueOf lowercase rejected");
        xact(() -> JapaneseEra.valueOf(null), NullPointerException.class, "japaneseEra", "valueOf null");
        xact(() -> JapaneseEra.SHOWA.range(java.time.temporal.ChronoField.YEAR_OF_ERA),
            java.time.temporal.UnsupportedTemporalTypeException.class, "Unsupported field: YearOfEra", "range YearOfEra");
        xeq(JapaneseEra.SHOWA.query(java.time.temporal.TemporalQueries.precision()).toString(), "Eras", "precision");
        check(JapaneseDate.of(2024, 6, 15).getEra() == JapaneseEra.valueOf("Reiwa"), "date era identity");
    }

}
