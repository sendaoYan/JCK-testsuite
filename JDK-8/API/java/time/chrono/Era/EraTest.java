/*
 * @test
 * @summary java.time.chrono.Era conformance: value/support/get via IsoEra.
 * @run main EraTest
 */

import java.time.LocalDate;
import java.time.chrono.Era;
import java.time.chrono.IsoEra;
import java.time.temporal.ChronoField;

public class EraTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testEra() {
        Era era = IsoEra.CE;
        check(era.getValue() == 1, "CE value");
        check(era.isSupported(ChronoField.ERA), "isSupported ERA");
        check(era.get(ChronoField.ERA) == 1, "get ERA");
        check(era.range(ChronoField.ERA).getMaximum() == 1, "range ERA max");
        check(era.adjustInto(LocalDate.of(2019, 1, 1)).equals(LocalDate.of(2019, 1, 1)),
              "CE adjustInto no-op");
    }

    public static void main(String[] args) {
        testEra();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
