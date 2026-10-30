/*
 * @test
 * @summary JLS 14.11 (Java SE 8): a switch statement transfers to the matching
 *          case and falls through to subsequent cases unless break is used; the
 *          default label matches when no case matches.
 * @run main SwitchTest
 */

public class SwitchTest {
    public static void main(String[] args) {
        int n = 0;
        switch (2) {
            case 1: n += 1;
            case 2: n += 2;   // falls through
            case 3: n += 3;   // executed due to fall-through
            break;
            default: n += 100;
        }
        if (n != 5) throw new AssertionError("fall-through");  // 2 + 3

        int d = 0;
        switch (9) {
            case 1: d = 1; break;
            default: d = 2;
        }
        if (d != 2) throw new AssertionError("default");

        int c = 0;
        switch ('b') {
            case 'a': c = 1; break;
            case 'b': c = 2; break;
        }
        if (c != 2) throw new AssertionError("switch on char");

        System.out.println("PASS");
    }
}
