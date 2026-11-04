/*
 * @test
 * @summary JLS 6.6.2.2 (Java SE 8): a protected constructor is accessible to a
 *          subclass in another package via a superclass constructor invocation
 *          super(...).
 * @compile Base.java Sub.java Main.java
 * @run main pcv2.Main
 */

package pcv2;

public class Main {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    public static void main(String[] args) {
        Sub s = new Sub(7);
        check(s.v == 7, "protected constructor accessible via super(...)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
