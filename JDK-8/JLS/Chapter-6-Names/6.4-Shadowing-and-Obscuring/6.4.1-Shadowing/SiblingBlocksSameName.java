/*
 * @test
 * @summary JLS 6.4.1 (Java SE 8): two local variables with the same name may be
 *          declared in sibling blocks because their scopes do not overlap.
 * @run main SiblingBlocksSameName
 */

public class SiblingBlocksSameName {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    public static void main(String[] args) {
        {
            int x = 1;
            check(x == 1, "first sibling block");
        }
        {
            int x = 2;
            check(x == 2, "second sibling block reuses the name");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
