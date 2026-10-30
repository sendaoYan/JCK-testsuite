/*
 * @test
 * @summary JLS 14.14.1: a basic for statement executes init, then repeatedly
 *          evaluates the (optional) condition, body and update. A continue runs
 *          the update before the next iteration; an absent condition loops until
 *          break.
 * @run main BasicForStatement
 */

public class BasicForStatement {
    public static void main(String[] args) {
        int sum = 0;
        for (int i = 0; i < 3; i++) sum += i;
        if (sum != 3) throw new AssertionError("basic for: sum == " + sum);  // 0+1+2

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (i == 1) continue;   // update runs before next iteration
            sb.append(i);
        }
        if (!"02".equals(sb.toString()))
            throw new AssertionError("continue runs update: " + sb);

        int n = 0;
        for (;;) { n++; if (n == 7) break; }   // absent condition: only break exits
        if (n != 7) throw new AssertionError("absent condition: " + n);

        System.out.println("PASS");
    }
}
