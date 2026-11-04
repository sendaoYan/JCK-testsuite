/*
 * @test
 * @summary java.time.temporal.TemporalAmount API conformance: Period as a temporal amount.
 * @run main TemporalAmountTest
 */

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.UnsupportedTemporalTypeException;

public class TemporalAmountTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTemporalAmount() {
        TemporalAmount ta = Period.ofDays(3);
        check(ta instanceof Period, "Period is a TemporalAmount");
        check(ta.getUnits().contains(ChronoUnit.DAYS), "unit days present");
        LocalDate start = LocalDate.of(2024, 1, 1);
        check(start.plus(ta).equals(LocalDate.of(2024, 1, 4)), "plus period");
    }

    public static void main(String[] args) {
        testTemporalAmount();
        testDeepTemporalAmount();
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


    static void testDeepTemporalAmount() {
        TemporalAmount p = Period.of(1, 2, 3);
        xeq(p.getUnits().toString(), "[Years, Months, Days]", "period units");
        check(p.get(ChronoUnit.YEARS) == 1 && p.get(ChronoUnit.MONTHS) == 2 && p.get(ChronoUnit.DAYS) == 3, "period get");
        xeq(p.addTo(LocalDate.of(2024, 1, 28)).toString(), "2025-03-31", "addTo");
        xeq(p.subtractFrom(LocalDate.of(2025, 3, 31)).toString(), "2024-01-28", "subtractFrom");
        TemporalAmount d = Duration.ofHours(25);
        xeq(d.getUnits().toString(), "[Seconds, Nanos]", "duration units");
        check(d.get(ChronoUnit.SECONDS) == 90000L && d.get(ChronoUnit.NANOS) == 0L, "duration get");
        xeq(d.addTo(LocalTime.of(0, 0)).toString(), "01:00", "duration addTo wraps");
        xeq(d.subtractFrom(LocalTime.of(2, 0)).toString(), "01:00", "duration subtractFrom wraps");
        xeq(p.addTo(LocalDateTime.of(2024, 1, 31, 10, 0)).toString(), "2025-04-03T10:00", "addTo datetime clamps");
        xact(() -> p.get(ChronoUnit.WEEKS), UnsupportedTemporalTypeException.class, "Unsupported unit: Weeks", "get WEEKS");
        xact(() -> d.get(ChronoUnit.MINUTES), UnsupportedTemporalTypeException.class, "Unsupported unit: Minutes", "duration get MINUTES");
        check(TemporalAmount.class.isAssignableFrom(Period.class), "Period is amount");
        check(TemporalAmount.class.isAssignableFrom(Duration.class), "Duration is amount");
        check(!TemporalAmount.class.isAssignableFrom(LocalDate.class), "date is not amount");
    }

}
