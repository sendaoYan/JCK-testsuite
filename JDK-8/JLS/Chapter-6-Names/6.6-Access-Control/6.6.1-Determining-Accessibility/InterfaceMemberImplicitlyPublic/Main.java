/*
 * @test
 * @summary JLS 6.6.1 / 9.3 / 9.4 (Java SE 8): members of an interface are
 *          implicitly public, so a field and an abstract method are accessible
 *          from another package without an explicit public modifier.
 * @compile I.java Impl.java Main.java
 * @run main ifc.Main
 */

package ifc;

import ifp.I;

public class Main {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    public static void main(String[] args) {
        check(I.K == 5, "interface field is implicitly public");
        I i = new Impl();
        check(i.m() == 6, "interface method is implicitly public");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
