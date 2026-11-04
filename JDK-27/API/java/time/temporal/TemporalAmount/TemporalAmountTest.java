/*
 * @test
 * @summary java.time.temporal.TemporalAmount conformance: white-box structure + black-box behavior.
 * @run main TemporalAmountTest
 */

import java.lang.reflect.Modifier;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.List;

public class TemporalAmountTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.temporal.TemporalAmount");
        check(c.isInterface(), "is an interface");
        check(Modifier.isInterface(c.getModifiers()), "modifier is interface");
        check(c.getMethod("get", TemporalUnit.class).getReturnType() == long.class, "get returns long");
        check(c.getMethod("getUnits").getReturnType() == List.class, "getUnits returns List");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // Period as a TemporalAmount
        TemporalAmount p = Period.of(1, 2, 3);
        check(p instanceof Period, "Period is a TemporalAmount");
        List<TemporalUnit> units = p.getUnits();
        check(units.size() == 3, "Period has 3 units");
        check(units.contains(ChronoUnit.YEARS) && units.contains(ChronoUnit.MONTHS) && units.contains(ChronoUnit.DAYS), "Period units YEARS/MONTHS/DAYS");
        check(p.get(ChronoUnit.YEARS) == 1, "Period get YEARS");
        check(p.get(ChronoUnit.MONTHS) == 2, "Period get MONTHS");
        check(p.get(ChronoUnit.DAYS) == 3, "Period get DAYS");

        // addTo / subtractFrom via Period and via Temporal.plus
        LocalDate start = LocalDate.of(2024, 1, 1);
        check(p.addTo(start).equals(LocalDate.of(2025, 3, 4)), "Period.addTo date");
        check(p.subtractFrom(start).equals(LocalDate.of(2022, 10, 29)), "Period.subtractFrom date");
        check(start.plus(Period.ofDays(3)).equals(LocalDate.of(2024, 1, 4)), "Temporal.plus(Period)");

        // Duration as a TemporalAmount
        TemporalAmount d = Duration.ofHours(2);
        check(d instanceof Duration, "Duration is a TemporalAmount");
        check(d.getUnits().size() == 2, "Duration two units");
        check(d.getUnits().contains(ChronoUnit.SECONDS) && d.getUnits().contains(ChronoUnit.NANOS), "Duration units SECONDS/NANOS");
        check(d.get(ChronoUnit.SECONDS) == 7200, "Duration get SECONDS 7200");
        check(d.get(ChronoUnit.NANOS) == 0, "Duration get NANOS 0");
        check(d.addTo(LocalTime.of(1, 0)).equals(LocalTime.of(3, 0)), "Duration.addTo LocalTime");
        check(d.subtractFrom(LocalTime.of(1, 0)).equals(LocalTime.of(23, 0)), "Duration.subtractFrom LocalTime");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
