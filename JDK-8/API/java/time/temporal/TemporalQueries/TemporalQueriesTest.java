/*
 * @test
 * @summary java.time.temporal.TemporalQueries conformance: static query factories.
 * @run main TemporalQueriesTest
 */

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;

public class TemporalQueriesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTemporalQueries() {
        check(TemporalQueries.zoneId() != null, "zoneId");
        check(TemporalQueries.chronology() != null, "chronology");
        check(TemporalQueries.precision() != null, "precision");
        check(TemporalQueries.zone() != null, "zone");
        check(TemporalQueries.offset() != null, "offset");
        check(TemporalQueries.localDate() != null, "localDate");
        check(TemporalQueries.localTime() != null, "localTime");

        TemporalQuery<LocalDate> q = TemporalQueries.localDate();
        check(q.queryFrom(LocalDate.of(2020, 1, 1)).equals(LocalDate.of(2020, 1, 1)),
              "localDate from LocalDate");
        check(q.queryFrom(LocalDateTime.of(2020, 1, 1, 12, 0)).equals(LocalDate.of(2020, 1, 1)),
              "localDate from LocalDateTime");

        TemporalQuery<LocalTime> tq = TemporalQueries.localTime();
        check(tq.queryFrom(LocalDateTime.of(2020, 1, 1, 12, 30)).equals(LocalTime.of(12, 30)),
              "localTime from LocalDateTime");

        check(TemporalQueries.offset().queryFrom(ZoneOffset.ofHours(2)) == ZoneOffset.ofHours(2),
              "offset from ZoneOffset");
    }

    public static void main(String[] args) {
        testTemporalQueries();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
