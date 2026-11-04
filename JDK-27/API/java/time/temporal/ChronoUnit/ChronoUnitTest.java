/*
 * @test
 * @summary java.time.temporal.ChronoUnit conformance: white-box structure + black-box behavior.
 * @run main ChronoUnitTest
 */

import java.lang.reflect.Modifier;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.UnsupportedTemporalTypeException;

public class ChronoUnitTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.temporal.ChronoUnit");
        check(c.isEnum(), "is an enum");
        check(Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() == Enum.class, "extends Enum");
        boolean implementsTemporalUnit = false;
        for (Class<?> i : c.getInterfaces()) if (i == TemporalUnit.class) implementsTemporalUnit = true;
        check(implementsTemporalUnit, "implements TemporalUnit");
        check(c.getEnumConstants().length == 16, "16 constants");
        check(ChronoUnit.valueOf("DAYS") == ChronoUnit.DAYS, "valueOf DAYS round-trip");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // durations
        check(ChronoUnit.DAYS.getDuration().equals(Duration.ofSeconds(86400)), "DAYS == 86400s");
        check(ChronoUnit.HOURS.getDuration().equals(Duration.ofSeconds(3600)), "HOURS == 3600s");
        check(ChronoUnit.MINUTES.getDuration().equals(Duration.ofSeconds(60)), "MINUTES == 60s");

        // duration estimation (DST makes days estimated; hours are exact)
        check(ChronoUnit.DAYS.isDurationEstimated(), "DAYS.isDurationEstimated() is true");
        check(!ChronoUnit.HOURS.isDurationEstimated(), "HOURS.isDurationEstimated() is false");

        // date/time classification
        check(ChronoUnit.DAYS.isDateBased() && !ChronoUnit.DAYS.isTimeBased(), "DAYS date-based only");
        check(ChronoUnit.HOURS.isTimeBased() && !ChronoUnit.HOURS.isDateBased(), "HOURS time-based only");
        check(!ChronoUnit.FOREVER.isDateBased() && !ChronoUnit.FOREVER.isTimeBased(), "FOREVER neither");

        // arithmetic
        check(ChronoUnit.MONTHS.addTo(LocalDate.of(2024, 1, 31), 1).equals(LocalDate.of(2024, 2, 29)), "MONTHS.addTo Jan31 +1 -> Feb29");
        check(ChronoUnit.DAYS.between(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 11)) == 10, "DAYS.between == 10");
        check(ChronoUnit.WEEKS.between(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 8)) == 1, "WEEKS.between == 1");

        // support
        check(ChronoUnit.DAYS.isSupportedBy(LocalDate.of(2024, 1, 1)), "DAYS supported by LocalDate");
        check(!ChronoUnit.HOURS.isSupportedBy(LocalDate.of(2024, 1, 1)), "HOURS not supported by LocalDate");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepChronoUnit();
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


    static void testDeepChronoUnit() {
        // exact duration table (probe-locked identical both sides)
        String[][] dur = {
            {"NANOS","PT0.000000001S"}, {"MICROS","PT0.000001S"}, {"MILLIS","PT0.001S"},
            {"SECONDS","PT1S"}, {"MINUTES","PT1M"}, {"HOURS","PT1H"}, {"HALF_DAYS","PT12H"},
            {"DAYS","PT24H"}, {"WEEKS","PT168H"}, {"MONTHS","PT730H29M6S"},
            {"YEARS","PT8765H49M12S"}, {"DECADES","PT87658H12M"},
            {"CENTURIES","PT876582H"}, {"MILLENNIA","PT8765820H"}, {"ERAS","PT8765820000000H"},
        };
        for (String[] e : dur)
            xeq(ChronoUnit.valueOf(e[0]).getDuration().toString(), e[1], "duration " + e[0]);
        xeq(ChronoUnit.FOREVER.getDuration().toString(), "PT2562047788015215H30M7.999999999S", "duration FOREVER");
        boolean[][] flags = { // {dateBased, timeBased, durationEstimated}
            {false,true,false},{false,true,false},{false,true,false},{false,true,false},
            {false,true,false},{false,true,false},{false,true,false},
            {true,false,true},{true,false,true},{true,false,true},{true,false,true},
            {true,false,true},{true,false,true},{true,false,true},{true,false,true},
            {false,false,true}};
        ChronoUnit[] us = ChronoUnit.values();
        check(us.length == 16, "16 units");
        for (int i = 0; i < us.length; i++) {
            check(us[i].isDateBased() == flags[i][0], "dateBased " + us[i]);
            check(us[i].isTimeBased() == flags[i][1], "timeBased " + us[i]);
            check(us[i].isDurationEstimated() == flags[i][2], "estimated " + us[i]);
        }
        check(ChronoUnit.DAYS.getDuration().compareTo(ChronoUnit.HOURS.getDuration().multipliedBy(24)) == 0, "DAYS = 24H");
        check(ChronoUnit.WEEKS.getDuration().equals(ChronoUnit.DAYS.getDuration().multipliedBy(7)), "WEEKS = 7 DAYS");
        check(ChronoUnit.HALF_DAYS.getDuration().equals(ChronoUnit.HOURS.getDuration().multipliedBy(12)), "HALF_DAYS = 12H");
        LocalDate a = LocalDate.of(2024, 2, 29), b = LocalDate.of(2024, 3, 5);
        check(ChronoUnit.DAYS.between(a, b) == 5, "between DAYS");
        check(ChronoUnit.WEEKS.between(a, b) == 0, "between WEEKS 0");
        check(ChronoUnit.MONTHS.between(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 1)) == 2, "between MONTHS");
        check(ChronoUnit.YEARS.between(LocalDate.of(2020, 6, 1), LocalDate.of(2024, 6, 1)) == 4, "between YEARS");
        check(ChronoUnit.DECADES.between(LocalDate.of(1980, 1, 1), LocalDate.of(2024, 1, 1)) == 4, "between DECADES");
        check(ChronoUnit.ERAS.between(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 1)) == 0, "between ERAS same");
        LocalTime t1 = LocalTime.of(10, 0), t2 = LocalTime.of(12, 30);
        check(ChronoUnit.MINUTES.between(t1, t2) == 150, "between MINUTES");
        check(ChronoUnit.HOURS.between(t1, t2) == 2, "between HOURS trunc");
        check(ChronoUnit.NANOS.between(t1, t2) == 9000000000000L, "between NANOS");
        check(ChronoUnit.MICROS.between(t1, t2) == 9000000000L, "between MICROS");
        check(ChronoUnit.DAYS.isSupportedBy(a), "DAYS supported by date");
        check(!ChronoUnit.DAYS.isSupportedBy(t1), "DAYS not supported by time");
        check(ChronoUnit.HOURS.isSupportedBy(t1), "HOURS supported by time");
        check(!ChronoUnit.HOURS.isSupportedBy(a), "HOURS not supported by date");
        check(ChronoUnit.HOURS.isSupportedBy(LocalDateTime.of(2024, 1, 1, 5, 0)), "HOURS supported by datetime");
        xact(() -> ChronoUnit.DAYS.between(a, t1), DateTimeException.class,
            "Unable to obtain LocalDate from TemporalAccessor: 10:00 of type java.time.LocalTime", "between mismatched types");
        xact(() -> ChronoUnit.valueOf("BOGUS"), IllegalArgumentException.class, null, "valueOf bogus");
        xact(() -> ChronoUnit.MONTHS.addTo(LocalTime.NOON, 1), UnsupportedTemporalTypeException.class,
            "Unsupported unit: Months", "addTo time");
        check(ChronoUnit.DAYS.addTo(LocalDate.of(2024, 2, 28), 1).toString().equals("2024-02-29"), "addTo date");
        xact(() -> ChronoUnit.MINUTES.addTo(LocalDate.of(2024, 1, 1), 1), UnsupportedTemporalTypeException.class,
            "Unsupported unit: Minutes", "addTo date minutes");
        xeq(ChronoUnit.MONTHS.toString(), "Months", "toString");
        check(ChronoUnit.DAYS.getDuration().get(ChronoUnit.SECONDS) == 86400L, "DAYS seconds");
        check(ChronoUnit.YEARS.compareTo(ChronoUnit.MONTHS) > 0, "unit compare Years>Months");
        check(ChronoUnit.NANOS.compareTo(ChronoUnit.MICROS) < 0, "unit compare Nanos<Micros");
    }

}
