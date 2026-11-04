/*
 * @test
 * @summary java.time.temporal.TemporalAdjuster conformance: white-box structure + black-box behavior.
 * @run main TemporalAdjusterTest
 */

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.Year;
import java.time.temporal.TemporalAdjusters;

public class TemporalAdjusterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.temporal.TemporalAdjuster");
        check(c.isInterface(), "is an interface");
        check(Modifier.isInterface(c.getModifiers()), "modifier is interface");
        check(c.getInterfaces().length == 0, "has no superinterfaces");
        java.lang.reflect.Method m = c.getMethod("adjustInto", Temporal.class);
        check(m.getReturnType() == Temporal.class, "adjustInto returns Temporal");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        LocalDate base = LocalDate.of(2024, 6, 15);

        // functional (lambda) adjuster: force day-of-month to 1
        TemporalAdjuster firstOfMonth = t -> t.with(ChronoField.DAY_OF_MONTH, 1);
        Temporal adjusted = firstOfMonth.adjustInto(base);
        check(adjusted.equals(LocalDate.of(2024, 6, 1)), "adjustInto sets day-of-month 1");

        // via Temporal.with(TemporalAdjuster)
        LocalDate viaWith = base.with(firstOfMonth);
        check(viaWith.equals(LocalDate.of(2024, 6, 1)), "LocalDate.with(TemporalAdjuster)");

        // composition-style adjuster: add days then clamp day-of-month
        TemporalAdjuster plusOneDay = t -> t.plus(1, ChronoUnit.DAYS);
        check(plusOneDay.adjustInto(base).equals(LocalDate.of(2024, 6, 16)), "adjustInto adds one day");

        // adjuster returning same value
        TemporalAdjuster identity = t -> t;
        check(identity.adjustInto(base).equals(base), "identity adjuster");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepTemporalAdjuster();
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


    static void testDeepTemporalAdjuster() {
        TemporalAdjuster toFriday = temporal -> temporal.with(ChronoField.DAY_OF_WEEK, 5);
        xeq(LocalDate.of(2024, 2, 29).with(toFriday).toString(), "2024-03-01", "lambda adjuster");
        xeq(LocalTime.of(10, 0).with(t -> t.with(ChronoField.MINUTE_OF_HOUR, 30)).toString(), "10:30", "time adjuster");
        xeq(TemporalAdjusters.lastDayOfMonth().adjustInto(LocalDate.of(2024, 2, 10)).toString(), "2024-02-29", "adjustInto");
        xeq(LocalDate.of(2024, 6, 15).with(TemporalAdjusters.firstDayOfMonth()).with(TemporalAdjusters.next(DayOfWeek.FRIDAY)).toString(),
            "2024-06-07", "chained adjusters");
        xact(() -> LocalDate.of(2024, 1, 1).with((TemporalAdjuster) null), NullPointerException.class, null, "with null adjuster");
        check(TemporalAdjuster.class.isAssignableFrom(LocalDate.class), "LocalDate is adjuster");
        check(TemporalAdjuster.class.isAssignableFrom(LocalTime.class), "LocalTime is adjuster");
        check(TemporalAdjuster.class.isAssignableFrom(Year.class), "Year is adjuster");
        xeq(LocalDate.of(2024, 2, 29).with(TemporalAdjusters.ofDateAdjuster(d2 -> d2.plusWeeks(1))).toString(), "2024-03-07", "ofDateAdjuster");
        xact(() -> TemporalAdjusters.ofDateAdjuster(null), NullPointerException.class, null, "ofDateAdjuster null");
        xeq(TemporalAdjusters.lastInMonth(DayOfWeek.SUNDAY).adjustInto(LocalDate.of(2024, 2, 1)).toString(), "2024-02-25", "lastInMonth via adjustInto");
    }

}
