/*
 * @test
 * @summary java.time.chrono.IsoEra conformance: BCE/CE values.
 * @run main IsoEraTest
 */

import java.time.chrono.IsoEra;

public class IsoEraTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIsoEra() {
        check(IsoEra.BCE.getValue() == 0, "BCE value");
        check(IsoEra.CE.getValue() == 1, "CE value");
        check(IsoEra.valueOf("CE") == IsoEra.CE, "valueOf CE");
        check(IsoEra.valueOf("BCE") == IsoEra.BCE, "valueOf BCE");
        check(IsoEra.values().length == 2, "two values");
        check(IsoEra.of(1) == IsoEra.CE, "of(1) CE");
        check(IsoEra.of(0) == IsoEra.BCE, "of(0) BCE");
    }

    public static void main(String[] args) {
        testIsoEra();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
