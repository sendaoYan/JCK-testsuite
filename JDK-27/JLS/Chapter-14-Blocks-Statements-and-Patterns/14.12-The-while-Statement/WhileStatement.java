/*
 * @test
 * @summary JLS 14.12 / 14.12.1: a while statement evaluates its Boolean
 *          expression each iteration; break completes it normally and continue
 *          (with or without a matching label) re-evaluates the expression.
 * @run main WhileStatement
 */

public class WhileStatement {
    public static void main(String[] args) {
        int i = 0;
        while (i < 3) i++;
        if (i != 3) throw new AssertionError("while count: " + i);

        // Boolean expression is unboxed.
        Boolean cond = Boolean.TRUE;
        int j = 0;
        while (cond) {
            j++;
            if (j == 2) cond = Boolean.FALSE;
        }
        if (j != 2) throw new AssertionError("Boolean unboxing: " + j);

        // break with no label completes the while statement normally.
        int k = 0;
        while (true) { k++; if (k == 5) break; }
        if (k != 5) throw new AssertionError("break: " + k);

        // continue with no label re-evaluates the condition.
        int sum = 0, n = 0;
        while (n < 5) {
            n++;
            if (n % 2 == 0) continue;
            sum += n;
        }
        if (sum != 9) throw new AssertionError("continue: sum == " + sum);  // 1+3+5

        // continue with a matching label re-evaluates the labeled while's condition.
        int count = 0;
        outer:
        while (count < 3) {
            count++;
            continue outer;
        }
        if (count != 3) throw new AssertionError("labeled continue: " + count);

        System.out.println("PASS");
    }
}
