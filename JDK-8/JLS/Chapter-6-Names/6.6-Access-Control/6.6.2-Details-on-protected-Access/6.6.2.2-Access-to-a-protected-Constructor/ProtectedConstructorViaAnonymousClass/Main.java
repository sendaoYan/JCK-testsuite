/*
 * @test
 * @summary JLS 6.6.2.2 (Java SE 8): a protected constructor is accessible via an
 *          anonymous class instance creation expression in a subclass context.
 * @compile Base.java Main.java
 * @run main pca2.Main
 */

package pca2;

import pca1.Base;

public class Main {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    public static void main(String[] args) {
        Base b = new Base() { };   // anonymous subclass invokes the protected constructor
        check(b.v() == 8, "protected constructor via anonymous class");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
