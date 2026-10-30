/*
 * @test
 * @summary JLS 14.16: continue transfers to the loop-continuation point of the
 *          innermost (or named) iteration statement; finally clauses run before
 *          control is transferred.
 * @run main ContinueStatement
 */

public class ContinueStatement {
    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i % 2 == 0) continue;
            sb.append(i);
        }
        if (!"13".equals(sb.toString())) throw new AssertionError("continue: " + sb);

        int count = 0;
        outer:
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                count++;
                continue outer;   // jumps to the outer loop's update
            }
        }
        if (count != 3) throw new AssertionError("labeled continue: " + count);

        sb.setLength(0);
        for (int i = 0; i < 3; i++) {
            try {
                sb.append(i);
                continue;
            } finally {
                sb.append('f');
            }
        }
        if (!"0f1f2f".equals(sb.toString()))
            throw new AssertionError("finally before continue: " + sb);

        System.out.println("PASS");
    }
}
