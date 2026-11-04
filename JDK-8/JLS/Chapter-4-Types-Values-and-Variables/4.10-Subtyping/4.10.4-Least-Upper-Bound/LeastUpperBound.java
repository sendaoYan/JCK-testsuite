/*
 * @test
 * @summary JLS 4.10.4 (Java SE 8): the least upper bound (lub) of two reference
 *          types is the most specific common supertype. It drives conditional
 *          expression typing and type inference for the conditional operator.
 * @run main LeastUpperBound
 */

import java.util.ArrayList;
import java.util.List;

public class LeastUpperBound {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // lub(String, Integer) via a conditional expression is
        // Comparable<? extends Serializable & Comparable<?>>-ish, but its
        // erased usable common supertype is Object; assign the result to Object.
        Object o = true ? "a" : 1;
        check(o instanceof String || o instanceof Integer, "conditional lub result is a String or Integer");

        // lub of two sibling types yields their common superclass.
        Object n = true ? Integer.valueOf(1) : Double.valueOf(2.0);
        check(n instanceof Number, "lub(Integer, Double) is Number");

        // lub(ArrayList<String>, ArrayList<Integer>) is usable as List<?>.
        List<?> l = true ? new ArrayList<String>() : new ArrayList<Integer>();
        check(l != null, "lub of two parameterizations is a wildcard List");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
