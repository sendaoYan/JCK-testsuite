/*
 * @test
 * @summary java.time.temporal.TemporalUnit conformance: via ChronoUnit.DAYS.
 * @run main TemporalUnitTest
 */

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.time.LocalTime;
import java.time.temporal.UnsupportedTemporalTypeException;

public class TemporalUnitTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTemporalUnit() {
        TemporalUnit days = ChronoUnit.DAYS;
        check(days.isDateBased(), "DAYS isDateBased");
        check(!days.isTimeBased(), "DAYS not isTimeBased");
        check(days.isDurationEstimated(), "DAYS isDurationEstimated");
        check(days.getDuration().equals(Duration.ofDays(1)), "DAYS duration");
        check(days.isSupportedBy(LocalDate.now()), "DAYS supported by LocalDate");
        check(LocalDate.of(2020, 1, 1).plus(1, days).equals(LocalDate.of(2020, 1, 2)), "DAYS plus");

        TemporalUnit hours = ChronoUnit.HOURS;
        check(hours.isTimeBased(), "HOURS isTimeBased");
        check(!hours.isDurationEstimated(), "HOURS not isDurationEstimated");
        check(hours.getDuration().equals(Duration.ofHours(1)), "HOURS duration");
    }

    public static void main(String[] args) {
        testTemporalUnit();
        testDeepTemporalUnit();
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


    static void testDeepTemporalUnit() {
        TemporalUnit u = ChronoUnit.WEEKS;
        xeq(u.getDuration().toString(), "PT168H", "duration");
        check(u.isDurationEstimated(), "weeks estimated");
        check(u.isDateBased(), "date based");
        xeq(u.addTo(LocalDate.of(2024, 2, 22), 1).toString(), "2024-02-29", "addTo");
        check(u.between(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29)) == 4, "between");
        xact(() -> u.addTo(LocalTime.NOON, 1), UnsupportedTemporalTypeException.class,
            "Unsupported unit: Weeks", "addTo time");
        xact(() -> u.between(LocalTime.NOON, LocalTime.MIDNIGHT), UnsupportedTemporalTypeException.class,
            "Unsupported unit: Weeks", "between times");
        TemporalUnit min = ChronoUnit.MINUTES;
        xeq(min.getDuration().toString(), "PT1M", "minutes duration");
        check(!min.isDurationEstimated(), "minutes exact");
        check(min.isTimeBased(), "minutes time based");
        check(!min.isDateBased(), "minutes not date based");
        check(ChronoUnit.DAYS.between(LocalDate.of(2024, 2, 28), LocalDate.of(2024, 3, 1)) == 2, "days between leap");
        xeq(ChronoUnit.HOURS.addTo(LocalTime.of(23, 0), 2).toString(), "01:00", "hours addTo wraps");
        xeq(ChronoUnit.FOREVER.toString(), "Forever", "FOREVER toString");
        check(ChronoUnit.FOREVER.isDurationEstimated(), "FOREVER estimated");
        xeq(ChronoUnit.MILLENNIA.getDuration().toString(), "PT8765820H", "millennia duration");
        xact(() -> ChronoUnit.FOREVER.addTo(LocalDate.of(2024, 1, 1), 1), UnsupportedTemporalTypeException.class,
            "Unsupported unit: Forever", "FOREVER addTo");
    }

}
