/*
 * @test
 * @summary JLS 6.2 (Java SE 8): "_" is a legal identifier in Java SE 8 (using it
 *          is only a warning; it becomes a keyword in Java SE 9).
 * @run main UnderscoreIdentifier
 */

public class UnderscoreIdentifier {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    public static void main(String[] args) {
        int _ = 1;      // legal identifier in Java SE 8
        _ = _ + 1;
        check(_ == 2, "_ is a usable identifier in Java SE 8");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
