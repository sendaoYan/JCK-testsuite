/*
 * @test
 * @summary JLS 14.11.1 / 14.11.2: a switch rule introduces either a statement
 *          expression, a block, or a throw statement; multiple case labels may
 *          be attached to one switch rule.
 * @run main SwitchRules
 */

public class SwitchRules {
    public static void main(String[] args) {
        int r = 0;
        switch (2) {
            case 1 -> r = 1;        // switch rule expression (statement expression)
            case 2 -> r = 2;
            default -> r = 9;
        }
        if (r != 2) throw new AssertionError("switch rule expression: r == " + r);

        int b = 0;
        switch (5) {
            case 5 -> { b = 1; b++; }   // switch rule block
            default -> { b = 99; }
        }
        if (b != 2) throw new AssertionError("switch rule block: b == " + b);

        int m = 0;
        switch (3) {
            case 1, 2, 3 -> m = 30;     // multiple case labels
            default -> m = 0;
        }
        if (m != 30) throw new AssertionError("multiple labels: m == " + m);

        try {
            ruleThrow();
            throw new AssertionError("switch rule throw should complete abruptly");
        } catch (IllegalStateException e) {
            if (!"rule-throw".equals(e.getMessage()))
                throw new AssertionError("rule-throw message: " + e.getMessage());
        }
    }

    static void ruleThrow() {
        switch (0) {
            case 0 -> throw new IllegalStateException("rule-throw");
            default -> throw new AssertionError("default should not apply");
        }
    }
}
