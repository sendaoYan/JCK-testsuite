/*
 * @test
 * @summary java.time.temporal.TemporalQuery conformance: white-box structure + black-box behavior.
 * @run main TemporalQueryTest
 */

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.temporal.TemporalQueries;

public class TemporalQueryTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.temporal.TemporalQuery");
        check(c.isInterface(), "is an interface");
        check(Modifier.isInterface(c.getModifiers()), "modifier is interface");
        check(c.getTypeParameters().length == 1, "single type parameter");
        java.lang.reflect.Method m = c.getMethod("queryFrom", TemporalAccessor.class);
        check(m.getReturnType() == Object.class, "queryFrom returns the type variable (erased Object)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // method reference as a TemporalQuery
        TemporalQuery<LocalDate> q = LocalDate::from;
        check(q.queryFrom(LocalDate.of(2024, 3, 1)).equals(LocalDate.of(2024, 3, 1)), "method-reference queryFrom");

        // lambda extracting the year as a boxed Long
        TemporalQuery<Long> year = ta -> ta.getLong(ChronoField.YEAR);
        check(year.queryFrom(LocalDate.of(2024, 3, 1)) == 2024L, "lambda query year");

        // lambda returning null for a time-only accessor
        TemporalQuery<LocalDate> maybeDate = TemporalQueriesShim.localDate();
        check(maybeDate.queryFrom(java.time.LocalTime.of(10, 0)) == null, "localDate query returns null for LocalTime");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepTemporalQuery();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // tiny holder so the test stays dependency-free of TemporalQueries import needs
    static final class TemporalQueriesShim {
        static TemporalQuery<LocalDate> localDate() { return java.time.temporal.TemporalQueries.localDate(); }
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
