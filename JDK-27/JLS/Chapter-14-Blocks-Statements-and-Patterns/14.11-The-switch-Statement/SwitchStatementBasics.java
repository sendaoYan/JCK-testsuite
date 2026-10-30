/*
 * @test
 * @summary JLS 14.11 / 14.11.3 (Example 14.11.3-1): a switch statement transfers
 *          control to the matching case label and falls through subsequent
 *          statement-group labels. String selectors compare with equals, enum
 *          selectors with the enum constant, and a non-enhanced switch with no
 *          matching label completes normally.
 * @run main SwitchStatementBasics
 */

public class SwitchStatementBasics {
    static String howMany(int k) {
        StringBuilder sb = new StringBuilder();
        switch (k) {
            case 1: sb.append("one ");
            case 2: sb.append("too ");
            case 3: sb.append("many");
        }
        return sb.toString();
    }

    enum E { A, B }

    public static void main(String[] args) {
        if (!"many".equals(howMany(3))) throw new AssertionError("howMany(3)");
        if (!"too many".equals(howMany(2))) throw new AssertionError("howMany(2)");
        if (!"one too many".equals(howMany(1))) throw new AssertionError("howMany(1)");

        int r = 0;
        switch ("hi") {
            case "hi": r = 1; break;
            case "bye": r = 2; break;
        }
        if (r != 1) throw new AssertionError("String selector");

        int e = 0;
        E day = E.B;
        switch (day) {
            case A: e = 1; break;
            case B: e = 2; break;
        }
        if (e != 2) throw new AssertionError("enum selector");

        int n = 0;
        switch (99) { case 1: n = 1; }   // no matching label -> completes normally
        if (n != 0) throw new AssertionError("no-match switch should do nothing");

        System.out.println("PASS");
    }
}
