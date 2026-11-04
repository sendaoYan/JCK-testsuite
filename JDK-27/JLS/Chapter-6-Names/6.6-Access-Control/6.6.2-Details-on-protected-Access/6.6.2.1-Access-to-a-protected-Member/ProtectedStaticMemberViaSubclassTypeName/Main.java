/*
 * @test
 * @summary JLS 6.6.2.1 (Java SE 27): a protected static member is accessible
 *          within the body of a subclass in another package, via a subclass
 *          type name (and via this subclass's own type name).
 * @compile Base.java Sub.java Main.java
 * @run main pst2.Main
 */

package pst2;

import pst1.Base;

public class Main extends Base {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    public static void main(String[] args) {
        check(Main.k == 5, "protected static member via this subclass type name");
        check(Sub.k == 5, "protected static member via sibling subclass type name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
