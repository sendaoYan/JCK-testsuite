/*
 * @test
 * @summary java.time.temporal.TemporalAccessor conformance: white-box structure + black-box behavior.
 * @run main TemporalAccessorTest
 */

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;

public class TemporalAccessorTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.temporal.TemporalAccessor");
        check(c.isInterface(), "is an interface");
        check(Modifier.isInterface(c.getModifiers()), "modifier is interface");
        check(c.getInterfaces().length == 0, "has no superinterfaces");
        check(c.getMethod("getLong", java.time.temporal.TemporalField.class).getReturnType() == long.class, "getLong returns long");
        check(c.getMethod("get", java.time.temporal.TemporalField.class).getReturnType() == int.class, "get returns int");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        TemporalAccessor date = LocalDate.of(2024, 6, 15);
        check(date instanceof LocalDate, "LocalDate is a TemporalAccessor");

        // support
        check(date.isSupported(ChronoField.DAY_OF_MONTH), "supports DAY_OF_MONTH");
        check(date.isSupported(ChronoField.YEAR), "supports YEAR");
        check(!date.isSupported(ChronoField.MINUTE_OF_HOUR), "does not support MINUTE_OF_HOUR");

        // getLong / get
        check(date.getLong(ChronoField.YEAR) == 2024, "getLong YEAR");
        check(date.get(ChronoField.DAY_OF_MONTH) == 15, "get DAY_OF_MONTH");

        // range on a concrete date (June 2024 has 30 days)
        check(date.range(ChronoField.DAY_OF_MONTH).getMinimum() == 1, "range DAY_OF_MONTH min 1");
        check(date.range(ChronoField.DAY_OF_MONTH).getMaximum() == 30, "range DAY_OF_MONTH max 30 for June");

        // query via concrete query
        check(date.query(TemporalQueries.localDate()).equals(LocalDate.of(2024, 6, 15)), "query localDate");

        // a time accessor
        TemporalAccessor time = LocalTime.of(10, 30);
        check(time.get(ChronoField.MINUTE_OF_HOUR) == 30, "LocalTime get MINUTE_OF_HOUR");
        check(!time.isSupported(ChronoField.YEAR), "LocalTime does not support YEAR");
        check(time.query(TemporalQueries.precision()) == ChronoUnit.NANOS, "LocalTime precision NANOS");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
