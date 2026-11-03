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
        System.out.println("PASS (" + passed + " assertions)");
    }

    // tiny holder so the test stays dependency-free of TemporalQueries import needs
    static final class TemporalQueriesShim {
        static TemporalQuery<LocalDate> localDate() { return java.time.temporal.TemporalQueries.localDate(); }
    }
}
