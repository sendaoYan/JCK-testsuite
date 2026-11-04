/*
 * @test
 * @summary java.time.chrono.JapaneseChronology conformance: id/calendarType/era.
 * @run main JapaneseChronologyTest
 */

import java.time.chrono.JapaneseChronology;
import java.time.chrono.JapaneseEra;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.chrono.JapaneseDate;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;

public class JapaneseChronologyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testJapaneseChronology() {
        JapaneseChronology jc = JapaneseChronology.INSTANCE;
        check("Japanese".equals(jc.getId()), "id");
        check("japanese".equals(jc.getCalendarType()), "calendarType");
        check(jc.eraOf(2) == JapaneseEra.HEISEI, "eraOf HEISEI");
        check(jc.date(JapaneseEra.HEISEI, 1, 1, 8) != null, "date by era");
        check(jc.isLeapYear(2020), "2020 leap");
    }

    public static void main(String[] args) {
        testJapaneseChronology();
        testDeepJapaneseChronology();
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


    static void testDeepJapaneseChronology() {
        xeq(JapaneseChronology.INSTANCE.getId() + "|" + JapaneseChronology.INSTANCE.getCalendarType(),
            "Japanese|japanese", "ids");
        check(JapaneseChronology.INSTANCE.isLeapYear(2024), "leap 2024");
        check(!JapaneseChronology.INSTANCE.isLeapYear(2023), "not leap 2023");
        xeq(JapaneseChronology.INSTANCE.period(1, 2, 3).toString(), "Japanese P1Y2M3D", "period toString");
        xeq(JapaneseChronology.INSTANCE.eraOf(-1).toString(), "Meiji", "eraOf -1");
        xeq(JapaneseChronology.INSTANCE.eraOf(0).toString(), "Taisho", "eraOf 0");
        xeq(JapaneseChronology.INSTANCE.eraOf(3).toString(), "Reiwa", "eraOf 3");
        xact(() -> JapaneseChronology.INSTANCE.eraOf(6), java.time.DateTimeException.class, "Invalid era: 6", "eraOf 6");
        xact(() -> JapaneseChronology.INSTANCE.eraOf(99), java.time.DateTimeException.class, "Invalid era: 99", "eraOf 99");
        check(JapaneseChronology.INSTANCE.eras().size() == 5, "five eras");
        xeq(JapaneseChronology.INSTANCE.dateEpochDay(19889L).toString(), "Japanese Reiwa 6-06-15", "dateEpochDay");
        xeq(JapaneseChronology.INSTANCE.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH),
            "Japanese Calendar", "display name");
        java.time.Clock fixed = java.time.Clock.fixed(java.time.Instant.parse("2024-06-15T10:15:30Z"), java.time.ZoneOffset.UTC);
        xeq(JapaneseChronology.INSTANCE.dateNow(fixed).toString(), "Japanese Reiwa 6-06-15", "dateNow fixed clock");
        check(JapaneseDate.of(2024, 6, 15).range(java.time.temporal.ChronoField.YEAR_OF_ERA).toString().equals("1 - 292276976"),
            "reiwa year range");
        check(JapaneseDate.of(JapaneseEra.SHOWA, 2, 1, 8).range(java.time.temporal.ChronoField.YEAR_OF_ERA).toString()
            .equals("1 - 63"), "showa year range");
    }

}
