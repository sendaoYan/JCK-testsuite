/*
 * @test
 * @summary JLS 16.2.4: a local variable declared with an initializer is definitely
 *          assigned after its declaration; each declarator in a multi-variable
 *          declaration is assigned by its own initializer.
 * @run main LocalVarDeclarationDA
 */

public class LocalVarDeclarationDA {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        int x = 1;
        check(x == 1, "initialized declaration");

        int a = 1, b = 2, c = 3;
        check(a + b + c == 6, "multiple declarators each assigned");

        int d;
        d = 42;
        check(d == 42, "assigned after a no-initializer declaration");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
