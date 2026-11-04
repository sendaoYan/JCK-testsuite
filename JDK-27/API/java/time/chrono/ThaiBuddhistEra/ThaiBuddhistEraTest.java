/*
 * @test
 * @summary java.time.chrono.ThaiBuddhistEra conformance: white-box structure + black-box behavior.
 * @run main ThaiBuddhistEraTest
 */

import java.lang.reflect.Modifier;
import java.time.chrono.Era;
import java.time.chrono.ThaiBuddhistEra;
import java.time.chrono.ThaiBuddhistChronology;
import java.time.chrono.ThaiBuddhistDate;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.UnsupportedTemporalTypeException;

public class ThaiBuddhistEraTest {
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
        Class<?> c = Class.forName("java.time.chrono.ThaiBuddhistEra");
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
        check(ThaiBuddhistEra.BEFORE_BE.getValue() == 0, "BEFORE_BE value");
        check(ThaiBuddhistEra.BE.getValue() == 1, "BE value");
        check(ThaiBuddhistEra.valueOf("BE") == ThaiBuddhistEra.BE, "valueOf BE");
        check(ThaiBuddhistEra.valueOf("BEFORE_BE") == ThaiBuddhistEra.BEFORE_BE, "valueOf BEFORE_BE");
        check(ThaiBuddhistEra.values().length == 2, "two values");
        check(ThaiBuddhistEra.of(1) == ThaiBuddhistEra.BE, "of(1) BE");
        check(ThaiBuddhistEra.of(0) == ThaiBuddhistEra.BEFORE_BE, "of(0) BEFORE_BE");

        expectThrows(IllegalArgumentException.class, () -> ThaiBuddhistEra.valueOf("NOPE"), "valueOf invalid");
        expectThrows(java.time.DateTimeException.class, () -> ThaiBuddhistEra.of(2), "of(2) invalid");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepThaiBuddhistEra();
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


    static void testDeepThaiBuddhistEra() {
        check(ThaiBuddhistEra.BEFORE_BE.getValue() == 0, "BEFORE_BE value");
        check(ThaiBuddhistEra.BE.getValue() == 1, "BE value");
        xeq(ThaiBuddhistEra.BEFORE_BE.toString(), "BEFORE_BE", "BEFORE_BE toString");
        xeq(ThaiBuddhistEra.BE.toString(), "BE", "BE toString");
        xeq(java.util.Arrays.toString(ThaiBuddhistEra.values()), "[BEFORE_BE, BE]", "values order");
        check(ThaiBuddhistEra.valueOf("BE") == ThaiBuddhistEra.BE, "valueOf");
        xeq(ThaiBuddhistEra.BE.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH),
            "BE", "BE full name");
        xeq(ThaiBuddhistEra.BE.getDisplayName(java.time.format.TextStyle.NARROW, java.util.Locale.ENGLISH),
            "BE", "BE narrow name");
        xeq(ThaiBuddhistEra.BEFORE_BE.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH),
            "BC", "BEFORE_BE full name");
        xact(() -> ThaiBuddhistEra.valueOf("BOGUS"), IllegalArgumentException.class,
            "No enum constant java.time.chrono.ThaiBuddhistEra.BOGUS", "valueOf bogus");
        xact(() -> ThaiBuddhistEra.BE.range(java.time.temporal.ChronoField.YEAR_OF_ERA),
            java.time.temporal.UnsupportedTemporalTypeException.class, "Unsupported field: YearOfEra", "range YearOfEra");
        check(ThaiBuddhistChronology.INSTANCE.eraOf(1) == ThaiBuddhistEra.BE, "chronology eraOf identity");
        check(ThaiBuddhistDate.of(2567, 6, 15).getEra() == ThaiBuddhistEra.BE, "date era identity");
    }

}
