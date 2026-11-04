/*
 * @test
 * @summary JLS 10.6 (Java SE 8): array initializers may be nested when the
 *          component type is itself an array type.
 * @run main ArrayInitializerNested
 */

public class ArrayInitializerNested {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        int[][] ia = { { 1, 2 }, null };
        check(ia.length == 2, "nested initializer outer length");
        check(ia[0].length == 2 && ia[0][1] == 2, "nested initializer component");
        check(ia[1] == null, "null component allowed");

        int[][][] deep = { { { 1 }, { 2, 3 } }, { { 4 } } };
        check(deep.length == 2 && deep[0][1][1] == 3, "deeply nested initializer");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
