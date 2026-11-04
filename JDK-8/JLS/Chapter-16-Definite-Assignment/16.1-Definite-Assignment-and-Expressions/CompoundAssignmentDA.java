/*
 * @test
 * @summary JLS 16.1.7 (Java SE 8): a compound assignment a op= b definitely
 *          assigns its left-hand variable.
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
        x &= 1;
        check(x == 1, "x &= 1");
        x |= 4;
        check(x == 5, "x |= 4");
        x ^= 5;
        check(x == 0, "x ^= 5");
        x += 2;
        check(x == 2, "x += 2");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
