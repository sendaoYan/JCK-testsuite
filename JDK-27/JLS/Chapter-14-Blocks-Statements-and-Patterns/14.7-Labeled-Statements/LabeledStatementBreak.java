/*
 * @test
 * @summary JLS 14.7: break with a label completes the labeled statement normally;
 *          an identifier may be used both as a label and as a local variable name
 *          without obscuring either (Example 14.7-1).
 * @run main LabeledStatementBreak
 */

public class LabeledStatementBreak {
    public static void main(String[] args) {
        int value = 0;
        test:
        {
            value = 42;
            break test;         // the labeled statement completes normally
            // value = 99;  // unreachable
        }
        if (value != 42) throw new AssertionError("labeled break: " + value);

        // The same identifier used as a label and as a local variable name.
        int max = 0;
        max:
        for (max = 0; max < 3; max++) {
            if (max == 1) continue max;   // 'max' label does not obscure 'max' variable
        }
        if (max != 3) throw new AssertionError("label/variable shadowing: " + max);

        System.out.println("PASS");
    }
}
