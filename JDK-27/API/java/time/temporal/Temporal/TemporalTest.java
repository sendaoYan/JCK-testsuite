/*
 * @test
 * @summary java.time.temporal.Temporal conformance: white-box structure + black-box behavior.
 * @run main TemporalTest
 */

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.ZonedDateTime;
import java.time.chrono.JapaneseDate;
import java.time.temporal.TemporalAmount;

public class TemporalTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.temporal.Temporal");
        check(c.isInterface(), "is an interface");
        check(Modifier.isInterface(c.getModifiers()), "modifier is interface");
        boolean extendsAccessor = false;
        for (Class<?> i : c.getInterfaces()) if (i == TemporalAccessor.class) extendsAccessor = true;
        check(extendsAccessor, "extends TemporalAccessor");
        check(c.getMethod("with", TemporalField.class, long.class).getReturnType() == Temporal.class, "with(TemporalField,long) returns Temporal");
        check(c.getMethod("plus", long.class, TemporalUnit.class).getReturnType() == Temporal.class, "plus(long,TemporalUnit) returns Temporal");
        check(c.getMethod("until", Temporal.class, TemporalUnit.class).getReturnType() == long.class, "until returns long");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Temporal t = LocalDate.of(2024, 6, 15);
        check(t instanceof LocalDate, "LocalDate is a Temporal");

        // field/unit support
        check(t.isSupported(ChronoField.YEAR), "isSupported(TemporalField YEAR)");
        check(t.isSupported(ChronoUnit.DAYS), "isSupported(TemporalUnit DAYS)");
        check(!t.isSupported(ChronoUnit.HOURS), "not supported HOURS on LocalDate");

        // with / plus / minus via fields and amounts
        check(t.with(ChronoField.YEAR, 2025).equals(LocalDate.of(2025, 6, 15)), "with year");
        check(t.plus(1, ChronoUnit.DAYS).equals(LocalDate.of(2024, 6, 16)), "plus 1 day");
        check(t.minus(1, ChronoUnit.DAYS).equals(LocalDate.of(2024, 6, 14)), "minus 1 day");
        check(t.plus(Period.ofDays(3)).equals(LocalDate.of(2024, 6, 18)), "plus(TemporalAmount Period)");
        check(t.minus(Period.ofDays(3)).equals(LocalDate.of(2024, 6, 12)), "minus(TemporalAmount Period)");

        // with(TemporalAdjuster)
        TemporalAdjuster firstOfMonth = ta -> ta.with(ChronoField.DAY_OF_MONTH, 1);
        check(t.with(firstOfMonth).equals(LocalDate.of(2024, 6, 1)), "with(TemporalAdjuster)");

        // until
        check(t.until(LocalDate.of(2024, 6, 20), ChronoUnit.DAYS) == 5, "until in days");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
