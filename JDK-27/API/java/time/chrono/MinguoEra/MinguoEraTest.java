/*
 * @test
 * @summary java.time.chrono.MinguoEra conformance: white-box structure + black-box behavior.
 * @run main MinguoEraTest
 */

import java.lang.reflect.Modifier;
import java.time.chrono.Era;
import java.time.chrono.MinguoEra;
import java.time.chrono.MinguoChronology;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.UnsupportedTemporalTypeException;

public class MinguoEraTest {
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
        Class<?> c = Class.forName("java.time.chrono.MinguoEra");
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
        check(MinguoEra.BEFORE_ROC.getValue() == 0, "BEFORE_ROC value");
        check(MinguoEra.ROC.getValue() == 1, "ROC value");
        check(MinguoEra.valueOf("ROC") == MinguoEra.ROC, "valueOf ROC");
        check(MinguoEra.valueOf("BEFORE_ROC") == MinguoEra.BEFORE_ROC, "valueOf BEFORE_ROC");
        check(MinguoEra.values().length == 2, "two values");
        check(MinguoEra.of(1) == MinguoEra.ROC, "of(1) ROC");
        check(MinguoEra.of(0) == MinguoEra.BEFORE_ROC, "of(0) BEFORE_ROC");

        expectThrows(IllegalArgumentException.class, () -> MinguoEra.valueOf("NOPE"), "valueOf invalid");
        expectThrows(java.time.DateTimeException.class, () -> MinguoEra.of(2), "of(2) invalid");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepMinguoEra();
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


    static void testDeepMinguoEra() {
        check(MinguoEra.BEFORE_ROC.getValue() == 0, "BEFORE_ROC value");
        check(MinguoEra.ROC.getValue() == 1, "ROC value");
        xeq(MinguoEra.BEFORE_ROC.toString(), "BEFORE_ROC", "BEFORE_ROC toString");
        xeq(MinguoEra.ROC.toString(), "ROC", "ROC toString");
        xeq(java.util.Arrays.toString(MinguoEra.values()), "[BEFORE_ROC, ROC]", "values order");
        check(MinguoEra.valueOf("ROC") == MinguoEra.ROC, "valueOf");
        xeq(MinguoEra.ROC.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH),
            "Minguo", "ROC full name");
        xeq(MinguoEra.ROC.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH),
            "Minguo", "ROC short name");
        xeq(MinguoEra.ROC.getDisplayName(java.time.format.TextStyle.NARROW, java.util.Locale.ENGLISH),
            "Minguo", "ROC narrow name");
        xeq(MinguoEra.BEFORE_ROC.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH),
            "B.R.O.C.", "BEFORE_ROC full name");
        xeq(MinguoEra.BEFORE_ROC.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH),
            "B.R.O.C.", "BEFORE_ROC short name");
        xeq(MinguoEra.BEFORE_ROC.getDisplayName(java.time.format.TextStyle.NARROW, java.util.Locale.ENGLISH),
            "B.R.O.C.", "BEFORE_ROC narrow name");
        xact(() -> MinguoEra.valueOf("BOGUS"), IllegalArgumentException.class,
            "No enum constant java.time.chrono.MinguoEra.BOGUS", "valueOf bogus");
        xact(() -> MinguoEra.ROC.range(java.time.temporal.ChronoField.YEAR_OF_ERA),
            java.time.temporal.UnsupportedTemporalTypeException.class, "Unsupported field: YearOfEra", "range YearOfEra");
        check(MinguoChronology.INSTANCE.eraOf(0) == MinguoEra.BEFORE_ROC, "chronology eraOf identity");
    }

}
