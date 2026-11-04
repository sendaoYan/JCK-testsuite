/*
 * @test
 * @summary JVMS 3.2: the compiler folds constant expressions (arithmetic and
 *          string concatenation of compile-time constants) so a folded int
 *          constant is usable as a switch case label and a folded string is
 *          the same interned literal.
 * @run main ConstantFolding
 */

public class ConstantFolding {
    static int passed = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final int A = 2;
    static final int B = A * 3;          // folded to 6 at compile time
    static final String S = "ab" + "c";  // folded to "abc" at compile time

    static int classify(int x) {
        switch (x) {
            case B: return 100;   // legal only because B is a constant expression
            default: return -1;
        }
    }

    public static void main(String[] args) {
        check(B == 6, "int constant expression is folded");
        check(classify(6) == 100, "folded constant usable as a case label");
        check(S == "abc", "string constant is folded and interned");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
