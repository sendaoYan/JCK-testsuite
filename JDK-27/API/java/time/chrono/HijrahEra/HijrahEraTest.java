/*
 * @test
 * @summary java.time.chrono.HijrahEra conformance: white-box structure + black-box behavior.
 * @run main HijrahEraTest
 */

import java.lang.reflect.Modifier;
import java.time.chrono.Era;
import java.time.chrono.HijrahEra;
import java.time.chrono.HijrahChronology;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.UnsupportedTemporalTypeException;

public class HijrahEraTest {
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
        Class<?> c = Class.forName("java.time.chrono.HijrahEra");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == Enum.class, "extends Enum");
        check(Era.class.isAssignableFrom(c), "implements Era");
        checkMethod(c, "values");
        checkMethod(c, "valueOf", String.class);
        checkMethod(c, "of", int.class);
        checkMethod(c, "getValue");
    }

    static void testBlackBox() {
        check(HijrahEra.AH.getValue() == 1, "AH value");
        check(HijrahEra.valueOf("AH") == HijrahEra.AH, "valueOf AH");
        check(HijrahEra.values().length == 1, "one value");
        check(HijrahEra.of(1) == HijrahEra.AH, "of(1) AH");

        expectThrows(IllegalArgumentException.class, () -> HijrahEra.valueOf("NOPE"), "valueOf invalid");
        expectThrows(java.time.DateTimeException.class, () -> HijrahEra.of(2), "of(2) invalid");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepHijrahEra();
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


    static void testDeepHijrahEra() {
        xeq(HijrahEra.AH.toString(), "AH", "toString");
        xeq(HijrahEra.valueOf("AH").toString(), "AH", "valueOf");
        check(HijrahEra.values().length == 1 && HijrahEra.values()[0] == HijrahEra.AH, "single era");
        check(HijrahEra.AH.getValue() == 1, "value 1");
        xeq(HijrahEra.AH.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH),
            "Anno Hegirae", "full name");
        xeq(HijrahEra.AH.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH),
            "AH", "short name");
        xeq(HijrahEra.AH.getDisplayName(java.time.format.TextStyle.NARROW, java.util.Locale.ENGLISH),
            "AH", "narrow name");
        xeq(HijrahChronology.INSTANCE.eraOf(1).toString(), "AH", "chronology eraOf");
        xact(() -> HijrahEra.valueOf("BOGUS"), IllegalArgumentException.class,
            "No enum constant java.time.chrono.HijrahEra.BOGUS", "valueOf bogus");
        xact(() -> HijrahEra.AH.range(java.time.temporal.ChronoField.YEAR_OF_ERA),
            java.time.temporal.UnsupportedTemporalTypeException.class, "Unsupported field: YearOfEra", "range YearOfEra");
    }

}
