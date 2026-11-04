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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
