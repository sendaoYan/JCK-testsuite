/*
 * @test
 * @summary JLS 5.1.6.2: a narrowing reference conversion is either checked or
 *          unchecked. An unchecked conversion (to a parameterized type or type
 *          variable) cannot be fully validated at run time; a checked
 *          conversion can. Completely unchecked (upcast) needs no run-time
 *          check; partially unchecked (downcast) does.
 * @run main CheckedAndUncheckedNarrowingReferenceConversions
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CheckedAndUncheckedNarrowingReferenceConversions {
    // Completely unchecked: raw ArrayList -> Collection<String> is an upcast at
    // the raw level (|ArrayList| <: |Collection|), so no run-time test needed.
    @SuppressWarnings("unchecked")
    static Collection<String> upcast() {
        ArrayList raw = new ArrayList();
        raw.add("x");
        return (Collection<String>) (Collection) raw;   // completely unchecked
    }

    // Partially unchecked: raw Collection -> ArrayList<String> is a downcast at
    // the raw level (|Collection| is not <: |ArrayList|), so a check is needed.
    @SuppressWarnings("unchecked")
    static ArrayList<String> downcast(Collection raw) {
        return (ArrayList<String>) raw;                  // partially unchecked
    }

    public static void main(String[] args) {
        Collection<String> c = upcast();
        if (c.size() != 1 || !c.iterator().next().equals("x")) {
            throw new AssertionError("completely unchecked upcast");
        }

        ArrayList<String> ok = downcast(new ArrayList());
        ok.add("y");
        if (!ok.get(0).equals("y")) throw new AssertionError("partially unchecked downcast");

        // A checked narrowing conversion (non-parameterized types) is fully
        // validated at run time (see 5.1.6.3).
        Object o = "str";
        String s = (String) o;
        if (!s.equals("str")) throw new AssertionError("checked narrowing conversion");

        System.out.println("PASS");
    }
}
