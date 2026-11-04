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
import java.time.DateTimeException;
import java.time.chrono.JapaneseChronology;
import java.time.chrono.JapaneseDate;
import java.time.temporal.TemporalAccessor;

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
        testDeepChronoLocalDateTime();
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


    static void testDeepChronoLocalDateTime() {
        ChronoLocalDateTime<?> c = ChronoLocalDateTime.from(java.time.LocalDateTime.of(2024, 6, 15, 10, 0));
        xeq(c.toString(), "2024-06-15T10:00", "from LocalDateTime");
        xeq(c.atZone(java.time.ZoneId.of("UTC")).toString(), "2024-06-15T10:00Z[UTC]", "atZone");
        xeq(c.toInstant(java.time.ZoneOffset.UTC).toString(), "2024-06-15T10:00:00Z", "toInstant");
        check(c.toEpochSecond(java.time.ZoneOffset.UTC) == 1718445600L, "toEpochSecond");
        check(c.toLocalDate().equals(java.time.LocalDate.of(2024, 6, 15)), "toLocalDate");
        check(c.toLocalTime().equals(java.time.LocalTime.of(10, 0)), "toLocalTime");
        ChronoLocalDateTime<?> jp = JapaneseDate.of(2024, 6, 15).atTime(java.time.LocalTime.NOON);
        xeq(jp.toString(), "Japanese Reiwa 6-06-15T12:00", "jp atTime");
        check(ChronoLocalDateTime.timeLineOrder().compare(jp, c) > 0, "timeLine NOON after 10:00");
        check(ChronoLocalDateTime.timeLineOrder().compare(jp, JapaneseDate.of(2024, 6, 15).atTime(java.time.LocalTime.NOON)) == 0,
            "timeLine equal");
        xact(() -> ChronoLocalDateTime.from(java.time.LocalDate.of(2024, 1, 1)), java.time.DateTimeException.class,
            "Unable to obtain LocalDateTime from TemporalAccessor: 2024-01-01 of type java.time.LocalDate", "from LocalDate");
        xact(() -> ChronoLocalDateTime.from(null), NullPointerException.class, "temporal", "from null");
        check(jp.getChronology().equals(JapaneseChronology.INSTANCE), "jp CLDT chronology");
        check(jp.isAfter(c), "isAfter cross-chrono");
        check(jp.isEqual(JapaneseDate.of(2024, 6, 15).atTime(java.time.LocalTime.NOON)), "isEqual");
    }

}
