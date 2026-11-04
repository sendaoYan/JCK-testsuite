/*
 * @test
 * @summary JLS 6.4.2 / 6.5.2 (Java SE 27): when a simple name could be either a
 *          variable or a type, the variable is chosen, obscuring the type; the
 *          type remains reachable by its fully qualified name.
 * @compile Thing.java Main.java
 * @run main app.Main
 */

package app;

import obscuring.Thing;

public class Main {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    static int Thing = 5;   // field obscures the imported type Thing

    public static void main(String[] args) {
        check(Thing == 5, "simple name resolves to the variable, not the imported type");
        check(obscuring.Thing.k == 9, "imported type reachable by its fully qualified name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
