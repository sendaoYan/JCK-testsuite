/*
 * @test
 * @summary java.util.stream.Collector.Characteristics enum conformance.
 * @run main CharacteristicsTest
 */

import java.util.stream.Collector;

public class CharacteristicsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCharacteristics() {
        check(Collector.Characteristics.values().length == 3, "3 constants");
        check(Collector.Characteristics.valueOf("CONCURRENT") == Collector.Characteristics.CONCURRENT,
              "valueOf");
        check(Collector.Characteristics.UNORDERED != Collector.Characteristics.IDENTITY_FINISH,
              "distinct constants");
    }

    public static void main(String[] args) {
        testCharacteristics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
