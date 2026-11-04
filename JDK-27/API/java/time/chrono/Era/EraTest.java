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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
