/*
 * @test
 * @summary JLS 4.10.5: upward and downward projections compute close supertypes
 *          and subtypes that do not mention restricted (synthetic) type
 *          variables, such as those introduced by capture conversion.
 *          Observable via capture of a wildcard and type inference.
 * @run main TypeProjections
 */

import java.util.List;
import java.util.ArrayList;

public class TypeProjections {
    // m accepts a List<T> for any T; calling it with List<? extends Number>
    // captures the wildcard to a synthetic type variable (upward projected).
    static <T> T first(List<T> list) {
        return list.get(0);
    }

    public static void main(String[] args) {
        List<? extends Number> ln = new ArrayList<Integer>();
        ((ArrayList<Integer>) ln).add(42);

        // Capture conversion of ? extends Number yields a synthetic type variable
        // whose upward projection is Number; first() returns a value assignable
        // to Number without mentioning the synthetic variable.
        Number n = first(ln);
        if (n.intValue() != 42) throw new AssertionError("capture + upward projection");

        // A wildcard whose bound does not mention a restricted variable is unchanged.
        List<String> ls = new ArrayList<String>();
        ls.add("x");
        String s = first(ls);
        if (!s.equals("x")) throw new AssertionError("projection leaves concrete types unchanged");

        System.out.println("PASS");
    }
}
