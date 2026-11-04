/*
 * @test
 * @summary java.time.chrono.AbstractChronology conformance: resolveDate and compareTo.
 * @run main AbstractChronologyTest
 */

import java.time.LocalDate;
import java.time.chrono.AbstractChronology;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.IsoChronology;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.HashMap;
import java.util.Map;

public class AbstractChronologyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAbstractChronology() {
        AbstractChronology ac = IsoChronology.INSTANCE;
        check(IsoChronology.INSTANCE instanceof AbstractChronology, "extends AbstractChronology");
        check(ac.compareTo(IsoChronology.INSTANCE) == 0, "compareTo self");

        Map<TemporalField, Long> map = new HashMap<TemporalField, Long>();
        map.put(ChronoField.YEAR, 2020L);
        map.put(ChronoField.MONTH_OF_YEAR, 1L);
        map.put(ChronoField.DAY_OF_MONTH, 1L);
        ChronoLocalDate resolved = ac.resolveDate(map, ResolverStyle.STRICT);
        check(resolved.equals(LocalDate.of(2020, 1, 1)), "resolveDate");
    }

    public static void main(String[] args) {
        testAbstractChronology();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
