/*
 * @test
 * @summary java.time.temporal.Temporal API conformance: with/plus via a LocalDate.
 * @run main TemporalTest
 */

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.ZonedDateTime;
import java.time.chrono.JapaneseDate;
import java.time.temporal.TemporalAmount;

public class TemporalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTemporal() {
        Temporal t = LocalDate.of(2024, 6, 15);
        check(t instanceof LocalDate, "LocalDate is a Temporal");
        check(t.isSupported(ChronoField.YEAR), "supports year");
        check(t.with(ChronoField.YEAR, 2025).equals(LocalDate.of(2025, 6, 15)),
              "with year");
        check(t.plus(1, ChronoUnit.DAYS).equals(LocalDate.of(2024, 6, 16)),
              "plus day");
    }

    public static void main(String[] args) {
        testTemporal();
        testDeepTemporal();
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


    static void testDeepTemporal() {
        // interface: default plus/minus(TemporalAmount), with, until; probe on LocalTime
        LocalTime t = LocalTime.of(10, 0);
        check(t.plus(Duration.ofMinutes(90)).toString().equals("11:30"), "default plus amount");
        check(t.minus(Duration.ofMinutes(90)).toString().equals("08:30"), "default minus amount");
        check(t.plus(2, ChronoUnit.HOURS).toString().equals("12:00"), "plus unit");
        check(t.minus(2, ChronoUnit.HOURS).toString().equals("08:00"), "minus unit");
        check(t.with(LocalTime.NOON).toString().equals("12:00"), "with adjuster");
        check(t.with(ChronoField.HOUR_OF_DAY, 5).toString().equals("05:00"), "with field");
        check(Temporal.class.isAssignableFrom(LocalDate.class), "LD is Temporal");
        check(Temporal.class.isAssignableFrom(LocalTime.class), "LT is Temporal");
        check(Temporal.class.isAssignableFrom(LocalDateTime.class), "LDT is Temporal");
        check(Temporal.class.isAssignableFrom(ZonedDateTime.class), "ZDT is Temporal");
        check(Temporal.class.isAssignableFrom(Instant.class), "Instant is Temporal");
        check(Temporal.class.isAssignableFrom(Year.class), "Year is Temporal");
        check(Temporal.class.isAssignableFrom(OffsetTime.class), "OT is Temporal");
        check(Temporal.class.isAssignableFrom(JapaneseDate.class), "JapaneseDate is Temporal");
        check(!Temporal.class.isAssignableFrom(Period.class), "Period is not Temporal");
        check(!Temporal.class.isAssignableFrom(Duration.class), "Duration is not Temporal");
        LocalDate a = LocalDate.of(2024, 2, 29);
        check(a.until(LocalDate.of(2024, 3, 1), ChronoUnit.DAYS) == 1, "until unit");
        check(a.until(LocalDate.of(2025, 2, 28)).equals(Period.of(0, 11, 30)), "until Period");
        check(a.isSupported(ChronoUnit.DAYS), "isSupported unit");
        check(a.isSupported(ChronoField.YEAR), "isSupported field");
        check(a.isSupported(ChronoUnit.CENTURIES), "centuries supported");
    }

}
