/*
 * @test
 * @summary JLS 4.11: types are used in 17 type contexts (in declarations and
 *          expressions). This test exercises a representative declaration and
 *          expression context from the list, plus the restriction that
 *          instanceof and array creation require reifiable types.
 * @run main WhereTypesAreUsed
 */

import java.util.ArrayList;
import java.util.List;

class Base {}
interface Op { int apply(int x); }

// Types used in declaration contexts: extends clause (1), return type (3),
// field type (6), formal parameter (7), local variable (9).
class ContextDemo extends Base {
    // (6) field type
    int divisor;

    // (7) formal parameter; (3) return type
    int divide(int v) {
        // (9) local variable type
        int r = v / divisor;
        return r;
    }
}

public class WhereTypesAreUsed {
    public static void main(String[] args) {
        // (13) class type to be instantiated in a class instance creation expression.
        ContextDemo w = new ContextDemo();
        w.divisor = 2;
        if (w.divide(10) != 5) throw new AssertionError("declaration contexts");

        // (14) element type in an array creation expression (reifiable required).
        String[] arr = new String[2];

        // (15) cast expression.
        Object o = "s";
        String s = (String) o;

        // (16) instanceof type comparison operator (reifiable required).
        if (!(o instanceof String)) throw new AssertionError("instanceof context");

        // (17) method reference: reference type to search for a member method.
        Op op = WhereTypesAreUsed::twice;
        if (op.apply(21) != 42) throw new AssertionError("method reference context");

        // (12) explicit type argument list to a method invocation.
        List<String> l = new ArrayList<String>();
        l.add("a");
        if (l.size() != 1) throw new AssertionError("explicit type argument");

        System.out.println("PASS");
    }

    static int twice(int x) { return x * 2; }
}
