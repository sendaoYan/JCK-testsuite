/*
 * @test
 * @summary JLS 18.5.2: since Java 9 the diamond operator may be used with an
 *          anonymous class, inferring the type arguments from the target type.
 * @run main DiamondAnonymousClass
 */

import java.util.ArrayList;
import java.util.List;

public class DiamondAnonymousClass {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        List<String> l = new ArrayList<>() {   // diamond with anonymous class (Java 9+)
            { add("a"); }
        };
        check("a".equals(l.get(0)), "diamond infers String for an anonymous class");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
