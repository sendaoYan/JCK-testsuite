/*
 * @test
 * @summary java.time.chrono.IsoEra conformance: BCE/CE values.
 * @run main IsoEraTest
 */

import java.time.chrono.IsoEra;
import java.time.LocalDate;
import java.time.chrono.IsoChronology;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.UnsupportedTemporalTypeException;

public class IsoEraTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIsoEra() {
        check(IsoEra.BCE.getValue() == 0, "BCE value");
        check(IsoEra.CE.getValue() == 1, "CE value");
        check(IsoEra.valueOf("CE") == IsoEra.CE, "valueOf CE");
        check(IsoEra.valueOf("BCE") == IsoEra.BCE, "valueOf BCE");
        check(IsoEra.values().length == 2, "two values");
        check(IsoEra.of(1) == IsoEra.CE, "of(1) CE");
        check(IsoEra.of(0) == IsoEra.BCE, "of(0) BCE");
    }

    public static void main(String[] args) {
        testIsoEra();
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
