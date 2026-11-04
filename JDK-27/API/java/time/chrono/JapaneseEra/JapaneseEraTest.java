/*
 * @test
 * @summary java.time.chrono.JapaneseEra conformance: white-box structure + black-box behavior.
 * @run main JapaneseEraTest
 */

import java.lang.reflect.Modifier;
import java.time.chrono.Era;
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

    static void checkMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); passed++; }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name, e); }
    }

    static void expectThrows(Class<? extends Throwable> t, Runnable r, String msg) {
        try { r.run(); } catch (Throwable e) {
            if (t.isInstance(e)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + t.getSimpleName() + " got " + e.getClass().getSimpleName(), e);
        }
        throw new AssertionError(msg + ": expected " + t.getSimpleName() + " but nothing thrown");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.chrono.JapaneseEra");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isEnum(), "not an enum (final class)");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(Era.class.isAssignableFrom(c), "implements Era");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        checkMethod(c, "of", int.class);
        checkMethod(c, "getValue");
        checkMethod(c, "values");
        checkMethod(c, "valueOf", String.class);
    }

    static void testBlackBox() {
        check(JapaneseEra.MEIJI.getValue() == -1, "MEIJI value");
        check(JapaneseEra.TAISHO.getValue() == 0, "TAISHO value");
        check(JapaneseEra.SHOWA.getValue() == 1, "SHOWA value");
        check(JapaneseEra.HEISEI.getValue() == 2, "HEISEI value");
        check(JapaneseEra.REIWA.getValue() == 3, "REIWA value");

        check(JapaneseEra.of(-1) == JapaneseEra.MEIJI, "of(-1) MEIJI");
        check(JapaneseEra.of(0) == JapaneseEra.TAISHO, "of(0) TAISHO");
        check(JapaneseEra.of(1) == JapaneseEra.SHOWA, "of(1) SHOWA");
        check(JapaneseEra.of(2) == JapaneseEra.HEISEI, "of(2) HEISEI");
        check(JapaneseEra.of(3) == JapaneseEra.REIWA, "of(3) REIWA");

        JapaneseEra[] values = JapaneseEra.values();
        check(values.length >= 4, "values() at least 4 eras");
        boolean hasHeisei = false;
        for (JapaneseEra e : values) if (e == JapaneseEra.HEISEI) hasHeisei = true;
        check(hasHeisei, "values() contains HEISEI");

        // of() rejects out-of-range values
        expectThrows(java.time.DateTimeException.class, () -> JapaneseEra.of(99), "of(99) invalid");
        expectThrows(java.time.DateTimeException.class, () -> JapaneseEra.of(-99), "of(-99) invalid");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
        check(JapaneseEra.REIWA.getValue() == 3 && JapaneseEra.REIWA.toString().equals("Reiwa"), "REIWA public constant");
        xeq(JapaneseEra.MEIJI.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH),
            "Meiji", "meiji full");
        xeq(JapaneseEra.SHOWA.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH),
            "Sh\u014dwa", "showa short");
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
