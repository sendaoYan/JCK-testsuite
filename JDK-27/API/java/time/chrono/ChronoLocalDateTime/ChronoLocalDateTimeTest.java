/*
 * @test
 * @summary java.time.chrono.ChronoLocalDateTime conformance: white-box structure + black-box behavior.
 * @run main ChronoLocalDateTimeTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChronoLocalDateTimeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> t, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable e) {
            if (t.isInstance(e)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + t.getSimpleName() + " got " + e.getClass().getSimpleName(), e);
        }
        throw new AssertionError(msg + ": expected " + t.getSimpleName() + " but nothing thrown");
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getMethods()) {
            if (m.getName().equals(name)) return true;
        }
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.chrono.ChronoLocalDateTime");
        check(c.isInterface(), "ChronoLocalDateTime is an interface");
        check(java.time.temporal.Temporal.class.isAssignableFrom(c), "extends Temporal");
        check(TemporalAdjuster.class.isAssignableFrom(c), "extends TemporalAdjuster");
        check(Comparable.class.isAssignableFrom(c), "extends Comparable");
        for (String m : new String[]{"timeLineOrder", "from", "getChronology", "toLocalDate",
                "toLocalTime", "isSupported", "with", "plus", "minus", "query", "adjustInto",
                "format", "atZone", "toInstant", "toEpochSecond", "compareTo", "isAfter", "isBefore",
                "isEqual"}) {
            check(hasMethod(c, m), "ChronoLocalDateTime has method " + m);
        }
    }

    static void testBlackBox() {
        ChronoLocalDateTime<?> dt = LocalDateTime.of(2020, 1, 1, 12, 0);
        check(dt.toLocalDate().equals(LocalDate.of(2020, 1, 1)), "toLocalDate");
        check(dt.toLocalTime().equals(LocalTime.of(12, 0)), "toLocalTime");
        check(dt.getChronology() == IsoChronology.INSTANCE, "getChronology");

        // Field access.
        check(dt.isSupported(ChronoField.HOUR_OF_DAY), "isSupported HOUR_OF_DAY");
        check(!dt.isSupported(ChronoField.INSTANT_SECONDS), "!isSupported INSTANT_SECONDS");
        check(dt.get(ChronoField.HOUR_OF_DAY) == 12, "get HOUR_OF_DAY");
        check(dt.getLong(ChronoField.YEAR) == 2020L, "getLong YEAR");
        check(dt.get(ChronoField.DAY_OF_MONTH) == 1, "get DAY_OF_MONTH");

        // plus / minus / with.
        check(dt.plus(1, ChronoUnit.HOURS).equals(LocalDateTime.of(2020, 1, 1, 13, 0)), "plus hour");
        check(dt.plus(1, ChronoUnit.DAYS).equals(LocalDateTime.of(2020, 1, 2, 12, 0)), "plus day");
        check(dt.minus(1, ChronoUnit.HOURS).equals(LocalDateTime.of(2020, 1, 1, 11, 0)), "minus hour");
        check(dt.with(ChronoField.YEAR, 2021).equals(LocalDateTime.of(2021, 1, 1, 12, 0)), "with YEAR");

        // toInstant / toEpochSecond with fixed offset.
        Instant instant = Instant.parse("2020-01-01T12:00:00Z");
        check(dt.toInstant(ZoneOffset.UTC).equals(instant), "toInstant UTC");
        check(dt.toEpochSecond(ZoneOffset.UTC) == instant.getEpochSecond(), "toEpochSecond UTC");

        // atZone (to ChronoZonedDateTime) and back.
        ChronoZonedDateTime<?> zdt = dt.atZone(ZoneId.of("UTC"));
        check(zdt.toLocalDateTime().equals(dt), "atZone UTC round-trip localDateTime");
        check(zdt.getZone().getId().equals("UTC"), "atZone keeps zone");

        // Ordering.
        check(dt.isBefore(LocalDateTime.of(2020, 1, 1, 13, 0)), "isBefore");
        check(dt.isAfter(LocalDateTime.of(2020, 1, 1, 11, 0)), "isAfter");
        check(dt.isEqual(LocalDateTime.of(2020, 1, 1, 12, 0)), "isEqual");
        check(dt.compareTo(LocalDateTime.of(2020, 1, 1, 12, 1)) < 0, "compareTo < 0");

        // timeLineOrder sorts chronologically.
        List<ChronoLocalDateTime<?>> list = new ArrayList<ChronoLocalDateTime<?>>();
        list.add(LocalDateTime.of(2020, 1, 2, 0, 0));
        list.add(LocalDateTime.of(2020, 1, 1, 12, 0));
        list.add(LocalDateTime.of(2019, 12, 31, 23, 59));
        Collections.sort(list, ChronoLocalDateTime.timeLineOrder());
        check(list.get(0).equals(LocalDateTime.of(2019, 12, 31, 23, 59))
              && list.get(1).equals(LocalDateTime.of(2020, 1, 1, 12, 0))
              && list.get(2).equals(LocalDateTime.of(2020, 1, 2, 0, 0)), "timeLineOrder sort");

        // format.
        check(dt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).equals("2020-01-01T12:00:00"), "format");

        // Exception path: unsupported field.
        expectThrows(java.time.temporal.UnsupportedTemporalTypeException.class,
              () -> dt.with(ChronoField.INSTANT_SECONDS, 0),
              "with INSTANT_SECONDS throws");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
