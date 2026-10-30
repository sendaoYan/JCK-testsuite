/*
 * @test
 * @summary JLS 14.15: break exits the innermost switch/while/do/for, or the
 *          labeled statement named by the identifier; any finally clauses run
 *          before control is transferred.
 * @run main BreakStatement
 */

public class BreakStatement {
    public static void main(String[] args) {
        int r = 0;
        switch (1) {
            case 1: r = 1; break;
            default: r = 2;
        }
        if (r != 1) throw new AssertionError("break out of switch: " + r);

        // labeled break exits a plain (non-loop) block.
        int x = 0;
        outer:
        {
            x = 1;
            break outer;
        }
        if (x != 1) throw new AssertionError("labeled break: " + x);

        // finally runs before break transfers control.
        StringBuilder sb = new StringBuilder();
        while (true) {
            try {
                sb.append("try");
                break;
            } finally {
                sb.append("finally");
            }
        }
        if (!"tryfinally".equals(sb.toString()))
            throw new AssertionError("finally before break: " + sb);

        System.out.println("PASS");
    }
}
