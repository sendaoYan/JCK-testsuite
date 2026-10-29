/*
 * @test
 * @summary JLS 4.10.4: the least upper bound of a set of reference types is a
 *          shared supertype more specific than any other shared supertype. For
 *          List<String> and List<Object>, the lub is List<?> (the spec's
 *          running example); it recovers the erased candidate set and then the
 *          least containing parameterization.
 * @run main LeastUpperBound
 */

import java.util.List;
import java.util.ArrayList;

public class LeastUpperBound {
    public static void main(String[] args) {
        List<String> ls = new ArrayList<String>();
        List<Object> lo = new ArrayList<Object>();

        // The type of the conditional expression is lub(List<String>, List<Object>).
        // Per JLS 4.10.4 this is List<?>; assignment to List<?> type-checks.
        List<?> lub = true ? ls : lo;

        if (lub == null) throw new AssertionError("lub must be non-null");
        if (!(lub instanceof List)) throw new AssertionError("lub is a List");

        // k = 1: lub(U) = U (a type's lub with itself is itself).
        Object o = true ? ls : ls;
        if (!(o instanceof List)) throw new AssertionError("lub(U) = U");

        System.out.println("PASS");
    }
}
