/*
 * @test
 * @summary JLS 10.6: if the component type is an array type, a variable
 *          initializer may itself be an array initializer (nested initializers);
 *          the nested initializer constructs and initializes a component array.
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

        // Deeper nesting.
        int[][][] deep = { { { 1 }, { 2, 3 } }, { { 4 } } };
        check(deep.length == 2 && deep[0].length == 2 && deep[0][1].length == 2 && deep[0][1][1] == 3,
              "deeply nested initializer");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
