/*
 * @test
 * @summary java.time.chrono.Era conformance: white-box structure + black-box behavior.
 * @run main EraTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.chrono.Era;
import java.time.chrono.IsoEra;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.ValueRange;
import java.util.Locale;
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
import java.time.temporal.TemporalQueries;
import java.time.temporal.UnsupportedTemporalTypeException;

public class EraTest {
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
        Class<?> c = Class.forName("java.time.chrono.Era");
        check(c.isInterface(), "Era is an interface");
        check(TemporalAccessor.class.isAssignableFrom(c), "extends TemporalAccessor");
        check(TemporalAdjuster.class.isAssignableFrom(c), "extends TemporalAdjuster");
        for (String m : new String[]{"getValue", "isSupported", "range", "get", "getLong", "query",
                "adjustInto", "getDisplayName"}) {
            check(hasMethod(c, m), "Era has method " + m);
        }
    }

    static void testBlackBox() {
        Era ce = IsoEra.CE;
        Era bce = IsoEra.BCE;
        check(ce.getValue() == 1, "CE value 1");
        check(bce.getValue() == 0, "BCE value 0");

        // Support / field access.
        check(ce.isSupported(ChronoField.ERA), "isSupported ERA");
        check(ce.get(ChronoField.ERA) == 1, "get ERA CE");
        check(bce.get(ChronoField.ERA) == 0, "get ERA BCE");
        check(ce.getLong(ChronoField.ERA) == 1L, "getLong ERA CE");
        check(ce.range(ChronoField.ERA).equals(ValueRange.of(0, 1)), "range ERA [0,1]");

        // adjustInto.
        check(ce.adjustInto(LocalDate.of(2019, 1, 1)).equals(LocalDate.of(2019, 1, 1)),
              "CE adjustInto no-op");
        check(ce.adjustInto(LocalDate.of(0, 1, 1)).equals(LocalDate.of(1, 1, 1)),
              "CE adjustInto BCE->CE");
        check(bce.adjustInto(LocalDate.of(2019, 1, 1)).equals(LocalDate.of(-2018, 1, 1)),
              "BCE adjustInto CE->BCE (2019 CE -> 2019 BCE)");

        // getDisplayName stable English text.
        check("AD".equals(ce.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)), "CE SHORT AD");
        check("BC".equals(bce.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)), "BCE SHORT BC");
        check("Anno Domini".equals(ce.getDisplayName(TextStyle.FULL, Locale.ENGLISH)), "CE FULL");
        check("Before Christ".equals(bce.getDisplayName(TextStyle.FULL, Locale.ENGLISH)), "BCE FULL");

        // query precision returns ERAS.
        check(ce.query(java.time.temporal.TemporalQueries.precision()) == java.time.temporal.ChronoUnit.ERAS,
              "query precision == ERAS");
        check(ce.query(java.time.temporal.TemporalQueries.chronology()) == null, "query chronology null");

        // Era does not support non-ERA fields.
        expectThrows(java.time.temporal.UnsupportedTemporalTypeException.class,
              () -> ce.get(ChronoField.YEAR), "get YEAR throws");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
            "Sh\u014dwa", "showa short name");
        xeq(HijrahEra.AH.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH),
            "Anno Hegirae", "hj era full name");
        xeq(HijrahEra.AH.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH),
            "AH", "hj era short name");
        xeq(MinguoEra.ROC.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH),
            "Minguo", "mg ROC full name");
        xeq(ThaiBuddhistEra.BE.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH),
            "BE", "tb BE full name");
    }

}
