/*
 * @test
 * @summary JLS 14.13: a do statement executes its body at least once and tests the
 *          condition after each execution.
 * @run main DoAtLeastOnce
 */

public class DoAtLeastOnce {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    public static void main(String[] args) {
        int n = 0;
        do {
            n++;
        } while (false);
        check(n == 1, "do body executes at least once");

        int sum = 0, i = 0;
        do {
            sum += i;
            i++;
        } while (i < 5);
        check(sum == 10, "do-while iterates");   // 0+1+2+3+4

        System.out.println("PASS (" + passed + " assertions)");
    }
}
