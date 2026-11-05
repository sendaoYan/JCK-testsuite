/*
 * @test
 * @summary jdk.dynalink.linker.ConversionComparator.Comparison conformance: enum constants.
 * @run main ComparisonTest
 */

import jdk.dynalink.linker.ConversionComparator.Comparison;
import java.util.*;

public class ComparisonTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testDeepContract() throws Throwable {
        List<Comparison> vals = List.of(Comparison.values());
        check(vals.equals(List.of(Comparison.INDETERMINATE,
                                  Comparison.TYPE_1_BETTER,
                                  Comparison.TYPE_2_BETTER)),
              "values in declaration order: " + vals);
        check(Comparison.valueOf("TYPE_1_BETTER") == Comparison.TYPE_1_BETTER, "valueOf");
        for (Comparison c : Comparison.values()) {
            check(c.toString().equals(c.name()), "toString is the name: " + c);
            check(c instanceof Enum, "enum constant");
        }
        check(Comparison.class.getDeclaringClass().getName().equals("jdk.dynalink.linker.ConversionComparator"),
              "nested in ConversionComparator");
        check(Comparison.class.isEnum(), "is an enum");
    }

    public static void main(String[] args) throws Throwable {
        check(Comparison.INDETERMINATE != null, "INDETERMINATE");
        check(Comparison.TYPE_1_BETTER != null, "TYPE_1_BETTER");
        check(Comparison.TYPE_2_BETTER != null, "TYPE_2_BETTER");
        check(Comparison.values().length == 3, "3 constants");
        check(Comparison.valueOf("INDETERMINATE") == Comparison.INDETERMINATE, "valueOf");
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
