/*
 * @test
 * @summary jdk.dynalink.linker.ConversionComparator.Comparison conformance: enum constants.
 * @run main ComparisonTest
 */

import jdk.dynalink.linker.ConversionComparator.Comparison;

public class ComparisonTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check(Comparison.INDETERMINATE != null, "INDETERMINATE");
        check(Comparison.TYPE_1_BETTER != null, "TYPE_1_BETTER");
        check(Comparison.TYPE_2_BETTER != null, "TYPE_2_BETTER");
        check(Comparison.values().length == 3, "3 constants");
        check(Comparison.valueOf("INDETERMINATE") == Comparison.INDETERMINATE, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
