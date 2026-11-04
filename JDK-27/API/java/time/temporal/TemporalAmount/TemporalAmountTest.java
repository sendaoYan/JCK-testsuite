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
import java.time.LocalDateTime;
import java.time.temporal.UnsupportedTemporalTypeException;

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
