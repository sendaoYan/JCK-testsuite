/*
 * @test
 * @summary java.time.temporal.TemporalQuery conformance: queryFrom.
 * @run main TemporalQueryTest
 */

import java.time.LocalDate;
import java.time.temporal.TemporalQuery;

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
