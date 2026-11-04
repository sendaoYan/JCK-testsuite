/*
 * @test
 * @summary JLS 6.3 (Java SE 27): the scope of a local class is the rest of the
 *          block in which it is declared, starting with its own declaration.
 * @run main LocalClassScope
 */

public class LocalClassScope {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    public static void main(String[] args) {
        class L { int v() { return 9; } }
        L l = new L();
        check(l.v() == 9, "local class usable after its declaration");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
