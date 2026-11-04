/*
 * @test
 * @summary java.time.temporal.TemporalQuery conformance: queryFrom.
 * @run main TemporalQueryTest
 */

import java.time.LocalDate;
import java.time.temporal.TemporalQuery;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalQueries;

public class TemporalQueryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTemporalQuery() {
        TemporalQuery<LocalDate> q = LocalDate::from;
        check(q.queryFrom(LocalDate.of(2020, 1, 1)).equals(LocalDate.of(2020, 1, 1)), "queryFrom");
    }

    public static void main(String[] args) {
        testTemporalQuery();
        testDeepTemporalQuery();
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


    static void testDeepTemporalQuery() {
        TemporalQuery<Integer> len = t -> t.get(ChronoField.DAY_OF_YEAR);
        check(len.queryFrom(LocalDate.of(2024, 6, 1)) == 153, "custom doy query");
        check(LocalDate.of(2024, 6, 1).query(len) == 153, "query via accessor");
        TemporalQuery<Boolean> isNoon = t -> t.get(ChronoField.HOUR_OF_DAY) == 12;
        check(LocalTime.NOON.query(isNoon), "isNoon true");
        check(LocalTime.MIDNIGHT.query(isNoon) == false, "isNoon false");
        TemporalQuery<String> name = t -> t.getClass().getSimpleName();
        check(name.queryFrom(LocalDate.of(2024, 1, 1)).equals("LocalDate"), "class name query");
        TemporalQuery<Object> throwing = t -> { throw new DateTimeException("boom"); };
        xact(() -> throwing.queryFrom(LocalDate.of(2024, 1, 1)), DateTimeException.class, "boom", "query may throw DTE");
        check(TemporalQueries.localDate() instanceof TemporalQuery, "predefined is query");
        check(TemporalQueries.zoneId() instanceof TemporalQuery, "zoneId is query");
        check(TemporalQueries.offset() instanceof TemporalQuery, "offset is query");
    }

}
