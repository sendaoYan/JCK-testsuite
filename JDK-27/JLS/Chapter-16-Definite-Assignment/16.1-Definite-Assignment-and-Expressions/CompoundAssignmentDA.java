/*
 * @test
 * @summary JLS 16.1.7: a compound assignment expression a op= b definitely
 *          assigns its left-hand variable (and requires it to be assigned before,
 *          because the old value is read).
 * @run main CompoundAssignmentDA
 */

public class CompoundAssignmentDA {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        int x = 3;
        x &= 1;          // 3 & 1 == 1
        check(x == 1, "x &= 1");
        x |= 4;          // 1 | 4 == 5
        check(x == 5, "x |= 4");
        x ^= 5;          // 5 ^ 5 == 0
        check(x == 0, "x ^= 5");
        x += 2;          // 0 + 2 == 2
        check(x == 2, "x += 2");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
