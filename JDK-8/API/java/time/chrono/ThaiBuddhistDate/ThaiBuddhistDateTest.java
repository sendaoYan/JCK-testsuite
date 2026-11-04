/*
 * @test
 * @summary java.time.chrono.ThaiBuddhistDate conformance: era/chronology/from.
 * @run main ThaiBuddhistDateTest
 */

import java.time.LocalDate;
import java.time.chrono.ThaiBuddhistChronology;
import java.time.chrono.ThaiBuddhistDate;
import java.time.chrono.ThaiBuddhistEra;
import java.time.temporal.ChronoField;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class ThaiBuddhistDateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testThaiBuddhistDate() {
        ThaiBuddhistDate td = ThaiBuddhistDate.of(2563, 1, 1);
        check(td.getEra() == ThaiBuddhistEra.BE, "era");
        check(td.getChronology() == ThaiBuddhistChronology.INSTANCE, "chronology");
        check(td.getLong(ChronoField.YEAR) == 2563, "year");
        check(td.equals(ThaiBuddhistDate.from(LocalDate.of(2020, 1, 1))), "from 2020-01-01");
        check(td.lengthOfMonth() == 31, "lengthOfMonth January");
    }

    public static void main(String[] args) {
        testThaiBuddhistDate();
        testDeepThaiBuddhistDate();
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


    static void testDeepThaiBuddhistDate() {
        ThaiBuddhistDate td = ThaiBuddhistDate.of(2567, 6, 15);
        xeq(td.toString(), "ThaiBuddhist BE 2567-06-15", "toString");
        check(td.get(java.time.temporal.ChronoField.YEAR) == 2567, "year 2567");
        check(td.lengthOfYear() == 366, "lengthOfYear 366");
        xeq(java.time.LocalDate.from(td).toString(), "2024-06-15", "to ISO 543 offset");
        xeq(ThaiBuddhistDate.from(java.time.LocalDate.of(2024, 6, 15)).toString(), "ThaiBuddhist BE 2567-06-15", "from ISO");
        xeq(ThaiBuddhistDate.of(0, 6, 15).toString(), "ThaiBuddhist BEFORE_BE 1-06-15", "year 0 BEFORE_BE 1");
        xeq(ThaiBuddhistDate.of(-1, 6, 15).toString(), "ThaiBuddhist BEFORE_BE 2-06-15", "negative year");
        xeq(ThaiBuddhistDate.of(1, 6, 15).toString(), "ThaiBuddhist BE 1-06-15", "BE year 1");
        xeq(td.with(java.time.temporal.ChronoField.YEAR, 2568).toString(), "ThaiBuddhist BE 2568-06-15", "with year");
        check(td.until(ThaiBuddhistDate.of(2568, 6, 15), java.time.temporal.ChronoUnit.YEARS) == 1, "until 1 year");
        check(td.getEra().toString().equals("BE"), "getEra");
        xeq(td.format(java.time.format.DateTimeFormatter.ofPattern("G uuuu-MM-d", java.util.Locale.ENGLISH)),
             "B.E. 2567-06-15", "tb format era pattern");
        check(ThaiBuddhistDate.from(java.time.LocalDate.of(2024, 6, 15)).equals(td), "equals round-trip");
        check(td.toEpochDay() == 19889L, "epochDay");
    }

}
