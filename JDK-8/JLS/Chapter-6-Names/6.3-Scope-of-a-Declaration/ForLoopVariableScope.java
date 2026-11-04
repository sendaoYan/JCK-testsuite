/*
 * @test
 * @summary JLS 6.3 (Java SE 8): the scope of a for-loop variable is the for
 *          statement, including its condition, update, and body; sibling loops
 *          may reuse the same variable name.
 * @run main ForLoopVariableScope
 */

public class ForLoopVariableScope {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    public static void main(String[] args) {
        int sum = 0;
        for (int i = 0; i < 3; i++) {
            sum += i;
        }
        check(sum == 3, "loop variable usable in body");

        int prod = 1;
        for (int i = 1; i <= 3; i++) {   // same name reused in a sibling loop
            prod *= i;
        }
        check(prod == 6, "sibling loop reuses the name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
