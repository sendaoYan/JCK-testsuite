/*
 * @test
 * @summary java.time.chrono.ChronoZonedDateTime conformance: white-box structure + black-box behavior.
 * @run main ChronoZonedDateTimeTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChronoZonedDateTimeTest {
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
        Class<?> c = Class.forName("java.time.chrono.ChronoZonedDateTime");
        check(c.isInterface(), "ChronoZonedDateTime is an interface");
        check(java.time.temporal.Temporal.class.isAssignableFrom(c), "extends Temporal");
        check(Comparable.class.isAssignableFrom(c), "extends Comparable");
        for (String m : new String[]{"timeLineOrder", "from", "range", "get", "getLong", "toLocalDate",
                "toLocalTime", "toLocalDateTime", "getChronology", "getOffset", "getZone",
                "withEarlierOffsetAtOverlap", "withLaterOffsetAtOverlap", "withZoneSameLocal",
                "withZoneSameInstant", "isSupported", "with", "plus", "minus", "query", "format",
                "toInstant", "toEpochSecond", "compareTo", "isBefore", "isAfter", "isEqual"}) {
            check(hasMethod(c, m), "ChronoZonedDateTime has method " + m);
        }
    }

    static void testBlackBox() {
        ZoneId utc = ZoneId.of("UTC");
        ChronoZonedDateTime<?> zdt = ZonedDateTime.of(2020, 1, 1, 12, 0, 0, 0, utc);
        check(zdt.toLocalDateTime().equals(LocalDateTime.of(2020, 1, 1, 12, 0)), "toLocalDateTime");
        check(zdt.toLocalDate().equals(LocalDate.of(2020, 1, 1)), "toLocalDate");
        check(zdt.toLocalTime().equals(LocalTime.of(12, 0)), "toLocalTime");
        check(zdt.getZone().getId().equals("UTC"), "getZone");
        check(zdt.getOffset().equals(ZoneOffset.UTC), "getOffset");
        check(zdt.getChronology() == IsoChronology.INSTANCE, "getChronology");

        // Field access.
        check(zdt.isSupported(ChronoField.YEAR), "isSupported YEAR");
        check(zdt.isSupported(ChronoField.INSTANT_SECONDS), "isSupported INSTANT_SECONDS");
        check(zdt.get(ChronoField.YEAR) == 2020, "get YEAR");
        check(zdt.get(ChronoField.OFFSET_SECONDS) == 0, "get OFFSET_SECONDS");
        check(zdt.getLong(ChronoField.DAY_OF_MONTH) == 1L, "getLong DAY_OF_MONTH");
        check(zdt.range(ChronoField.DAY_OF_MONTH).getMaximum() == 31, "range DAY_OF_MONTH max 31");

        // plus / minus / with.
        check(zdt.plus(1, ChronoUnit.DAYS).toLocalDate().equals(LocalDate.of(2020, 1, 2)), "plus day");
        check(zdt.minus(1, ChronoUnit.HOURS).toLocalTime().equals(LocalTime.of(11, 0)), "minus hour");
        check(zdt.with(ChronoField.YEAR, 2021).toLocalDate().equals(LocalDate.of(2021, 1, 1)), "with YEAR");

        // toInstant / toEpochSecond.
        Instant instant = Instant.parse("2020-01-01T12:00:00Z");
        check(zdt.toInstant().equals(instant), "toInstant");
        check(zdt.toEpochSecond() == instant.getEpochSecond(), "toEpochSecond");

        // withZoneSameInstant / withZoneSameLocal (Asia/Shanghai is UTC+8).
        ZoneId shanghai = ZoneId.of("Asia/Shanghai");
        ChronoZonedDateTime<?> sameInstant = zdt.withZoneSameInstant(shanghai);
        check(sameInstant.getOffset().getTotalSeconds() == 8 * 3600, "withZoneSameInstant offset +08:00");
        check(sameInstant.toInstant().equals(instant), "withZoneSameInstant keeps instant");
        check(sameInstant.toLocalTime().equals(LocalTime.of(20, 0)), "withZoneSameInstant local 20:00");
        ChronoZonedDateTime<?> sameLocal = zdt.withZoneSameLocal(shanghai);
        check(sameLocal.toLocalTime().equals(LocalTime.of(12, 0)), "withZoneSameLocal keeps 12:00");
        check(sameLocal.getOffset().getTotalSeconds() == 8 * 3600, "withZoneSameLocal offset +08:00");

        // withEarlierOffsetAtOverlap / withLaterOffsetAtOverlap on UTC (no overlap).
        check(zdt.withEarlierOffsetAtOverlap().equals(zdt), "withEarlierOffsetAtOverlap UTC no-op");
        check(zdt.withLaterOffsetAtOverlap().equals(zdt), "withLaterOffsetAtOverlap UTC no-op");

        // Ordering.
        check(zdt.isBefore(ZonedDateTime.of(2020, 1, 1, 13, 0, 0, 0, utc)), "isBefore");
        check(zdt.isAfter(ZonedDateTime.of(2020, 1, 1, 11, 0, 0, 0, utc)), "isAfter");
        check(zdt.isEqual(ZonedDateTime.of(2020, 1, 1, 12, 0, 0, 0, utc)), "isEqual");
        check(zdt.compareTo(ZonedDateTime.of(2020, 1, 1, 12, 0, 0, 1, utc)) < 0, "compareTo < 0");

        // timeLineOrder sorts by instant.
        List<ChronoZonedDateTime<?>> list = new ArrayList<ChronoZonedDateTime<?>>();
        list.add(ZonedDateTime.of(2020, 1, 2, 0, 0, 0, 0, utc));
        list.add(ZonedDateTime.of(2020, 1, 1, 12, 0, 0, 0, utc));
        list.add(ZonedDateTime.of(2019, 12, 31, 23, 59, 0, 0, utc));
        Collections.sort(list, ChronoZonedDateTime.timeLineOrder());
        check(list.get(0).equals(ZonedDateTime.of(2019, 12, 31, 23, 59, 0, 0, utc))
              && list.get(2).equals(ZonedDateTime.of(2020, 1, 2, 0, 0, 0, 0, utc)), "timeLineOrder sort");

        // format.
        check(zdt.format(DateTimeFormatter.ISO_ZONED_DATE_TIME).equals("2020-01-01T12:00:00Z[UTC]"), "format");

        // Exception path: from() on a temporal without a zone throws.
        expectThrows(java.time.DateTimeException.class,
              () -> ChronoZonedDateTime.from(LocalDate.of(2020, 1, 1)),
              "from LocalDate throws DateTimeException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
