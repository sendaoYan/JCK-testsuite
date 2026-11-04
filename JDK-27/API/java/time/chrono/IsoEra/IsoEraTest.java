/*
 * @test
 * @summary java.time.chrono.IsoEra conformance: white-box structure + black-box behavior.
 * @run main IsoEraTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.chrono.Era;
import java.time.chrono.IsoEra;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ValueRange;
import java.util.Locale;
import java.time.chrono.IsoChronology;
import java.time.temporal.UnsupportedTemporalTypeException;

public class IsoEraTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> t, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable e) {
            if (t.isInstance(e)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + t.getSimpleName() + " got " + e.getClass().getSimpleName(), e);
        }
        throw new AssertionError(msg + ": expected " + t.getSimpleName() + " but nothing thrown");
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getMethods()) {
            if (m.getName().equals(name)) return true;
        }
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.chrono.IsoEra");
        check(c.isEnum(), "IsoEra is an enum");
        check(Modifier.isFinal(c.getModifiers()), "IsoEra is final");
        check(c.getSuperclass() == Enum.class, "IsoEra extends Enum");
        check(Era.class.isAssignableFrom(c), "IsoEra implements Era");
        check(hasMethod(c, "values"), "has values");
        check(hasMethod(c, "valueOf"), "has valueOf");
        check(hasMethod(c, "of"), "has of");
        check(hasMethod(c, "getValue"), "has getValue");
    }

    static void testBlackBox() {
        check(IsoEra.BCE.getValue() == 0, "BCE value 0");
        check(IsoEra.CE.getValue() == 1, "CE value 1");

        // valueOf / values / of.
        check(IsoEra.valueOf("CE") == IsoEra.CE, "valueOf CE");
        check(IsoEra.valueOf("BCE") == IsoEra.BCE, "valueOf BCE");
        check(IsoEra.values().length == 2, "two values");
        check(IsoEra.values()[0] == IsoEra.BCE && IsoEra.values()[1] == IsoEra.CE,
              "values order BCE,CE");
        check(IsoEra.of(1) == IsoEra.CE, "of(1) CE");
        check(IsoEra.of(0) == IsoEra.BCE, "of(0) BCE");

        // Field access via Era/TemporalAccessor.
        check(IsoEra.CE.isSupported(ChronoField.ERA), "isSupported ERA");
        check(IsoEra.CE.get(ChronoField.ERA) == 1, "get ERA CE");
        check(IsoEra.BCE.get(ChronoField.ERA) == 0, "get ERA BCE");
        check(IsoEra.CE.getLong(ChronoField.ERA) == 1L, "getLong ERA");
        check(IsoEra.CE.range(ChronoField.ERA).equals(ValueRange.of(0, 1)), "range ERA [0,1]");

        // getDisplayName stable English.
        check("AD".equals(IsoEra.CE.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)), "CE SHORT AD");
        check("BC".equals(IsoEra.BCE.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)), "BCE SHORT BC");
        check("Anno Domini".equals(IsoEra.CE.getDisplayName(TextStyle.FULL, Locale.ENGLISH)), "CE FULL");
        check("Before Christ".equals(IsoEra.BCE.getDisplayName(TextStyle.FULL, Locale.ENGLISH)), "BCE FULL");

        // adjustInto.
        check(IsoEra.CE.adjustInto(LocalDate.of(0, 1, 1)).equals(LocalDate.of(1, 1, 1)), "CE adjustInto BCE->CE");
        check(IsoEra.BCE.adjustInto(LocalDate.of(2019, 1, 1)).equals(LocalDate.of(-2018, 1, 1)), "BCE adjustInto CE->BCE");

        // Exception paths.
        expectThrows(IllegalArgumentException.class,
              () -> IsoEra.valueOf("XX"), "valueOf XX throws IllegalArgumentException");
        expectThrows(java.time.DateTimeException.class,
              () -> IsoEra.of(2), "of(2) throws DateTimeException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepIsoEra();
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


    static void testDeepIsoEra() {
        check(IsoEra.BCE.getValue() == 0, "BCE value");
        check(IsoEra.CE.getValue() == 1, "CE value");
        xeq(IsoEra.BCE.toString(), "BCE", "BCE toString");
        xeq(IsoEra.CE.toString(), "CE", "CE toString");
        xeq(java.util.Arrays.toString(IsoEra.values()), "[BCE, CE]", "values order");
        check(IsoEra.valueOf("BCE") == IsoEra.BCE && IsoEra.valueOf("CE") == IsoEra.CE, "valueOf");
        xeq(IsoEra.CE.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH),
            "Anno Domini", "CE full name");
        xeq(IsoEra.BCE.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH), "BC", "BCE short name");
        xeq(IsoEra.CE.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH), "AD", "CE short name");
        xeq(IsoChronology.INSTANCE.eraOf(0).toString(), "BCE", "chronology eraOf BCE");
        xeq(java.time.LocalDate.of(2024, 6, 15).getEra().toString(), "CE", "LocalDate getEra");
        xeq(java.time.LocalDate.of(-5, 6, 15).getEra().toString(), "BCE", "negative year era");
        check(java.time.LocalDate.of(-5, 6, 15).get(java.time.temporal.ChronoField.YEAR_OF_ERA) == 6, "BCE year of era counts up");
        check(java.time.LocalDate.of(2024, 6, 15).get(java.time.temporal.ChronoField.YEAR_OF_ERA) == 2024, "CE year of era");
        xact(() -> IsoEra.valueOf("BOGUS"), IllegalArgumentException.class,
            "No enum constant java.time.chrono.IsoEra.BOGUS", "valueOf bogus");
        xact(() -> IsoEra.CE.range(java.time.temporal.ChronoField.YEAR_OF_ERA),
            java.time.temporal.UnsupportedTemporalTypeException.class, "Unsupported field: YearOfEra", "range YearOfEra");
    }

}
