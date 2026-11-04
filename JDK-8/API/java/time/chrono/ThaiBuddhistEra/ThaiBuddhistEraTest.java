/*
 * @test
 * @summary java.time.chrono.ThaiBuddhistEra conformance: BEFORE_BE/BE values.
 * @run main ThaiBuddhistEraTest
 */

import java.time.chrono.ThaiBuddhistEra;

public class ThaiBuddhistEraTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testThaiBuddhistEra() {
        check(ThaiBuddhistEra.BEFORE_BE.getValue() == 0, "BEFORE_BE value");
        check(ThaiBuddhistEra.BE.getValue() == 1, "BE value");
        check(ThaiBuddhistEra.valueOf("BE") == ThaiBuddhistEra.BE, "valueOf BE");
        check(ThaiBuddhistEra.values().length == 2, "two values");
        check(ThaiBuddhistEra.of(1) == ThaiBuddhistEra.BE, "of(1) BE");
    }

    public static void main(String[] args) {
        testThaiBuddhistEra();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
