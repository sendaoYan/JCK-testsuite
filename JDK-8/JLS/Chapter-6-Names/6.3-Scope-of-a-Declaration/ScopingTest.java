/*
 * @test
 * @summary JLS 6.3 (Java SE 8): a declaration's scope extends over the block it
 *          is declared in, and a local variable is in scope from its declarator
 *          to the end of that block; a for-loop variable is scoped to the loop.
 * @run main ScopingTest
 */

public class ScopingTest {
    static int x = 1;   // field x

    static int f() {
        int x = 2;      // local variable shadows the field within this method
        return x;
    }

    static int g(int p) { return p; }

    public static void main(String[] args) {
        if (x != 1) throw new AssertionError("field in scope");
        if (f() != 2) throw new AssertionError("local shadows field");
        if (g(10) != 10) throw new AssertionError("parameter scope");

        {
            int y = 5;
            if (y != 5) throw new AssertionError("block scope");
        }

        for (int i = 0; i < 3; i++) {
            if (i < 0) throw new AssertionError("loop variable scope");
        }

        System.out.println("PASS");
    }
}
