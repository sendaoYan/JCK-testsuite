/*
 * @test
 * @summary java.time.chrono.MinguoDate conformance: era/chronology/from.
 * @run main MinguoDateTest
 */

import java.time.LocalDate;
import java.time.chrono.MinguoChronology;
import java.time.chrono.MinguoDate;
import java.time.chrono.MinguoEra;
import java.time.temporal.ChronoField;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class MinguoDateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMinguoDate() {
        MinguoDate md = MinguoDate.of(1, 1, 1);
        check(md.getEra() == MinguoEra.ROC, "era");
        check(md.getChronology() == MinguoChronology.INSTANCE, "chronology");
        check(md.getLong(ChronoField.YEAR) == 1, "year");
        check(md.equals(MinguoDate.from(LocalDate.of(1912, 1, 1))), "from 1912-01-01");
        check(md.lengthOfMonth() == 31, "lengthOfMonth January");
        check(md.toEpochDay() == LocalDate.of(1912, 1, 1).toEpochDay(), "toEpochDay");
    }

    public static void main(String[] args) {
        testMinguoDate();
        testDeepMinguoDate();
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


    static void testDeepMinguoDate() {
        MinguoDate md = MinguoDate.of(113, 6, 15);
        xeq(md.toString(), "Minguo ROC 113-06-15", "toString");
        check(md.get(java.time.temporal.ChronoField.YEAR) == 113, "proleptic year 113");
        check(md.get(java.time.temporal.ChronoField.YEAR_OF_ERA) == 113, "year of era 113");
        check(md.lengthOfYear() == 366, "lengthOfYear 366");
        xeq(java.time.LocalDate.from(md).toString(), "2024-06-15", "to ISO 1911 offset");
        check(md.toEpochDay() == 19889L, "epochDay");
        xeq(MinguoDate.from(java.time.LocalDate.of(2024, 6, 15)).toString(), "Minguo ROC 113-06-15", "from ISO");
        xeq(MinguoDate.from(java.time.LocalDate.of(1912, 1, 1)).toString(), "Minguo ROC 1-01-01", "ROC epoch day 1");
        xeq(MinguoDate.from(java.time.LocalDate.of(1911, 12, 31)).toString(), "Minguo BEFORE_ROC 1-12-31", "before ROC boundary");
        xeq(MinguoDate.of(0, 1, 1).toString(), "Minguo BEFORE_ROC 1-01-01", "year 0 is BEFORE_ROC 1");
        xeq(MinguoDate.of(-1, 6, 15).toString(), "Minguo BEFORE_ROC 2-06-15", "negative year counts up in BEFORE_ROC");
        xeq(md.plus(1, java.time.temporal.ChronoUnit.YEARS).toString(), "Minguo ROC 114-06-15", "plus 1 year");
        check(md.until(MinguoDate.of(113, 7, 15), java.time.temporal.ChronoUnit.DAYS) == 30, "until 30 days");
        check(md.getEra().toString().equals("ROC"), "getEra");
        xeq(md.format(java.time.format.DateTimeFormatter.ofPattern("G uuuu-MM-d", java.util.Locale.ENGLISH)),
             "R.O.C. 0113-06-15", "format era pattern");
        check(MinguoDate.of(113, 6, 15).equals(MinguoDate.from(java.time.LocalDate.of(2024, 6, 15))), "equals round-trip");
        xact(() -> MinguoDate.of(113, 2, 30), java.time.DateTimeException.class, "Invalid date 'FEBRUARY 30'", "feb 30");
    }

}
