/*
 * @test
 * @summary JLS 6.4.1 (Java SE 8): a local variable shadows an enum constant;
 *          the constant remains reachable by its qualified name.
 * @run main LocalShadowsEnumConstant
 */

public class LocalShadowsEnumConstant {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    enum Color { RED, GREEN }

    public static void main(String[] args) {
        int RED = 1;                        // local shadows the enum constant
        check(RED == 1, "local shadows enum constant");
        check(Color.RED == Color.RED && Color.GREEN.ordinal() == 1, "constant reachable by qualified name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
