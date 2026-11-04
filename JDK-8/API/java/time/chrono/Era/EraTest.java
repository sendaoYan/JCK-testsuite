/*
 * @test
 * @summary java.time.chrono.Era conformance: value/support/get via IsoEra.
 * @run main EraTest
 */

import java.time.LocalDate;
import java.time.chrono.Era;
import java.time.chrono.IsoEra;
import java.time.temporal.ChronoField;
import java.time.chrono.HijrahChronology;
import java.time.chrono.HijrahDate;
import java.time.chrono.HijrahEra;
import java.time.chrono.IsoChronology;
import java.time.chrono.JapaneseChronology;
import java.time.chrono.JapaneseDate;
import java.time.chrono.JapaneseEra;
import java.time.chrono.MinguoChronology;
import java.time.chrono.MinguoDate;
import java.time.chrono.MinguoEra;
import java.time.chrono.ThaiBuddhistChronology;
import java.time.chrono.ThaiBuddhistDate;
import java.time.chrono.ThaiBuddhistEra;
import java.time.format.TextStyle;
import java.time.temporal.TemporalQueries;
import java.time.temporal.UnsupportedTemporalTypeException;

public class EraTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testEra() {
        Era era = IsoEra.CE;
        check(era.getValue() == 1, "CE value");
        check(era.isSupported(ChronoField.ERA), "isSupported ERA");
        check(era.get(ChronoField.ERA) == 1, "get ERA");
        check(era.range(ChronoField.ERA).getMaximum() == 1, "range ERA max");
        check(era.adjustInto(LocalDate.of(2019, 1, 1)).equals(LocalDate.of(2019, 1, 1)),
              "CE adjustInto no-op");
    }

    public static void main(String[] args) {
        testEra();
        testDeepEra();
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


    static void testDeepEra() {
        xeq(java.util.Arrays.toString(JapaneseChronology.INSTANCE.eras().toArray()),
            "[Meiji, Taisho, Showa, Heisei, Reiwa]", "japanese eras");
        xeq(java.util.Arrays.toString(IsoChronology.INSTANCE.eras().toArray()), "[BCE, CE]", "iso eras");
        xeq(java.util.Arrays.toString(MinguoChronology.INSTANCE.eras().toArray()), "[BEFORE_ROC, ROC]", "minguo eras");
        xeq(java.util.Arrays.toString(ThaiBuddhistChronology.INSTANCE.eras().toArray()), "[BEFORE_BE, BE]", "thai eras");
        xeq(java.util.Arrays.toString(HijrahChronology.INSTANCE.eras().toArray()), "[AH]", "hijrah eras");
        check(JapaneseEra.MEIJI.getValue() == -1 && JapaneseEra.TAISHO.getValue() == 0, "jp era values negative");
        check(JapaneseEra.SHOWA.getValue() == 1 && JapaneseEra.HEISEI.getValue() == 2, "jp era values positive");
        check(MinguoEra.BEFORE_ROC.getValue() == 0 && MinguoEra.ROC.getValue() == 1, "minguo era values");
        check(ThaiBuddhistEra.BEFORE_BE.getValue() == 0 && ThaiBuddhistEra.BE.getValue() == 1, "thai era values");
        check(IsoEra.BCE.getValue() == 0 && IsoEra.CE.getValue() == 1, "iso era values");
        check(HijrahEra.AH.getValue() == 1, "hijrah era value");
        xeq(HijrahDate.of(1445, 1, 1).getEra().toString(), "AH", "hj getEra");
        xeq(JapaneseDate.of(2024, 6, 15).getEra().toString(), "Reiwa", "jp getEra");
        xeq(MinguoDate.of(113, 6, 15).getEra().toString(), "ROC", "mg getEra");
        xeq(ThaiBuddhistDate.of(2567, 6, 15).getEra().toString(), "BE", "tb getEra");
        xeq(java.time.LocalDate.of(2024, 6, 15).getEra().toString(), "CE", "iso getEra");
        xeq(JapaneseEra.SHOWA.query(java.time.temporal.TemporalQueries.precision()).toString(), "Eras", "era precision query");
        xact(() -> JapaneseEra.MEIJI.range(java.time.temporal.ChronoField.YEAR_OF_ERA),
            java.time.temporal.UnsupportedTemporalTypeException.class, "Unsupported field: YearOfEra", "era range YearOfEra");
        xact(() -> IsoEra.CE.range(java.time.temporal.ChronoField.YEAR_OF_ERA),
            java.time.temporal.UnsupportedTemporalTypeException.class, "Unsupported field: YearOfEra", "iso era range");
        xeq(IsoEra.CE.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH),
            "Anno Domini", "CE full name");
        xeq(IsoEra.BCE.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH), "BC", "BCE short name");
        xeq(JapaneseEra.MEIJI.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH),
            "Meiji", "meiji full name");
        xeq(JapaneseEra.HEISEI.getDisplayName(java.time.format.TextStyle.NARROW, java.util.Locale.ENGLISH),
            "H", "heisei narrow name");
        xeq(JapaneseEra.valueOf("Reiwa").getDisplayName(java.time.format.TextStyle.NARROW, java.util.Locale.ENGLISH),
            "R", "reiwa narrow name");
        xeq(JapaneseEra.SHOWA.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH),
             "Showa", "showa short name");
        xeq(HijrahEra.AH.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH),
             "Anno Domini", "hj era full name");
        xeq(HijrahEra.AH.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH),
             "AD", "hj era short name");
        xeq(MinguoEra.ROC.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH),
             "Anno Domini", "mg ROC full name");
        xeq(ThaiBuddhistEra.BE.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH),
             "Anno Domini", "tb BE full name");
    }

}
